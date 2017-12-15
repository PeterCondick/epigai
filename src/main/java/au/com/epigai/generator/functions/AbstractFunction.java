package au.com.epigai.generator.functions;

public abstract class AbstractFunction extends AbstractStatement {
	
	private String returnsName;
	
	private boolean returnIsNewVar = true;
	
	/**
	 * returning null means the function returns void
	 * @return
	 */
	public abstract Class getReturns();
	
	public String getReturnsName() {
		return returnsName;
	}
	public void setReturnsName(String returnsName) {
		this.returnsName = returnsName;
	}
	
	public boolean isReturnIsNewVar() {
		return returnIsNewVar;
	}

	public void setReturnIsNewVar(boolean returnIsNewVar) {
		this.returnIsNewVar = returnIsNewVar;
	}
	
}
