package au.com.epigai.generator.functions.flowimpls;

import java.util.Optional;

import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;

public class FlowControlReturn extends AbstractStatement implements FlowControl {
	
	@Override
	public void printCode() {
		System.out.print(getIndent());
		System.out.println("return " + getReturnVariableName() + ";");
	}

	@Override
	public Optional<Class[]> getParameters() {
		return Optional.of(new Class[]{Object.class});
	}
	
	public String getReturnVariableName() {
		return getParameterNames()[0];
	}
	
}
