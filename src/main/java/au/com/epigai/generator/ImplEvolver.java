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
	
	public static List<AbstractIntFunction> evolveFrom(List<AbstractIntFunction> existingFunctions) {
		if (existingFunctions != null && existingFunctions.size() > 0) {
			return evolve(existingFunctions);
		} else {
			// random selection
			System.out.println("ImplEvolver evolveFrom initialRandom");
			return initialRandom();
		}
	}
	
	private static List<AbstractIntFunction> initialRandom() {
		
		int numberOfFunctions = numberOfNewFunctions();
		
//		int randomNumber = random.nextInt(intFunctionKeys.size());
//		System.out.println("randomNumber is " + randomNumber);
//		
//		String key = intFunctionKeys.get(randomNumber);
		
		List<AbstractIntFunction> functionsChosen = new ArrayList<AbstractIntFunction>();
		
		functionsChosen = addFunctions(functionsChosen, numberOfFunctions);
		
//		for (int i = 0; i < numberOfFunctions; i++) {
//			int randomNumber = random.nextInt(intFunctionKeys.size());
//			//System.out.println("randomNumber is " + randomNumber);
//			
//			String key = intFunctionKeys.get(randomNumber);
//			try {
//				functionsChosen.add((AbstractIntFunction)availableIntFunctions.get(key).newInstance());
//			} catch (Exception e) {
//				// TODO - handle this more nicely
//				throw new RuntimeException("caught an exception", e);
//			}
//		}
		
		//functionsChosen.add(availableIntFunctions.get(key));
		
		return functionsChosen;
	}
	
	private static List<AbstractIntFunction> evolve(List<AbstractIntFunction> existingFunctions) {
		// first of all - create a new list then copy the functions from existing to the new list
		List<AbstractIntFunction> evolvedFunctions = new ArrayList<AbstractIntFunction>();
		evolvedFunctions.addAll(existingFunctions);
		// delete 0, 1 or 2 functions from the end then add 1 or 2 functions - so we will always have at least one function
		
		// first of all - how many functions can we delete
		// note - we can only delete functions from the end - if we delete intermediate ones we can't be sure the later ones
		// won't depend on the variables the deleted ones returned
		int numFuncsToDelete = numberToDeleteFunctions(evolvedFunctions.size());
		//System.out.println("going to delete " + numFuncsToDelete + " while evolving");
		for (int i = 0; i < numFuncsToDelete; i++) {
			//System.out.println("removing element " + (evolvedFunctions.size() - 1));
			evolvedFunctions.remove(evolvedFunctions.size() - 1);
		}
		
		// now how many functions can we add
		int numFunctsToAdd = numberToAddFunctions();
		//System.out.println("going to add " + numFunctsToAdd + " while evolving");
		evolvedFunctions = addFunctions(evolvedFunctions, numFunctsToAdd);
		
		return evolvedFunctions;
	}
	
	private static int numberOfNewFunctions() {
		// returns 1, 2 or 3
		return 1 + random.nextInt(3);
	}
	
	private static List<AbstractIntFunction> addFunctions(List<AbstractIntFunction> addTo, int numberToAdd) {
		for (int i = 0; i < numberToAdd; i++) {
			int randomNumber = random.nextInt(intFunctionKeys.size());
			//System.out.println("randomNumber is " + randomNumber);
			
			String key = intFunctionKeys.get(randomNumber);
			try {
				addTo.add((AbstractIntFunction)availableIntFunctions.get(key).newInstance());
			} catch (Exception e) {
				// TODO - handle this more nicely
				throw new RuntimeException("caught an exception", e);
			}
		}
		return addTo;
	}
	
	private static int numberToDeleteFunctions(int currentNumberOfFunctions) {
		int maxNumToDelete = 5;
		// returns 0 - maxNum
		int randomNumToDelete = random.nextInt(maxNumToDelete + 1);
		if (randomNumToDelete > currentNumberOfFunctions) {
			return currentNumberOfFunctions;
		} else {
			return randomNumToDelete;
		}
	}
	
	private static int numberToAddFunctions() {
		int maxNumToAdd = 1;
		// returns 1 - maxNum
		return 1 + random.nextInt(maxNumToAdd);
		//return 1;
	}
	
}
