package au.com.epigai.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.epigai.generator.functions.AbstractStatement;

public class CodeBlock {

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
	
	public void printCode() {
		getStatements().stream().forEachOrdered(statement -> statement.printCode());
	}
	
}
