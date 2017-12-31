package au.com.epigai.generator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;

public class Generator {
	
	public static void generate(Class interfaceToImplement, Method methodToImplement, SpecUnitTest... unitTestInstances) {	
		
		System.out.println("EpiGAI Generating code");
		
		if (interfaceToImplement != null 
				&& methodToImplement != null 
				&& unitTestInstances != null 
				&& unitTestInstances.length > 0) {
		
			// need to know what method to test
			System.out.println("Interface name is " + interfaceToImplement.getName() + " and canonical " + interfaceToImplement.getCanonicalName());
					
			String methodName = methodToImplement.getName();
			System.out.println("methodName is " + methodName);
			
			Class returnType = methodToImplement.getReturnType();
			System.out.println("Return Type is " + returnType.getName() + " canonical " + returnType.getCanonicalName());
			
			Parameter[] parameters = methodToImplement.getParameters();
			Arrays.stream(parameters).forEach(parameter -> System.out.println("parameter type " + parameter.getType().getName() + " and name " + parameter.getName()));
			
			
			boolean solutionFound = false;
			// need to come up with a few random implementations of it
			CodeBlock codeBlockA = ImplEvolver.evolveFrom(null, methodToImplement.getReturnType().getName());
			CodeBlock codeBlockB = ImplEvolver.evolveFrom(null, methodToImplement.getReturnType().getName());
			CodeBlock codeBlockC = ImplEvolver.evolveFrom(null, methodToImplement.getReturnType().getName());
			
			List<CodeBlock> impls = new ArrayList<CodeBlock>();
			
			impls.add(codeBlockA);
			impls.add(codeBlockB);
			impls.add(codeBlockC);
			
			// as well as functions - needs to know
			// what variables it has
			// every function call result must be assigned to a variable
		
			ResultsAndFunctionsWrapper currentBest = null;
			
			while (!solutionFound) {
				
				// TODO - why do parallel streams not work?
				// maybe something to do with this being static?
				Optional<ResultsAndFunctionsWrapper> rafwOpt = impls.stream()
					.map(codeBlock -> {
						ResultsAndFunctionsWrapper resultsAndFunctions = testOneImpl(codeBlock, interfaceToImplement, methodToImplement, unitTestInstances);
						return resultsAndFunctions;
					}).max((rafw1, rafw2) -> {
						// positive means first greater than second
						if (rafw1.getTestResults().getPassed() > rafw2.getTestResults().getPassed()) {
							return 1;
						} else if (rafw1.getTestResults().getPassed() == rafw2.getTestResults().getPassed()) {
							// return the one with the fewest lines
							if (rafw1.getCodeBlock().getStatements().size() < rafw2.getCodeBlock().getStatements().size()) {
								return 1;
							} else {
								return -1;
							}
						} else {
							return -1;
						}
					});
				
				// rafwOpt should now be the best result
				if (rafwOpt.isPresent()) {
					
					TestResults testResults = rafwOpt.get().getTestResults();
					
					if (testResults.getFailed() > 0) {
						System.out.println("##### New results ##### passed " + testResults.getPassed() + " failed " + testResults.getFailed());
						rafwOpt.get().getCodeBlock().printCode();
						if (currentBest != null) {
							System.out.println("##### Current best solution ##### passed " + currentBest.getTestResults().getPassed() + " failed " + currentBest.getTestResults().getFailed());
							currentBest.getCodeBlock().printCode();
						} else {
							System.out.println("##### No current solution #####");
						}
						// compare with the previous best
						if (currentBest != null && currentBest.getTestResults().getPassed() > testResults.getPassed()) {
							// evolve from the previous impl (current best)
							System.out.println("##### Sticking with the previous solution #####");
							evolve(impls, currentBest, methodToImplement.getReturnType().getName());
						} else if (currentBest != null && currentBest.getTestResults().getPassed() == testResults.getPassed()) {
							// found an equivalent solution
							// evolve from the one with the least lines - or the new one if they are the same
							if (currentBest.getCodeBlock().getStatements().size() < rafwOpt.get().getCodeBlock().getStatements().size()) {
								// stay with current best
								System.out.println("##### Sticking with the previous solution - less lines#####");
								evolve(impls, currentBest, methodToImplement.getReturnType().getName());
							} else {
								// evolve from the new impl
								System.out.println("####### New results are the new best solution - less or equal lines#########");
								currentBest = rafwOpt.get();
								evolve(impls, rafwOpt.get(), methodToImplement.getReturnType().getName());
							}
						} else {
							// found a new better solution
							// evolve from the new impl
							System.out.println("####### New results are the new best solution #########");
							currentBest = rafwOpt.get();
							evolve(impls, rafwOpt.get(), methodToImplement.getReturnType().getName());
						}
					} else {
						solutionFound = true;
						System.out.println("####### Found a solution #########");
						rafwOpt.get().getCodeBlock().printCode();
						// TODO - the returns statement
					}
				} else {
					throw new RuntimeException("no test results - unexpected");
				}
			}
		} else {
			System.out.println("something is null");
		}
		
	}
	
	private static ResultsAndFunctionsWrapper testOneImpl(CodeBlock codeBlock, 
													Class interfaceToImplement, 
													Method methodToImplement, 
													SpecUnitTest... unitTestInstances) {
		
		MethodProxy methodProxy = new MethodProxy(interfaceToImplement, methodToImplement, codeBlock);
		
		Object proxy = Proxy.newProxyInstance(interfaceToImplement.getClassLoader(), new Class[] {interfaceToImplement}, methodProxy);
		
		// run the tests against the implementation
		List<Class> classes = new ArrayList<Class>();
		Arrays.stream(unitTestInstances)
				.forEach(unitTestInstance -> {
					if (unitTestInstance instanceof SpecUnitTest) {
						SpecUnitTest sut = (SpecUnitTest)unitTestInstance;
						sut.setInstanceToTest(proxy);
						classes.add(sut.getClass());
					}
				});
	
		TestResults testResults = classes.stream()
				.map(testClass -> {
					JUnitCore junitCore = new JUnitCore();
					Result result = junitCore.run(testClass);
					int ran = result.getRunCount();
					int failed = result.getFailureCount();
					//System.out.println("Ran " + ran + " and " + failed + " failed");
					TestResults tr1 = new TestResults();
					tr1.setRan(ran);
					tr1.setFailed(failed);
					tr1.setPassed(ran - failed);
					return tr1;
				}).reduce(new TestResults(), (tr2, tr3) -> tr2.addAll(tr3));
		
		System.out.println("ran " + testResults.getRan() + " tests - passed: " + testResults.getPassed() + " failed: " + testResults.getFailed());
		
		ResultsAndFunctionsWrapper resultsAndFunctions = new ResultsAndFunctionsWrapper();
		resultsAndFunctions.setCodeBlock(codeBlock);
		resultsAndFunctions.setTestResults(testResults);
		return resultsAndFunctions;
	}
	
	private static List<CodeBlock> evolve(List<CodeBlock> impls, ResultsAndFunctionsWrapper evolveFrom, String returnTypeName) {
		
		impls.clear();
		CodeBlock codeBlockNewA = ImplEvolver.evolveFrom(evolveFrom.getCodeBlock(), returnTypeName);
		CodeBlock codeBlockNewB = ImplEvolver.evolveFrom(evolveFrom.getCodeBlock(), returnTypeName);
		CodeBlock codeBlockNewC = ImplEvolver.evolveFrom(evolveFrom.getCodeBlock(), returnTypeName);
		// and bung in a new from scratch impl just in case it's gone the wrong way
		CodeBlock codeBlockNewD = ImplEvolver.evolveFrom(null, returnTypeName);
		
		impls.add(codeBlockNewA);
		impls.add(codeBlockNewB);
		impls.add(codeBlockNewC);
		impls.add(codeBlockNewD);
		
		return impls;
	}
	
}
