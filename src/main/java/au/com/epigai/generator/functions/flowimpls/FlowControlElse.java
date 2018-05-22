package au.com.epigai.generator.functions.flowimpls;

import java.util.Optional;

import au.com.epigai.generator.CodeBlock;
import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.FlowControl;

public class FlowControlElse extends AbstractStatement implements FlowControl {

	private CodeBlock codeBlock;
	
	@Override
	public void printCode() {
		System.out.print(" else {");
		codeBlock.printCode();
		System.out.print(getIndent());
		System.out.println("}");
	}

	@Override
	public Optional<Class[]> getParameters() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	public CodeBlock getCodeBlock() {
		return codeBlock;
	}

	public void setCodeBlock(CodeBlock codeBlock) {
		this.codeBlock = codeBlock;
	}

}
