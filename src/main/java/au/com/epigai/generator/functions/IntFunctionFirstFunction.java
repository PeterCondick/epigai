package au.com.epigai.generator.functions;

import java.util.Arrays;

import au.com.epigai.generator.AbstractFunction;
import au.com.epigai.generator.AbstractIntFunction;
import au.com.epigai.generator.IntFunction;

public class IntFunctionFirstFunction extends AbstractIntFunction {

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
		return first;
	}

	@Override
	public void printCode() {
		//System.out.println(firstName + ";");
		System.out.println(getReturns().getName() + " " + getReturnsName() + " = " + getParameterNames()[0] + ";");
	}

}
