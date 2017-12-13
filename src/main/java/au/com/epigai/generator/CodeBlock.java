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

public class CodeBlock implements PrintableCode {

	private List<AbstractStatement> statements;
	// this will contain variable values by variable name
	private Map<String, Object> variableValues = new HashMap<String, Object>();
	// this will contain all the variable names for each type (ie, int, String) of variable
	private Map<String, Set<String>> variables;
	private Set<String> variableNames = Collections.synchronizedSet(new HashSet<String>());
	// null if this is the top level block
	private CodeBlock parentBlock;
	
	public List<AbstractStatement> getStatements() {
		return statements;
	}
	public void setStatements(List<AbstractStatement> statements) {
		this.statements = statements;
	}
	
	public Map<String, Object> getVariableValues() {
		return variableValues;
	}
	public void addToVariableValues(String key, Object value) {
		variableValues.put(key, value);
	}
	
	public Map<String, Set<String>> getVariables() {
		return variables;
	}
	public void setVariables(Map<String, Set<String>> variables) {
		this.variables = variables;
	}
	public void addToVariables(String key, Set<String> varNames) {
		if (variables == null) {
			throw new RuntimeException("variables is null");
		}
		variables.put(key, varNames);
	}
	
	public Set<String> getVariableNames() {
		return variableNames;
	}
	public void addToVariableNames(String variableName) {
		variableNames.add(variableName);
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
		
		int lastReturnedVal = 0;
		
		// for each function
		for (AbstractStatement statement : getStatements()) {
			if (statement instanceof AbstractIntFunction) {
				AbstractIntFunction intFunction = (AbstractIntFunction)statement;
				// get the names of it's args
				String[] paramNames = intFunction.getParameterNames();
				// get the values of those args
				// TODO making an assumption everything has two params here
				// invoke the function
				int retVal = intFunction.function((Integer)getVariableValues().get(paramNames[0]), (Integer)getVariableValues().get(paramNames[1]));
				// store the returned value in variableValues with the returned name
				addToVariableValues(intFunction.getReturnsName(), retVal);
				//System.out.println("in code block execute variable " + intFunction.getReturnsName() + " set to " + retVal);
				lastReturnedVal = retVal;
			} else if (statement instanceof FlowControl) {
				// TODO
			} else {
				throw new RuntimeException("not yet implemented");
			}
		}
		
		// TODO - this is assuming the last variable is returned
		// TODO and this is assuming its just ints
		if (isTopLevelParent()) {
			Optional<Object> retOptional = Optional.of(Integer.valueOf(lastReturnedVal));
			return Optional.of(retOptional);
		} else {
			return Optional.empty();
		}
	}
	
	
}
