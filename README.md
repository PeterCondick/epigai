### epigai

# Epigenetic AI

Single celled organisms can learn through epigenetics. Genes are switched on or switched off until the required behaviour is achieved.

This project is an attempt to do something similar in java code.

# Generate code from an interface and unit tests

Starting with a list of possible java code instructions, an interface and unit tests that specify the required behaviour epigai can evolve a
java implementation that passes all the unit tests.

# To Use

Client project needs to include epigai and junit.

Write an interface with a method you want to implement (method parameters should be named arg0, arg1 etc).

Write unit tests for the method (Unit test classes need to implement SpecUnitTest). Example implementation below (where toGenInstance is the instance the unit test will test against).

```
public class ToGenTestD implements SpecUnitTest {

	private static Object staticTestObjectInstance;
	
	private ToGen toGenInstance;
	
	public void setInstanceToTest(Object testObjectInstance) {
		staticTestObjectInstance = testObjectInstance;
	}
	
	@Before
	public void setUp() throws Exception {
		if (staticTestObjectInstance != null && staticTestObjectInstance instanceof ToGen) {
			toGenInstance = (ToGen)staticTestObjectInstance;
		} else {
			toGenInstance = new ToGenImpl();
		}
	}
	
	@Test
	public void testAddThenMultiA() {
		//System.out.println("in test addThenMultiA D");
		int result = toGenInstance.addThenMulti(1, 2, 3, 4);
		assertEquals(21, result);
	}
```

Write a utility class to call the generator (example below) and run it.

```
	private static void generateAddThenMulti() {
		Class interfaceToImpl = ToGen.class;
		Method methodToImpl = null;
		try {
			methodToImpl = interfaceToImpl.getMethod("addThenMulti", new Class[] {int.class, int.class, int.class, int.class});
		} catch (NoSuchMethodException nsme) {
			System.out.println("caught a nsme");
			nsme.printStackTrace();
		}
		
		SpecUnitTest testA = new au.com.egaiuser.tests.ToGenTestD();
		
		Generator.generate(interfaceToImpl, methodToImpl, testA);
	}
```

When the generator has found a solution copy and paste the printed out code into your implementation of the interface

```
####### Found a solution #########
int var1 = arg3 + arg2;
int var2 = var1 * arg0;
int var3 = arg0;
int var4 = arg0 + arg1;
int var5 = var1 * var4;
return var5;
called the generator
```

You're done. No need to write the implementation yourself.

Note - currently epigai is quite limited in functionality. Only int parameters are catered for, and a limited range of operations are available.

See the project [https://github.com/PeterCondick/egaiuser](https://github.com/PeterCondick/egaiuser) for an example project that uses this project to evolve code.





