package au.com.epigai.generator.functions.intimpls;

import java.util.Optional;

import au.com.epigai.generator.functions.AbstractIntFunction;

public class IntFunctionZeroImpl extends AbstractIntFunction {

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
		return 0;
	}

	@Override
	public void printCode() {
		System.out.print(getIndent());
		if (isReturnIsNewVar()) {
			System.out.print(getReturns().getName() + " ");
		}
		System.out.println(getReturnsName() + " = 0;");
	}

}
