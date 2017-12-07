package au.com.epigai.generator.functions;

public abstract class AbstractStatement {
	
	private String[] parameterNames;
	
	public abstract void printCode();
	
	public String[] getParameterNames() {
		return parameterNames;
	}
	public void setParameterNames(String[] parameterNames) {
		this.parameterNames = parameterNames;
	}
	
}
