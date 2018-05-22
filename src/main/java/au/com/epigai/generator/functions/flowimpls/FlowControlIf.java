package au.com.epigai.generator.functions.flowimpls;

import java.util.List;
import java.util.Optional;

import au.com.epigai.generator.CodeBlock;
import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;
import au.com.epigai.generator.functions.conditions.AbstractBooleanCondition;

public class FlowControlIf extends AbstractStatement implements FlowControl {
	
	private CodeBlock codeBlock;
	
	private AbstractBooleanCondition booleanCondition;

	private FlowControlElse flowControlElse;
	
	@Override
	public void printCode() {
		System.out.print(getIndent());
		System.out.print("if (");
		booleanCondition.printCode();
		System.out.println(") {");
		codeBlock.printCode();
		System.out.print(getIndent());
		if (flowControlElse != null) {
			System.out.print("}");
			flowControlElse.printCode();
		} else {
			System.out.println("}");
		}
	}

	public void execute(Object first, Object second) {
		if (booleanCondition.evaluate(first, second)) {
			Optional<Optional<Object>> codeBlockRet = codeBlock.execute();
			if (codeBlockRet.isPresent()) {
				// TODO if we want anything other than the top level code block returning stuff will need to implement this properly
				throw new RuntimeException("Cant cope with code blocks returning stuff at the moment");
			}
		} else {
			if (flowControlElse != null) {
				Optional<Optional<Object>> codeBlockRet = flowControlElse.getCodeBlock().execute();
				if (codeBlockRet.isPresent()) {
					// TODO if we want anything other than the top level code block returning stuff will need to implement this properly
					throw new RuntimeException("Cant cope with code blocks returning stuff at the moment");
				}
			}
		}
	}

	public AbstractBooleanCondition getBooleanCondition() {
		return booleanCondition;
	}

	public void setBooleanCondition(AbstractBooleanCondition booleanCondition) {
		this.booleanCondition = booleanCondition;
	}
	
	public CodeBlock getCodeBlock() {
		return codeBlock;
	}

	public void setCodeBlock(CodeBlock codeBlock) {
		this.codeBlock = codeBlock;
	}

	public FlowControlElse getFlowControlElse() {
		return flowControlElse;
	}

	public void setFlowControlElse(FlowControlElse flowControlElse) {
		this.flowControlElse = flowControlElse;
	}

	@Override
	public Optional<Class[]> getParameters() {
		if (booleanCondition == null) {
			throw new RuntimeException("need to set the boolean condition first");
		} else {
			return booleanCondition.getParameters();
		}
	}
	
	@Override
	public String[] getParameterNames() {
		if (booleanCondition == null) {
			throw new RuntimeException("need to set the boolean condition first");
		} else {
			return booleanCondition.getParameterNames();
		}
	}
	
	
}
