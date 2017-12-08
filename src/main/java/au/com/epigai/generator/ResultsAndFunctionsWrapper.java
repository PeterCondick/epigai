package au.com.epigai.generator;

import java.util.List;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;

public class ResultsAndFunctionsWrapper {
	
	List<AbstractStatement> statements;
	TestResults testResults;
	
	public List<AbstractStatement> getStatements() {
		return statements;
	}
	public void setStatements(List<AbstractStatement> statements) {
		this.statements = statements;
	}
	public TestResults getTestResults() {
		return testResults;
	}
	public void setTestResults(TestResults testResults) {
		this.testResults = testResults;
	}
	
	
}
