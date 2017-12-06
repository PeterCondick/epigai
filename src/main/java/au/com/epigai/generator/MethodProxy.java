package au.com.epigai.generator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodProxy implements InvocationHandler {
	
	private static final String VAR_NAME_PREFIX = "var";
	
	private Class interfaceToImplement;
	private Method methodToImplement;
	private List<AbstractIntFunction> intFunctions;
	// this will contain variable values by variable name
	private Map<String, Object> variableValues = new HashMap<String, Object>();
	// this will contain all the variable names for each type (ie, int, String) of variable
	private Map<String, Set<String>> variables;
	private Set<String> variableNames = Collections.synchronizedSet(new HashSet<String>());
	
	private int nameSequence = 1;
	
	public MethodProxy(Class interfaceToImplement, Method methodToImplement, List<AbstractIntFunction> intFunctions) {
		this.interfaceToImplement = interfaceToImplement;
		this.methodToImplement = methodToImplement;
		this.intFunctions = intFunctions;
		
		Parameter[] parameters = methodToImplement.getParameters();
		this.variables = Arrays.stream(parameters).collect(Collectors.toMap(parameter -> parameter.getType().getName(), parameter -> {
			Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
			varNames.add(parameter.getName());
			
			// TODO is this ok - a cheeky side effect - probably should be in an intermediate map/forEach
			variableNames.add(parameter.getName());
			
			return varNames;
		}, (vN1, vN2) -> {
			vN1.addAll(vN2);
			return vN1;
		}));
		
		// go through the functions in order
		// work out what variables there are and randomly decide what variables to pass
		// if not void work out a name of the returned variable and add to the function
		// set the names of the variables called with 
		// this needs to be a for loop not a stream because we want to modify the nameSequence var inside the loop
		for (AbstractIntFunction intFunction : intFunctions) {
				
			// TODO this if statement is making the assumption that all functions have parameters
			// at the moment they do but in the future they might not then this will need to change
			if (intFunction.getParameterNames() != null) {
				// we are evolving - this function already has variable names from a previous run
				// just read the return name from the function and write to variables and variableNames
				Class returnType = intFunction.getReturns();
				if (returnType != null) {
					String retName = intFunction.getReturnsName();
					if (retName == null) {
						throw new RuntimeException("Function has parameter names set but not the return name");
					}
					saveReturnVar(returnType, retName);
				} // else do nothing - the function returns void
			} else {
				// this is a new function
				// set the parameter names in the function
				Class[] params = intFunction.getParameters();
				if (params != null && params.length > 0) {
					// TODO AHHHGGG - java 7
					String[] parameterNames = new String[params.length];
					for (int i = 0; i < params.length; i++) {
						String paramTypeName = params[i].getName();
						if (variables.containsKey(paramTypeName)) {
							Set<String> thisTypeVarNames = variables.get(paramTypeName);
							// TODO use Collections.toArray?
							String[] ttvnArray = thisTypeVarNames.stream().toArray(String[]::new);
							Random random = new Random();
							int randomNumber = random.nextInt(ttvnArray.length);
							parameterNames[i] = ttvnArray[randomNumber];
						} else {
							throw new RuntimeException("no parameters available of specified type");
						}
					}
					intFunction.setParameterNames(parameterNames);
				}
				
				// now deal with the return type
				Class returnType = intFunction.getReturns();
				if (returnType != null) {
					// generate a name
					String returnVarName = generateVarName();
					boolean nameNotUnique = variableNames.contains(returnVarName);
					while (nameNotUnique) {
						returnVarName = generateVarName();
						nameNotUnique = variableNames.contains(returnVarName);
					}
					// so here we have a unique var name
					intFunction.setReturnsName(returnVarName);
					
					// then we have to add the new var to variables and variableNames
					saveReturnVar(returnType, returnVarName);
//					variableNames.add(returnVarName);
//					if (variables.containsKey(returnType.getName())) {
//						// update
//						variables.get(returnType.getName()).add(returnVarName);
//					} else {
//						// add it
//						Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
//						varNames.add(returnVarName);
//						variables.put(returnType.getName(), varNames);
//					}
				}
				
			}
		}
		
		// need a Map of key type and value list of variable names
		//System.out.println("constructor finished");
	}
	
	private String generateVarName() {
		String varName = VAR_NAME_PREFIX + nameSequence;
		nameSequence++;
		return varName;
	}
	
	private void saveReturnVar(Class returnType, String returnVarName) {
		variableNames.add(returnVarName);
		if (variables.containsKey(returnType.getName())) {
			// update
			variables.get(returnType.getName()).add(returnVarName);
		} else {
			// add it
			Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
			varNames.add(returnVarName);
			variables.put(returnType.getName(), varNames);
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// note - args are an array of the args in the correct order
		// from method we can't get the original names of the parameters/args - just made up ones
		// Grrr
		if (method.equals(methodToImplement)) {
			//System.out.println("in the MethodProxy invocationHandler - methods are equal");
			
			// populate variableValues with the passed in args
			Parameter[] params = method.getParameters();
			for (int i = 0; i < params.length; i++) {
				variableValues.put(params[i].getName(), args[i]);
			}
			
			int lastReturnedVal = 0;
			
			// for each function
			for (AbstractIntFunction intFunction : intFunctions) {
				// get the names of it's args
				String[] paramNames = intFunction.getParameterNames();
				// get the values of those args
				// TODO making an assumption everything has two params here
				// invoke the function
				int retVal = intFunction.function((Integer)variableValues.get(paramNames[0]), (Integer)variableValues.get(paramNames[1]));
				// store the returned value in variableValues with the returned name
				variableValues.put(intFunction.getReturnsName(), retVal);
				//System.out.println("variable " + intFunction.getReturnsName() + " set to " + retVal);
				lastReturnedVal = retVal;
			}
			
			// TODO for now just return the last variable
			return lastReturnedVal;
			
//			AbstractIntFunction firstFunction = intFunctions.get(0);
//			
//			int a = firstFunction.function((Integer)args[0], (Integer)args[1]);
			
			//return a;
		} else {
			System.out.println("in the MethodProxy invocationHandler - wrong method");
			throw new RuntimeException("method " + method.getName() + " not equal to methodToImplement " + methodToImplement.getName());
		}
	}

}
