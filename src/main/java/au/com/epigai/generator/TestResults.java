package au.com.epigai.generator;

public class TestResults {

	private int ran;
	private int failed;
	private int passed;
	
	public int getRan() {
		return ran;
	}
	public void setRan(int ran) {
		this.ran = ran;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
	public int getPassed() {
		return passed;
	}
	public void setPassed(int passed) {
		this.passed = passed;
	}
	
	public TestResults addAll(TestResults toAdd) {
		this.setRan(this.getRan() + toAdd.getRan());
		this.setFailed(this.getFailed() + toAdd.getFailed());
		this.setPassed(this.getPassed() + toAdd.getPassed());
		return this;
	}
	
}
