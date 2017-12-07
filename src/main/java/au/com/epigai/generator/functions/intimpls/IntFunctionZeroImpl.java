package au.com.epigai.generator.functions.intimpls;

import au.com.epigai.generator.functions.AbstractFunction;
import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.IntFunction;

public class IntFunctionZeroImpl extends AbstractIntFunction {

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
		return 0;
	}

	@Override
	public void printCode() {
		System.out.println(getReturns().getName() + " " + getReturnsName() + " = 0;");
	}

}
