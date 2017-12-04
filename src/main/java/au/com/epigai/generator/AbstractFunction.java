package au.com.epigai.generator;

public abstract class AbstractFunction {
	
	private String[] parameterNames;
	private String returnsName;
	
	/**
	 * returning null means the function returns void
	 * @return
	 */
	public abstract Class getReturns();
	
	public abstract Class[] getParameters();
	
	public abstract void printCode();
	
	public String[] getParameterNames() {
		return parameterNames;
	}
	public void setParameterNames(String[] parameterNames) {
		this.parameterNames = parameterNames;
	}
	public String getReturnsName() {
		return returnsName;
	}
	public void setReturnsName(String returnsName) {
		this.returnsName = returnsName;
	}
	
	
}
