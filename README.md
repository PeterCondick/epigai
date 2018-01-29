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
public class ToGenTestE implements SpecUnitTest {

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
	public void testAddToMaxA() {
		int result = toGenInstance.addToMax(1, 2, 4);
		assertEquals(3, result);
	}
```

Write a utility class to call the generator (example below) and run it.

```
	private static void generateAddToMax() {
		Class interfaceToImpl = ToGen.class;
		Method methodToImpl = null;
		try {
			methodToImpl = interfaceToImpl.getMethod("addToMax", new Class[] {int.class, int.class, int.class});
		} catch (NoSuchMethodException nsme) {
			System.out.println("caught a nsme");
			nsme.printStackTrace();
		}
		
		SpecUnitTest testA = new au.com.egaiuser.tests.ToGenTestE();
		
		Generator.generate(interfaceToImpl, methodToImpl, testA);
	}
```

When the generator has found a solution copy and paste the printed out code into your implementation of the interface
(example tests method should return arg0 + arg1 unless the sum is greater than arg2, in which case return arg2)

```
####### Found a solution #########
    int var1 = arg0 + arg1;
    int var2 = arg2 * arg0;
    if (var1 > arg2) {
        var1 = var2 / arg0;      // note in this case it's generated a strange way to set var1 = arg2 (var1 = (arg2 * arg0)/arg0)
        int var3 = arg1 + arg2;
    }
    return var1;
```

you can then optionally run further in a line-minimisation mode

```
Do you want to try to minimise the lines for another 1000 cycles - y or n?
y
```

until you've had enough

```
Do you want to try to minimise the lines for another 1000 cycles - y or n?
n
###### Stopping. The best solution found was #####
    int var1 = arg0 + arg1;
    if (var1 > arg2) {
        var1 = arg2;        // much simpler way to set var1 = arg2 this time
        int var2 = 0;       // could get it to drop this line too if I continued running it
    }
    return var1;
EpigaiRunner called the generator
```


You're done. No need to write the implementation yourself.





Note - currently epigai is quite limited in functionality. Only int parameters and if statements are catered for, with a limited range of int manipulation operations available.

See the project [https://github.com/PeterCondick/egaiuser](https://github.com/PeterCondick/egaiuser) for an example project that uses this project to evolve code.





