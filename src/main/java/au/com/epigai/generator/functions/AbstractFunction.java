package au.com.epigai.generator.functions;

public abstract class AbstractFunction extends AbstractStatement {
	
	private String returnsName;
	
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
	
	
}
