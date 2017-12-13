package au.com.epigai.generator.functions.conditions;

public class BooleanConditionTrue extends AbstractBooleanCondition {
	
	@Override
	public void printCode() {
		System.out.print("true");
	}

	@Override
	public boolean evaluate(Object first, Object second) {
		return true;
	}

	@Override
	public Class getFirstParameterType() {
		return null;
	}

	@Override
	public Class getSecondParameterType() {
		return null;
	}
	
}
