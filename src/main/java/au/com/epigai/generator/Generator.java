package au.com.epigai.generator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

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
			List<AbstractIntFunction> intFunctions = ImplEvolver.evolveFrom(null);
			
			// as well as functions - needs to know
			// what variables it has
			// every function call result must be assigned to a variable
		
			while (!solutionFound) {
				
				MethodProxy methodProxy = new MethodProxy(interfaceToImplement, methodToImplement, intFunctions);
		
				Object proxy = Proxy.newProxyInstance(interfaceToImplement.getClassLoader(), new Class[] {interfaceToImplement}, methodProxy);
				
				// run the tests against the implementations
				
				List<Class> classes = new ArrayList<Class>();
				Arrays.stream(unitTestInstances)
						.forEach(unitTestInstance -> {
							if (unitTestInstance instanceof SpecUnitTest) {
								SpecUnitTest sut = (SpecUnitTest)unitTestInstance;
								sut.setInstanceToTest(proxy);
								classes.add(sut.getClass());
							}
						});
				
				// select the best one
				
				// evolve new implementations from that one
			
				TestResults testResults = classes.stream()
						.map(testClass -> {
							JUnitCore junitCore = new JUnitCore();
							Result result = junitCore.run(testClass);
							int ran = result.getRunCount();
							int failed = result.getFailureCount();
							System.out.println("Ran " + ran + " and " + failed + " failed");
							TestResults tr1 = new TestResults();
							tr1.setRan(ran);
							tr1.setFailed(failed);
							tr1.setPassed(ran - failed);
							return tr1;
						}).reduce(new TestResults(), (tr2, tr3) -> tr2.addAll(tr3));
				
				System.out.println("ran " + testResults.getRan() + " tests - passed: " + testResults.getPassed() + " failed: " + testResults.getFailed());
				
				if (testResults.getFailed() > 0) {
					// TODO - select the best so far then pass that into the evolver to evolve from
					intFunctions = ImplEvolver.evolveFrom(null);
				} else {
					solutionFound = true;
					System.out.println("####### Found a solution #########");
					intFunctions.stream().forEachOrdered(intFunction -> intFunction.printCode());
					// TODO - the returns statement
				}
			}
		} else {
			System.out.println("something is null");
		}
		
	}
}
