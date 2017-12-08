package au.com.epigai.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import au.com.epigai.generator.functions.AbstractIntFunction;
import au.com.epigai.generator.functions.AbstractStatement;
import au.com.epigai.generator.functions.intimpls.IntFunctionFirstFunction;
import au.com.epigai.generator.functions.intimpls.IntFunctionMultiplyImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionOneImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionSumImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionZeroImpl;

public class ImplEvolver {
	
	private static final Map<String, Class> availableIntFunctions = new HashMap<String, Class>();
	private static final List<String> intFunctionKeys;
	
	private static Random random = new Random();
	
	static {
		availableIntFunctions.put("F", IntFunctionFirstFunction.class);
		availableIntFunctions.put("O", IntFunctionOneImpl.class);
		availableIntFunctions.put("S", IntFunctionSumImpl.class);
		availableIntFunctions.put("M", IntFunctionMultiplyImpl.class);
		availableIntFunctions.put("Z", IntFunctionZeroImpl.class);
		
		intFunctionKeys = new ArrayList<String>(availableIntFunctions.keySet());
	}
	
	public static List<AbstractStatement> evolveFrom(List<AbstractStatement> existingStatements) {
		if (existingStatements != null && existingStatements.size() > 0) {
			return evolve(existingStatements);
		} else {
			// random selection
			System.out.println("ImplEvolver evolveFrom initialRandom");
			return initialRandom();
		}
	}
	
	private static List<AbstractStatement> initialRandom() {
		
		int numberOfFunctions = numberOfNewFunctions();
		
		List<AbstractStatement> functionsChosen = new ArrayList<AbstractStatement>();
		
		functionsChosen = addFunctions(functionsChosen, numberOfFunctions);
		
		return functionsChosen;
	}
	
	private static List<AbstractStatement> evolve(List<AbstractStatement> existingStatements) {
		// first of all - create a new list then copy the functions from existing to the new list
		List<AbstractStatement> evolvedstatements = new ArrayList<AbstractStatement>();
		evolvedstatements.addAll(existingStatements);
		// delete 0, 1 or 2 functions from the end then add 1 or 2 functions - so we will always have at least one function
		
		// first of all - how many functions can we delete
		// note - we can only delete functions from the end - if we delete intermediate ones we can't be sure the later ones
		// won't depend on the variables the deleted ones returned
		int numStmtsToDelete = numberToDeleteFunctions(evolvedstatements.size());
		//System.out.println("going to delete " + numFuncsToDelete + " while evolving");
		for (int i = 0; i < numStmtsToDelete; i++) {
			//System.out.println("removing element " + (evolvedFunctions.size() - 1));
			evolvedstatements.remove(evolvedstatements.size() - 1);
		}
		
		// now how many functions can we add
		int numStmtsToAdd = numberToAddFunctions();
		//System.out.println("going to add " + numFunctsToAdd + " while evolving");
		evolvedstatements = addFunctions(evolvedstatements, numStmtsToAdd);
		
		return evolvedstatements;
	}
	
	private static int numberOfNewFunctions() {
		// returns 1, 2 or 3
		return 1 + random.nextInt(3);
	}
	
	private static List<AbstractStatement> addFunctions(List<AbstractStatement> addTo, int numberToAdd) {
		// TODO get it to add stuff other than int functions
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
		int maxNumToAdd = 3;
		// returns 1 - maxNum
		return 1 + random.nextInt(maxNumToAdd);
		//return 1;
	}
	
}
