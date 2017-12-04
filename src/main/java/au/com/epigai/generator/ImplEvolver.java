package au.com.epigai.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import au.com.epigai.generator.functions.IntFunctionFirstFunction;
import au.com.epigai.generator.functions.IntFunctionMultiplyImpl;
import au.com.epigai.generator.functions.IntFunctionOneImpl;
import au.com.epigai.generator.functions.IntFunctionSumImpl;

public class ImplEvolver {
	
	private static final Map<String, Class> availableIntFunctions = new HashMap<String, Class>();
	private static final List<String> intFunctionKeys;
	
	private static Random random = new Random();
	
	static {
		availableIntFunctions.put("F", IntFunctionFirstFunction.class);
		availableIntFunctions.put("O", IntFunctionOneImpl.class);
		availableIntFunctions.put("S", IntFunctionSumImpl.class);
		availableIntFunctions.put("M", IntFunctionMultiplyImpl.class);
		
		intFunctionKeys = new ArrayList<String>(availableIntFunctions.keySet());
	}
	
	public static List<AbstractIntFunction> evolveFrom(List<String> existingFunctions) {
		if (existingFunctions != null && existingFunctions.size() > 0) {
			// TODO - evolve from this
			throw new RuntimeException("Not yet implemented");
		} else {
			// random selection
			System.out.println("ImplEvolver evolveFrom initialRandom");
			return initialRandom();
		}
		
		
		//return null;
	}
	
	private static List<AbstractIntFunction> initialRandom() {
		
		int numberOfFunctions = numberOfNewFunctions();
		
//		int randomNumber = random.nextInt(intFunctionKeys.size());
//		System.out.println("randomNumber is " + randomNumber);
//		
//		String key = intFunctionKeys.get(randomNumber);
		
		List<AbstractIntFunction> functionsChosen = new ArrayList<AbstractIntFunction>();
		
		for (int i = 0; i < numberOfFunctions; i++) {
			int randomNumber = random.nextInt(intFunctionKeys.size());
			System.out.println("randomNumber is " + randomNumber);
			
			String key = intFunctionKeys.get(randomNumber);
			try {
				functionsChosen.add((AbstractIntFunction)availableIntFunctions.get(key).newInstance());
			} catch (Exception e) {
				// TODO - handle this more nicely
				throw new RuntimeException("caught an exception", e);
			}
		}
		
		//functionsChosen.add(availableIntFunctions.get(key));
		
		return functionsChosen;
	}
	
	private static int numberOfNewFunctions() {
		return 1 + random.nextInt(3);
	}
	
}
