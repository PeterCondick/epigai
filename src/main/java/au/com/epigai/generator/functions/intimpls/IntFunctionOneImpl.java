package au.com.epigai.generator.functions.intimpls;

import java.util.Optional;

import au.com.epigai.generator.functions.AbstractIntFunction;

public class IntFunctionOneImpl extends AbstractIntFunction {

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
		return 1;
	}

	@Override
	public void printCode() {
		if (isReturnIsNewVar()) {
			System.out.print(getReturns().getName() + " ");
		}
		System.out.println(getReturnsName() + " = 1;");
	}

}
