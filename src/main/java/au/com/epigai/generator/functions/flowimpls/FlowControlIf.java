package au.com.epigai.generator.functions.flowimpls;

import java.util.List;
import java.util.Optional;

import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;
import au.com.epigai.generator.functions.conditions.AbstractBooleanCondition;

public class FlowControlIf extends AbstractStatement implements FlowControl {
	
	private List<AbstractStatement> statementBlock;
	
	private AbstractBooleanCondition booleanCondition;
	
	public void setBooleanCondition(AbstractBooleanCondition booleanCondition) {
		this.booleanCondition = booleanCondition;
	}

	@Override
	public void printCode() {
		System.out.println("return " + getParameterNames()[0] + ";");
	}

	public void execute(Object first, Object second) {
		if (booleanCondition.evaluate(first, second)) {
			//statementBlock.)
		}
	}
	
	public List<AbstractStatement> getStatementBlock() {
		return statementBlock;
	}

	public void setStatementBlock(List<AbstractStatement> statementBlock) {
		this.statementBlock = statementBlock;
	}

	@Override
	public Optional<Class[]> getParameters() {
		return Optional.of(new Class[]{Object.class, Object.class});
	}
	
	
	
	
}
