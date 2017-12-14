package au.com.epigai.generator.functions.conditions;

import java.util.Optional;

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
	public Optional<Class[]> getParameters() {
		return Optional.of(new Class[]{int.class, int.class});
	}

}
