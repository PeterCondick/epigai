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
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;
import au.com.epigai.generator.functions.flowimpls.FlowControlReturn;

public class MethodProxy implements InvocationHandler {
	
	private static final String VAR_NAME_PREFIX = "var";
	
	private Class interfaceToImplement;
	private Method methodToImplement;
	
	private CodeBlock codeBlock;
	
//	private List<AbstractStatement> statements;
//	// this will contain variable values by variable name
//	private Map<String, Object> variableValues = new HashMap<String, Object>();
//	// this will contain all the variable names for each type (ie, int, String) of variable
//	private Map<String, Set<String>> variables;
//	private Set<String> variableNames = Collections.synchronizedSet(new HashSet<String>());
	
	private int nameSequence = 1;
	
	public MethodProxy(Class interfaceToImplement, Method methodToImplement, CodeBlock codeBlock) {
		this.interfaceToImplement = interfaceToImplement;
		this.methodToImplement = methodToImplement;
		this.codeBlock = codeBlock;
		
		Parameter[] parameters = methodToImplement.getParameters();
		Map<String, Set<String>> variables = Arrays.stream(parameters).collect(Collectors.toMap(parameter -> parameter.getType().getName(), 
			parameter -> {
				Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
				varNames.add(parameter.getName());
				
				// TODO is this ok - a cheeky side effect - probably should be in an intermediate map/forEach
				codeBlock.addToVariableNames(parameter.getName());
				
				return varNames;
			}, (vN1, vN2) -> {
				vN1.addAll(vN2);
				return vN1;
			}));
		this.codeBlock.setVariables(variables);
		
		String lastRetValName = "";
		
		// go through the functions in order
		// work out what variables there are and randomly decide what variables to pass
		// if not void work out a name of the returned variable and add to the function
		// set the names of the variables called with 
		// this needs to be a for loop not a stream because we want to modify the nameSequence var inside the loop
		for (AbstractStatement statement : codeBlock.getStatements()) {
			// TODO this if statement is making the assumption that all functions have parameters
			// at the moment they do but in the future they might not then this will need to change
			if (statement instanceof AbstractIntFunction) {
				AbstractIntFunction intFunction = (AbstractIntFunction)statement;
				
				if (statement.getParameterNames() != null) {
					// we are evolving - this function already has variable names from a previous run
					// just read the return name from the function and write to variables and variableNames
					Class returnType = intFunction.getReturns();
					if (returnType != null) {
						String retName = intFunction.getReturnsName();
						if (retName == null) {
							throw new RuntimeException("Function has parameter names set but not the return name");
						}
						lastRetValName = retName;
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
						statement.setParameterNames(parameterNames);
					}
					
					// now deal with the return type
					Class returnType = intFunction.getReturns();
					if (returnType != null) {
						// generate a name
						String returnVarName = generateVarName();
						boolean nameNotUnique = codeBlock.getVariableNames().contains(returnVarName);
						while (nameNotUnique) {
							returnVarName = generateVarName();
							nameNotUnique = codeBlock.getVariableNames().contains(returnVarName);
						}
						// so here we have a unique var name
						intFunction.setReturnsName(returnVarName);
						
						// then we have to add the new var to variables and variableNames
						lastRetValName = returnVarName;
						saveReturnVar(returnType, returnVarName);
					}
					
				}
			} else if (statement instanceof FlowControl) {
				if (statement instanceof FlowControlReturn) {
					FlowControlReturn fcr = (FlowControlReturn)statement;
					// set the param to be the last variable that was added
					fcr.setParameterNames(new String[] {lastRetValName});
				}
				// TODO
			} else {
				throw new RuntimeException("statement of a type that is not yet implemented");
			}
		}
	}
	
	private String generateVarName() {
		String varName = VAR_NAME_PREFIX + nameSequence;
		nameSequence++;
		return varName;
	}
	
	private void saveReturnVar(Class returnType, String returnVarName) {
		// variable names
		codeBlock.addToVariableNames(returnVarName);
		// variables
		if (codeBlock.getVariables().containsKey(returnType.getName())) {
			// update
			codeBlock.getVariables().get(returnType.getName()).add(returnVarName);
		} else {
			// add it
			Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
			varNames.add(returnVarName);
			codeBlock.addToVariables(returnType.getName(), varNames);
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
				codeBlock.addToVariableValues(params[i].getName(), args[i]);
			}
			
			Optional<Optional<Object>> retValOO = codeBlock.execute();
			
			// able to hard code this like this as we know this is a top level block
			// TODO once dealing with stuff other than just ints will need to revisit this (ie a method may return void)
			if (retValOO.isPresent() && retValOO.get().isPresent()) {
				return retValOO.get().get();
			} else {
				return 0;
			}
			
//			int lastReturnedVal = 0;
//			
//			// for each function
//			for (AbstractStatement statement : codeBlock.getStatements()) {
//				if (statement instanceof AbstractIntFunction) {
//					AbstractIntFunction intFunction = (AbstractIntFunction)statement;
//					// get the names of it's args
//					String[] paramNames = intFunction.getParameterNames();
//					// get the values of those args
//					// TODO making an assumption everything has two params here
//					// invoke the function
//					int retVal = intFunction.function((Integer)codeBlock.getVariableValues().get(paramNames[0]), (Integer)codeBlock.getVariableValues().get(paramNames[1]));
//					// store the returned value in variableValues with the returned name
//					codeBlock.addToVariableValues(intFunction.getReturnsName(), retVal);
//					//System.out.println("variable " + intFunction.getReturnsName() + " set to " + retVal);
//					lastReturnedVal = retVal;
//				} else if (statement instanceof FlowControl) {
//					// TODO
//				} else {
//					throw new RuntimeException("not yet implemented");
//				}
//			}
//			
//			// TODO for now just return the last variable
//			return lastReturnedVal;
			
		} else {
			System.out.println("in the MethodProxy invocationHandler - wrong method");
			throw new RuntimeException("method " + method.getName() + " not equal to methodToImplement " + methodToImplement.getName());
		}
	}

}
