package au.com.epigai.generator.functions.conditions;

public class BooleanConditionIntEquals extends AbstractBooleanCondition {

	
	@Override
	public boolean evaluate(Object first, Object second) {
		if (first != null &&
				first instanceof Integer &&
				second != null &&
				second instanceof Integer) {
			return ((Integer)first).intValue() == ((Integer)second).intValue();
		} else {
			throw new RuntimeException("unexpected types or inputs are null");
		}
	}

	@Override
	public void printCode() {
		System.out.print(getParameterNames()[0] + " == " + getParameterNames()[1]);
	}

	@Override
	public Class getFirstParameterType() {
		return int.class;
	}

	@Override
	public Class getSecondParameterType() {
		return int.class;
	}

}
