package au.com.epigai.generator.functions.conditions;

import java.util.Optional;

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
	public Optional<Class[]> getParameters() {
		return Optional.empty();
	}

}
