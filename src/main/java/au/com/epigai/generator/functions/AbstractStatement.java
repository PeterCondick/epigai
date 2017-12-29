package au.com.epigai.generator.functions;

import java.util.Optional;

import au.com.epigai.generator.PrintableCode;

public abstract class AbstractStatement implements PrintableCode {
	
	/* contains the names of the variables that are passed into this function */
	private String[] parameterNames;
	
	private String indent;
	
	/* The types of the parameters */
	public abstract Optional<Class[]> getParameters();
	
	public String[] getParameterNames() {
		return parameterNames;
	}
	public void setParameterNames(String[] parameterNames) {
		this.parameterNames = parameterNames;
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}
	
	
}
