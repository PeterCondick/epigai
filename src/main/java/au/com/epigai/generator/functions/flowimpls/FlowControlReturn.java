package au.com.epigai.generator.functions.flowimpls;

import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;

public class FlowControlReturn extends AbstractStatement implements FlowControl {
	
	@Override
	public void printCode() {
		// TODO Auto-generated method stub
		System.out.println("return " + getParameterNames()[0] + ";");
	}
	
}
