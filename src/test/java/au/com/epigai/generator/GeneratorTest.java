package au.com.epigai.generator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeneratorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	void testGenerate() {
		Generator.generate(null, null);
		assert(true);
		System.out.println("ran test");
	}
	
}
