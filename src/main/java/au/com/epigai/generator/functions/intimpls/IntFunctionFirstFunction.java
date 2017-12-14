package au.com.epigai.generator.functions.intimpls;

import java.util.Optional;

import au.com.epigai.generator.functions.AbstractIntFunction;

public class IntFunctionFirstFunction extends AbstractIntFunction {

	@Override
	public Class getReturns() {
		// TODO Auto-generated method stub
		return int.class;
	}

	@Override
	public Optional<Class[]> getParameters() {
		return Optional.of(new Class[]{int.class, int.class});
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
