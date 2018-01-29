package au.com.epigai.generator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;

public class Generator {
	
	private static final int DEFAULT_MINIMISE_PER_CYCLE = 1000;
	
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
			
			// to read command line input
			Scanner scanner = new Scanner(System.in);
			
			Parameter[] parameters = methodToImplement.getParameters();
			Arrays.stream(parameters).forEach(parameter -> System.out.println("parameter type " + parameter.getType().getName() + " and name " + parameter.getName()));
			
			
			boolean solutionFound = false;
			boolean minimising = false;
			int minimisingCycles = 0;
			
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
			
			while (!solutionFound || minimising) {
				
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
							if (rafw1.getCodeBlock().getLineCount() < rafw2.getCodeBlock().getLineCount()) {
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
					
					if (!solutionFound && testResults.getFailed() > 0) {
						// should not get in here when minimising
						System.out.println("##### New results ##### passed " + testResults.getPassed() + 
												" failed " + testResults.getFailed() + " lines " +
												rafwOpt.get().getCodeBlock().getLineCount());
						rafwOpt.get().getCodeBlock().printCode();
						if (currentBest != null) {
							System.out.println("##### Current best solution ##### passed " + 
												currentBest.getTestResults().getPassed() + 
												" failed " + currentBest.getTestResults().getFailed() + 
												" lines " + currentBest.getCodeBlock().getLineCount());
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
							if (currentBest.getCodeBlock().getLineCount() < rafwOpt.get().getCodeBlock().getLineCount()) {
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
						// TODO - addToMax sometimes coming up with invalid solutions if an ifIntEquals is involved
												
						// if in here we have either just found a solution or we found a solution earlier and are now trying to minimise the number of lines
						if (!solutionFound) {
							solutionFound = true;
							currentBest = rafwOpt.get();
							System.out.println("####### Found a solution #########");
							currentBest.getCodeBlock().printCode();
						}
						
						if (minimising) {
							System.out.println("###### Current solution - passes all tests ##### - lines " + currentBest.getCodeBlock().getLineCount());
							currentBest.getCodeBlock().printCode();
							if (testResults.getFailed() == 0) {
								System.out.println("###### New Solution - also passes all tests #####");
								rafwOpt.get().getCodeBlock().printCode();
							} else {
								System.out.println("###### New Solution does not pass all tests #####");
							}
							
							// so if we are here then both the current best and the new one passed all tests and we are minimising
							if (testResults.getFailed() > 0) {
								// new impl didn't pass all tests - stay with current best
								System.out.println("##### Sticking with the previous solution as the new solution didn't pass #####");
							} else if (currentBest != null && 
									currentBest.getCodeBlock().getLineCount() < rafwOpt.get().getCodeBlock().getLineCount()) {
								// it is possible that it found a solution on the first attempt so we need the currentBest != null check
								// stay with current best
								System.out.println("##### Sticking with the previous solution - less lines#####");
							} else {
								// evolve from the new impl
								System.out.println("####### New results are the new best solution - less or equal lines#########");
								currentBest = rafwOpt.get();
							}
						}
						
						if (minimising && minimisingCycles > DEFAULT_MINIMISE_PER_CYCLE) {
							minimising = false;
						}
						
						if (!minimising) {
							// do we continue and minimiseLines?
							System.out.println("Do you want to try to minimise the lines for another " + DEFAULT_MINIMISE_PER_CYCLE + " cycles - y or n?");
							String minStr = scanner.next();
							if ("y".equalsIgnoreCase(minStr) || "yes".equalsIgnoreCase(minStr)) {
								System.out.println("Going to try to minimise lines for another " + DEFAULT_MINIMISE_PER_CYCLE + " cycles");
								minimising = true;
								minimisingCycles = 0;
							} else {
								minimising = false;
								System.out.println("###### Stopping. The best solution found was #####");
								currentBest.getCodeBlock().printCode();
							}
						}
						
						if (minimising) {
							evolve(impls, currentBest, methodToImplement.getReturnType().getName());
						}
						
					}
				} else {
					throw new RuntimeException("no test results - unexpected");
				}
				
				if (minimising) {
					minimisingCycles++;
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
