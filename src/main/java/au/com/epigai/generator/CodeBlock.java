package au.com.epigai.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;
import au.com.epigai.generator.functions.flowimpls.FlowControlIf;

public class CodeBlock implements PrintableCode {

	private static final String INDENT = "    ";
	
	private List<AbstractStatement> statements;
	// this will contain variable values by variable name
	private Map<String, Object> variableValues = new HashMap<String, Object>();
	// this will contain all the variable names for each type (ie, int, String) of variable including the method arguments
	private Map<String, Set<String>> variables = new HashMap<String, Set<String>>();
	// this will contain all the variable names for each type (ie, int, String) of variable excluding the method arguments
	private Map<String, Set<String>> variablesNoArgs = new HashMap<String, Set<String>>();
	private Set<String> variableNames = Collections.synchronizedSet(new HashSet<String>());
	// null if this is the top level block
	private CodeBlock parentBlock;
	//private boolean updatedUpperLevelVariable = false;
	//private String lastUpperLevelVariableUpdated = null;
	private String lastUpdatedVariable;
	
	public List<AbstractStatement> getStatements() {
		return statements;
	}
	public void setStatements(List<AbstractStatement> statements) {
		this.statements = statements;
	}
	
	public Map<String, Object> getVariableValues() {
		return variableValues;
	}
	public void addToVariableValues(String key, Object value, boolean newVariable) {
		if (newVariable) {
			// something is creating a new variable
			variableValues.put(key, value);
			lastUpdatedVariable = key;
		} else {
			// the value of an existing variable is being updated
			if (variableValues.containsKey(key)) {
				// the variable was created in this block
				variableValues.put(key, value);
				lastUpdatedVariable = key;
			} else {
				if (isTopLevelParent()) {
					throw new RuntimeException("can not find variable " + key);
				} else {
					// maybe the variable is in the parent block
					parentBlock.addToVariableValues(key, value, newVariable);
				}
			}
		}
	}
	public Object getVariableValue(String varName) {
		if (variableValues.containsKey(varName)) {
			return variableValues.get(varName);
		} else {
			if (isTopLevelParent()) {
				throw new RuntimeException("Variable " + varName + " not found");
			} else {
				return parentBlock.getVariableValue(varName);
			}
		}
	}
	
	public Map<String, Set<String>> getVariables() {
		return variables;
	}
	/**
	 * copy variables, variablesNoArgs and variableNames
	 * 
	 * @param copyFrom
	 */
	public void copyVariables(CodeBlock copyFrom) {
		// copy variables
		Set<String> cfVKeys = copyFrom.getVariables().keySet();
		for (String cfVKey : cfVKeys) {
			Set<String> cfVVals = copyFrom.getVariables().get(cfVKey);
			Set<String> valsCopied = Collections.synchronizedSet(new HashSet<String>());
			valsCopied.addAll(cfVVals);
			variables.put(cfVKey, valsCopied);
		}
		
		// copy variablesNoArgs
		Set<String> cfVKeysNoArgs = copyFrom.getVariablesNoArgs().keySet();
		for (String cfVKeyNoArgs : cfVKeysNoArgs) {
			Set<String> cfVValsNoArgs = copyFrom.getVariablesNoArgs().get(cfVKeyNoArgs);
			Set<String> valsCopiedNoArgs = Collections.synchronizedSet(new HashSet<String>());
			valsCopiedNoArgs.addAll(cfVValsNoArgs);
			variablesNoArgs.put(cfVKeyNoArgs, valsCopiedNoArgs);
		}
		
		// copy variableNames
		variableNames.addAll(copyFrom.getVariableNames());
	}
	
	public void addToVariables(Map<String, Set<String>> newVariables, boolean addingArgs) {
		if (variables.isEmpty()) {
			variables.putAll(newVariables);
			if (!addingArgs) {
				variablesNoArgs.putAll(newVariables);
			}
		} else {
			throw new RuntimeException("trying to overwrite existing variables");
		}
	}
	public void addToVariables(String key, Set<String> varNames, boolean addingArgs) {
		if (variables.containsKey(key)) {
			Set<String> existingVarNames = variables.get(key);
			for (String newName : varNames) {
				existingVarNames.add(newName);
			}
		} else {
			variables.put(key, varNames);
		}
		if (!addingArgs) {
			if (variablesNoArgs.containsKey(key)) {
				Set<String> existingVarNamesNoArgs = variablesNoArgs.get(key);
				for (String newNameNoArgs : varNames) {
					existingVarNamesNoArgs.add(newNameNoArgs);
				}
			} else {
				variablesNoArgs.put(key, varNames);
			}
		}
	}
	
	public Map<String, Set<String>> getVariablesNoArgs() {
		return variablesNoArgs;
	}
//	public void addToVariablesNoArgs(Map<String, Set<String>> newVariables) {
//		variablesNoArgs.putAll(newVariables);
//	}
//	public void addToVariablesNoArgs(String key, Set<String> varNames) {
//		if (variablesNoArgs == null) {
//			throw new RuntimeException("variables is null");
//		}
//		variablesNoArgs.put(key, varNames);
//	}
	public boolean isDoesVariableOfTypeExist(String typeName, boolean includeArgs) {
		Set<String> varsForType = null;
		if (includeArgs) {
			varsForType = variables.get(typeName);
		} else {
			varsForType = variablesNoArgs.get(typeName);
		}
		return varsForType != null && !varsForType.isEmpty();
	}
	
	public Set<String> getVariableNames() {
		return variableNames;
	}
	public void addToVariableNames(String variableName) {
		variableNames.add(variableName);
	}
	public void addToVariableNames(Set<String> names) {
		variableNames.addAll(names);
	}
	
	public CodeBlock getParentBlock() {
		return parentBlock;
	}
	public void setParentBlock(CodeBlock parentBlock) {
		this.parentBlock = parentBlock;
	}
	public boolean isTopLevelParent() {
		if (this.parentBlock == null) {
			return true;
		} else {
			return false;
		}
	}
	
//	public boolean isUpdatedUpperLevelVariable() {
//		return updatedUpperLevelVariable;
//	}
//	public String getLastUpperLevelVariableUpdated() {
//		return lastUpperLevelVariableUpdated;
//	}
	
	@Override
	public void printCode() {
		getStatements().stream().forEachOrdered(statement -> statement.printCode());
	}
	
	/**
	 * Execute the code contained in this code block
	 * 
	 * @return An Optional in an Optional. This is to distinguise between a piece of code that does not end with a return statement
	 *         so doesn't return anything (get back one empty optional) and a piece of code that does return something but that something
	 *         is null (get back an empty optional in an optional) and a piece of code that does return something 
	 *         (get back something in an optional in an optional)
	 */
	public Optional<Optional<Object>> execute() {
		
		//int lastReturnedVal = 0;
		
		// for each function
		for (AbstractStatement statement : getStatements()) {
			if (statement instanceof AbstractIntFunction) {
				AbstractIntFunction intFunction = (AbstractIntFunction)statement;
				// get the names of it's args
				String[] paramNames = intFunction.getParameterNames();
				// get the values of those args
				// TODO making an assumption everything has two params here
				// invoke the function
				int retVal = intFunction.function((Integer)getVariableValue(paramNames[0]), (Integer)getVariableValue(paramNames[1]));
				// store the returned value in variableValues with the returned name
				addToVariableValues(intFunction.getReturnsName(), retVal, intFunction.isReturnIsNewVar());
				//System.out.println("in code block execute variable " + intFunction.getReturnsName() + " set to " + retVal);
				//lastReturnedVal = retVal;
				//System.out.println("processing an int func");
//				if (!isTopLevelParent()) {
//					System.out.println("processing an int func not in a top level code block");
//				}
//				if (!isTopLevelParent() && 
//						!intFunction.isReturnIsNewVar() &&
//						!variableValues.containsKey(intFunction.getReturnsName())) {
//					// it's setting a var that is at a upper level
//					updatedUpperLevelVariable = true;
//					lastUpperLevelVariableUpdated = intFunction.getReturnsName();
//					System.out.println("in a nested codeblock - setting upper updated var to " + intFunction.getReturnsName());
//				}
			} else if (statement instanceof FlowControl) {
				if (statement instanceof FlowControlIf) {
					FlowControlIf fcIf = (FlowControlIf)statement;
					Optional<Class[]> optIfParamTypes = fcIf.getParameters();
					if (optIfParamTypes.isPresent()) {
						String[] paramNames = fcIf.getParameterNames();
						// currently just assuming if there are params there are 2 of them and they are ints
						fcIf.execute((Integer)getVariableValue(paramNames[0]), (Integer)getVariableValue(paramNames[1]));
					} else {
						fcIf.execute(null, null);
					}
//					if (fcIf.getCodeBlock().updatedUpperLevelVariable) {
//						// TODO this only works at the moment because only one extra level is allowed
//						// if nested code blocks could happen then we'd probably need to know all the UpperLevelVariableUpdated
//						// variables - so we could find the last one for this level
//						if (variableValues.containsKey(fcIf.getCodeBlock().getLastUpperLevelVariableUpdated())) {
//							lastReturnedVal = (Integer)variableValues.get(fcIf.getCodeBlock().getLastUpperLevelVariableUpdated());
//							System.out.println("the if statement set the value of " + fcIf.getCodeBlock().getLastUpperLevelVariableUpdated() + " to " + lastReturnedVal);
//						}
//					}
				} else {
					// TODO
				}
			} else {
				throw new RuntimeException("not yet implemented");
			}
		}
		
		// TODO - this is assuming the last variable is returned
		// TODO and this is assuming its just ints
		if (isTopLevelParent() && lastUpdatedVariable != null) {
			Optional<Object> retOptional = Optional.of((Integer)variableValues.get(lastUpdatedVariable));
			return Optional.of(retOptional);
		} else {
			return Optional.empty();
		}
	}
	
	public String getIndent() {
		if (isTopLevelParent()) {
			return INDENT;
		} else {
			return parentBlock.getIndent() + INDENT;
		}
	}
	
}
