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
import au.com.epigai.generator.functions.conditions.AbstractBooleanCondition;
import au.com.epigai.generator.functions.flowimpls.FlowControlIf;
import au.com.epigai.generator.functions.flowimpls.FlowControlReturn;

public class MethodProxy implements InvocationHandler {
	
	private static final String VAR_NAME_PREFIX = "var";
	
	private Class interfaceToImplement;
	private Method methodToImplement;
	
	private CodeBlock codeBlock;
	
	private Random random = new Random();
	
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
		
		// this is the top level code block and we are adding the method parameters to the variables for the code block
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
		this.codeBlock.addToVariables(variables, true);
		//this.codeBlock.setVariables(variables);
		
		String lastRetValName = "";
		
		// go through the functions in order
		// work out what variables there are and randomly decide what variables to pass
		// if not void work out a name of the returned variable and add to the function
		// set the names of the variables called with 
		// this needs to be a for loop not a stream because we want to modify the nameSequence var inside the loop
		for (AbstractStatement statement : codeBlock.getStatements()) {
			statement.setIndent(codeBlock.getIndent());
			
			// TODO this if statement is making the assumption that all functions have parameters
			// at the moment they do but in the future they might not then this will need to change
			if (statement instanceof AbstractIntFunction) {
				AbstractIntFunction intFunction = (AbstractIntFunction)statement;
				
				lastRetValName = varsForIntFunction(intFunction, codeBlock);
				
			} else if (statement instanceof FlowControl) {
				if (statement instanceof FlowControlReturn) {
					FlowControlReturn fcr = (FlowControlReturn)statement;
					// set the param to be a random declared variable of the return type
					String retTypeName = this.methodToImplement.getReturnType().getName();
					if (!this.codeBlock.isDoesVariableOfTypeExist(retTypeName, false)) {
						throw new RuntimeException("handling a return statement but no variable declaired of type " + retTypeName);
					}
					String retVarName = getRandomExistingVariableOfType(this.codeBlock, retTypeName, false);
					fcr.setParameterNames(new String[] {retVarName});
					//fcr.setParameterNames(new String[] {lastRetValName});
				}
				if (statement instanceof FlowControlIf) {
					FlowControlIf fcIf = (FlowControlIf)statement;
					
					// pass parameters into the boolean condition
					AbstractBooleanCondition ifBoolCond = fcIf.getBooleanCondition();
					Optional<Class[]> ifbcParamTypes = ifBoolCond.getParameters();
					if (ifbcParamTypes.isPresent()) {
						// it accepts parameters - are they already set?
						String[] ifbcParamNames = ifBoolCond.getParameterNames();
						if (ifbcParamNames != null && ifbcParamNames.length > 0) {
							// already set - no need to do anything for the condition
						} else {
							Class[] params = ifbcParamTypes.get();
							String[] parameterNames = new String[params.length];
							for (int i = 0; i < params.length; i++) {
								String paramTypeName = params[i].getName();
								if (variables.containsKey(paramTypeName)) {
									Set<String> thisTypeVarNames = variables.get(paramTypeName);
									// TODO use Collections.toArray?
									String[] ttvnArray = thisTypeVarNames.stream().toArray(String[]::new);
									int randomNumber = random.nextInt(ttvnArray.length);
									parameterNames[i] = ttvnArray[randomNumber];
								} else {
									throw new RuntimeException("no parameters available of specified type");
								}
							}
							ifBoolCond.setParameterNames(parameterNames);
						}
					}
					
					// work out the variables for the code block
					CodeBlock ifCodeBlock = fcIf.getCodeBlock();
					ifCodeBlock.copyVariables(codeBlock);
//					ifCodeBlock.addToVariables(codeBlock.getVariables());
//					ifCodeBlock.addToVariableNames(codeBlock.getVariableNames());
					
					for (AbstractStatement ifCBStatement : ifCodeBlock.getStatements()) {
						ifCBStatement.setIndent(ifCodeBlock.getIndent());
						// TODO this if statement is making the assumption that all functions have parameters
						// at the moment they do but in the future they might not then this will need to change
						if (ifCBStatement instanceof AbstractIntFunction) {
							AbstractIntFunction ifIntFunction = (AbstractIntFunction)ifCBStatement;
							
							// TODO - need to implement this properly
							String lastVarInIfCodeBlock = varsForIntFunction(ifIntFunction, ifCodeBlock);
						}
					}
					
				}
				// TODO
			} else {
				throw new RuntimeException("statement of a type that is not yet implemented");
			}
		}
	}
	
	private String varsForIntFunction(AbstractIntFunction intFunction, CodeBlock codeBlock) {
		String returnValName = "";
		
		if (intFunction.getParameterNames() != null) {
			// we are evolving - this function already has variable names from a previous run
			// just read the return name from the function and write to variables and variableNames
			Class returnType = intFunction.getReturns();
			if (returnType != null) {
				String retName = intFunction.getReturnsName();
				if (retName == null) {
					throw new RuntimeException("Function has parameter names set but not the return name");
				}
				returnValName = retName;
				if (intFunction.isReturnIsNewVar()) {
					saveReturnVar(returnType, retName);
				}
			} // else do nothing - the function returns void
		} else {
			// this is a new function
			// set the parameter names in the function
			Optional<Class[]> paramsOpt = intFunction.getParameters();
			Class[] params = null;
			if (paramsOpt.isPresent()) {
				params = paramsOpt.get();
			}
			if (params != null && params.length > 0) {
				// TODO AHHHGGG - java 7
				String[] parameterNames = new String[params.length];
				for (int i = 0; i < params.length; i++) {
					String paramTypeName = params[i].getName();
					//if (codeBlock.getVariables().containsKey(paramTypeName)) {
					if (codeBlock.isDoesVariableOfTypeExist(paramTypeName, true)) {
						parameterNames[i] = getRandomExistingVariableOfType(codeBlock, paramTypeName, true);
//						Set<String> thisTypeVarNames = codeBlock.getVariables().get(paramTypeName);
//						// TODO use Collections.toArray?
//						String[] ttvnArray = thisTypeVarNames.stream().toArray(String[]::new);
//						int randomNumber = random.nextInt(ttvnArray.length);
//						parameterNames[i] = ttvnArray[randomNumber];
					} else {
						throw new RuntimeException("no parameters available of specified type");
					}
				}
				intFunction.setParameterNames(parameterNames);
			}
			
			// now deal with the return type
			Class returnType = intFunction.getReturns();
			if (returnType != null) {
				
				// TODO - decide if we are going to make the return a new variable or not
				// need to check if there is a variable of the appropriate type first
				int retNewVarOrNot = random.nextInt(2);
				if (retNewVarOrNot == 0 && codeBlock.isDoesVariableOfTypeExist(returnType.getName(), false)) {
					// update an existing variable
					intFunction.setReturnIsNewVar(false);
					// get one of the existing variables
					String retVarName = getRandomExistingVariableOfType(codeBlock, returnType.getName(), false);
					// set that to the return name
					intFunction.setReturnsName(retVarName);
					returnValName = retVarName;
				} else {
					// declare a new variable
					// default is true but just being cautious
					intFunction.setReturnIsNewVar(true);
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
					returnValName = returnVarName;
					saveReturnVar(returnType, returnVarName);
				}
			}
			
		}
		
		return returnValName;
	}
	
	private String getRandomExistingVariableOfType(CodeBlock codeBlock, String paramTypeName, boolean includeArgs) {
		Set<String> thisTypeVarNames = null;
		if (includeArgs) {
			thisTypeVarNames = codeBlock.getVariables().get(paramTypeName);
		} else {
			thisTypeVarNames = codeBlock.getVariablesNoArgs().get(paramTypeName);
		}
		// TODO use Collections.toArray?
		String[] ttvnArray = thisTypeVarNames.stream().toArray(String[]::new);
		int randomNumber = random.nextInt(ttvnArray.length);
		return ttvnArray[randomNumber];
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
//		if (codeBlock.getVariables().containsKey(returnType.getName())) {
//			// update
//			codeBlock.getVariables().get(returnType.getName()).add(returnVarName);
//		} else {
			// add it
		Set<String> varNames = Collections.synchronizedSet(new HashSet<String>());
		varNames.add(returnVarName);
		codeBlock.addToVariables(returnType.getName(), varNames, false);
//		}
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
				codeBlock.addToVariableValues(params[i].getName(), args[i], true);
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
