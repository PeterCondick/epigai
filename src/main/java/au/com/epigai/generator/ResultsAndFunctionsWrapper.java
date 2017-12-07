package au.com.epigai.generator;

import java.util.List;

import au.com.epigai.generator.functions.AbstractIntFunction;

public class ResultsAndFunctionsWrapper {
	
	List<AbstractIntFunction> intFunctions;
	TestResults testResults;
	
	public List<AbstractIntFunction> getIntFunctions() {
		return intFunctions;
	}
	public void setIntFunctions(List<AbstractIntFunction> intFunctions) {
		this.intFunctions = intFunctions;
	}
	public TestResults getTestResults() {
		return testResults;
	}
	public void setTestResults(TestResults testResults) {
		this.testResults = testResults;
	}
	
	
}
