package au.com.epigai.generator.functions.intimpls;

import au.com.epigai.generator.functions.AbstractFunction;
import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.IntFunction;

public class IntFunctionMultiplyImpl extends AbstractIntFunction {

	@Override
	public Class getReturns() {
		// TODO Auto-generated method stub
		return int.class;
	}

	@Override
	public Class[] getParameters() {
		// TODO Auto-generated method stub
		return new Class[]{int.class, int.class};
	}
	
	@Override
	public int function(int first, int second) {
		return first * second;
	}

	@Override
	public void printCode() {
		//System.out.println(firstName + " * " + secondName + ";");
		System.out.println(getReturns().getName() + " " + getReturnsName() + " = " + getParameterNames()[0] + " * " + getParameterNames()[1] + ";");
	}

}
