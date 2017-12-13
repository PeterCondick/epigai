package au.com.epigai.generator.functions;

import au.com.epigai.generator.PrintableCode;

public abstract class AbstractStatement implements PrintableCode {
	
	private String[] parameterNames;
	
	public String[] getParameterNames() {
		return parameterNames;
	}
	public void setParameterNames(String[] parameterNames) {
		this.parameterNames = parameterNames;
	}
	
}
