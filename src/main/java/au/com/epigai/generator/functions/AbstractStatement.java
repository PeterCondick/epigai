package au.com.epigai.generator.functions;

import java.util.Optional;

import au.com.epigai.generator.PrintableCode;

public abstract class AbstractStatement implements PrintableCode {
	
	/* contains the names of the variables that are passed into this function */
	private String[] parameterNames;
	
	/* The types of the parameters */
	public abstract Optional<Class[]> getParameters();
	
	public String[] getParameterNames() {
		return parameterNames;
	}
	public void setParameterNames(String[] parameterNames) {
		this.parameterNames = parameterNames;
	}
	
}
