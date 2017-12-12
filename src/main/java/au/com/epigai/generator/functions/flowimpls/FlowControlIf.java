package au.com.epigai.generator.functions.flowimpls;

import java.util.List;

import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;

public class FlowControlIf extends AbstractStatement implements FlowControl {
	
	private List<AbstractStatement> statementBlock;
	
	@Override
	public void printCode() {
		System.out.println("return " + getParameterNames()[0] + ";");
	}

	public List<AbstractStatement> getStatementBlock() {
		return statementBlock;
	}

	public void setStatementBlock(List<AbstractStatement> statementBlock) {
		this.statementBlock = statementBlock;
	}
	
	
	
	
}
