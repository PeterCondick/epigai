package au.com.epigai.generator.functions.conditions;

import au.com.epigai.generator.functions.AbstractStatement;

public abstract class AbstractBooleanCondition extends AbstractStatement implements BooleanCondition {
	
	public abstract Class getFirstParameterType();
	
	public abstract Class getSecondParameterType();
	
}
