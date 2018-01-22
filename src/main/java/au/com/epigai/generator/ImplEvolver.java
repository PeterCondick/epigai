package au.com.epigai.generator;

import java.lang.reflect.Constructor;
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
import au.com.epigai.generator.functions.conditions.AbstractBooleanCondition;
import au.com.epigai.generator.functions.conditions.BooleanConditionIntEquals;
import au.com.epigai.generator.functions.conditions.BooleanConditionIntGreaterThan;
import au.com.epigai.generator.functions.conditions.BooleanConditionTrue;
import au.com.epigai.generator.functions.flowimpls.FlowControlIf;
import au.com.epigai.generator.functions.flowimpls.FlowControlReturn;
import au.com.epigai.generator.functions.intimpls.IntFunctionDivideImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionFirstFunction;
import au.com.epigai.generator.functions.intimpls.IntFunctionMultiplyImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionOneImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionSumImpl;
import au.com.epigai.generator.functions.intimpls.IntFunctionZeroImpl;

public class ImplEvolver {
	
	private static final Map<String, Class> availableIntFunctions = new HashMap<String, Class>();
	private static final List<String> intFunctionKeys;
	
	private static final Map<String, Class> availableFlowControls = new HashMap<String, Class>();
	private static final List<String> flowControlKeys;
	
	private static final Map<String, Class> availableBooleanConditions = new HashMap<String, Class>();
	private static final List<String> booleanConditionKeys;
	
	private static Random random = new Random();
	
	static {
		// int functions
		availableIntFunctions.put("F", IntFunctionFirstFunction.class);
		availableIntFunctions.put("O", IntFunctionOneImpl.class);
		availableIntFunctions.put("S", IntFunctionSumImpl.class);
		availableIntFunctions.put("M", IntFunctionMultiplyImpl.class);
		availableIntFunctions.put("Z", IntFunctionZeroImpl.class);
		availableIntFunctions.put("D", IntFunctionDivideImpl.class);
		
		intFunctionKeys = new ArrayList<String>(availableIntFunctions.keySet());
		
		// flow control
		availableFlowControls.put("R", FlowControlReturn.class);
		availableFlowControls.put("I", FlowControlIf.class);
		
		flowControlKeys = new ArrayList<String>(availableFlowControls.keySet());
		
		// boolean conditions
		//availableBooleanConditions.put("T", BooleanConditionTrue.class);
		availableBooleanConditions.put("I", BooleanConditionIntEquals.class);
		availableBooleanConditions.put("G", BooleanConditionIntGreaterThan.class);
		
		booleanConditionKeys = new ArrayList<String>(availableBooleanConditions.keySet());
	}
	
	public static CodeBlock evolveFrom(CodeBlock existingCodeBlock, String returnType) {
		// TODO - this current impl allows an implementation to only have a if statement - the method 
		// impl then does not have a value to return so it defaults to returning 0
		// should find a nice way to cope with that
		if (existingCodeBlock != null && 
				existingCodeBlock.getStatements() != null && 
				existingCodeBlock.getLineCount() > 0) {
			return evolve(existingCodeBlock, returnType);
		} else {
			// random selection
			System.out.println("ImplEvolver evolveFrom initialRandom");
			return initialRandom(returnType);
		}
	}
	
	private static CodeBlock initialRandom(String returnType) {
		
		int numberOfFunctions = numberOfNewFunctions();
		
		List<AbstractStatement> functionsChosen = new ArrayList<AbstractStatement>();
		CodeBlock codeBlock = new CodeBlock();
		
		functionsChosen = addFunctions(functionsChosen, numberOfFunctions, true, codeBlock, returnType);
		
		// add a return statement
		// TODO - do this properly once flow controls are implemented properly
		try {
			Constructor fcrConstructor = availableFlowControls.get("R").getConstructor();
			functionsChosen.add((AbstractStatement)fcrConstructor.newInstance());
		} catch (Exception e) {
			// TODO - handle this more nicely
			throw new RuntimeException("caught an exception", e);
		}
		
		codeBlock.setStatements(functionsChosen);
		return codeBlock;
	}
	
	private static CodeBlock initialNestedBlock(CodeBlock parentBlock) {
		
		int numberOfFunctions = numberOfNewFunctions();
		
		List<AbstractStatement> functionsChosen = new ArrayList<AbstractStatement>();
		CodeBlock codeBlock = new CodeBlock();
		codeBlock.setParentBlock(parentBlock);
		functionsChosen = addFunctions(functionsChosen, numberOfFunctions, false, codeBlock, null);
		
		codeBlock.setStatements(functionsChosen);
		return codeBlock;
	}
	
	private static CodeBlock evolve(CodeBlock codeBlock, String returnType) {
		CodeBlock newCodeBlock = new CodeBlock();
		
		// first of all - create a new list then copy the functions from existing to the new list
		List<AbstractStatement> evolvedstatements = new ArrayList<AbstractStatement>();
		evolvedstatements.addAll(codeBlock.getStatements());
		// delete 0, 1 or 2 functions from the end then add 1 or 2 functions - so we will always have at least one function
		
		// always remove the return statement
		if (evolvedstatements.get(evolvedstatements.size() - 1) instanceof FlowControlReturn) {
			evolvedstatements.remove(evolvedstatements.size() - 1);
		}
		
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
		boolean allowFlows = false;
		if (codeBlock.isTopLevelParent()) {
			allowFlows = true;
		}
		evolvedstatements = addFunctions(evolvedstatements, numStmtsToAdd, allowFlows, newCodeBlock, returnType);
		
		// add a return statement - we always delete at least 1 function - so the returns is always deleted
		// TODO - do this properly once flow controls are implemented properly
		try {
			Constructor fcrConstructor = availableFlowControls.get("R").getConstructor();
			evolvedstatements.add((AbstractStatement)fcrConstructor.newInstance());
		} catch (Exception e) {
			// TODO - handle this more nicely
			throw new RuntimeException("caught an exception", e);
		}
		
		newCodeBlock.setStatements(evolvedstatements);
		return newCodeBlock;
	}
	
	private static int numberOfNewFunctions() {
		// returns 1, 2 or 3
		return 1 + random.nextInt(3);
	}
	
	private static List<AbstractStatement> addFunctions(List<AbstractStatement> addTo, int numberToAdd, boolean allowFlowControl, CodeBlock thisBlock, String mustDeclareVariableOfType) {
		
		// firstly what type of statement will the next statement be?
		// only IntFunctions or FlowControl so far
		int iStart = 0;
		if (mustDeclareVariableOfType != null && addTo.isEmpty()) {
			iStart = 1;
			// TODO - make this cope with types other than int
			if (mustDeclareVariableOfType.equals(int.class.getName())) {
				addAnIntFunction(addTo);
			} else {
				throw new RuntimeException("can't declare a variable of type " + mustDeclareVariableOfType + " not yet implemented");
			}
		} // else if it's not empty we can assume the first statement is already declaring a variable of the correct type
		
		for (int i = iStart; i < numberToAdd; i++) {
			int whatTypeStmtsNum = 3;
			if (allowFlowControl) {
				whatTypeStmtsNum = 4;
			}
			int whatTypeStmtInt = random.nextInt(whatTypeStmtsNum);
			
			if (whatTypeStmtInt < 3) {
				// add a int function
				addAnIntFunction(addTo);
//				int randomNumber = random.nextInt(intFunctionKeys.size());
//				//System.out.println("randomNumber is " + randomNumber);
//				
//				String key = intFunctionKeys.get(randomNumber);
//				try {
//					Constructor constructor = availableIntFunctions.get(key).getConstructor();
//					addTo.add((AbstractStatement)constructor.newInstance());
//					//addTo.add((AbstractIntFunction)availableIntFunctions.get(key).newInstance());
//				} catch (Exception e) {
//					// TODO - handle this more nicely
//					throw new RuntimeException("caught an exception", e);
//				}
			} else {
				//System.out.println(whatTypeStmtInt + " so adding a flow control");
				// add a flow control
				// hard coding to a if flow control for now
				String key = "I";
				try {
					Constructor fcConstructor = availableFlowControls.get(key).getConstructor();
					FlowControlIf fcIf = (FlowControlIf)fcConstructor.newInstance();
					
					// add the boolean condition
					int randomNumberBoolCond = random.nextInt(booleanConditionKeys.size());
					
					String bcKey = booleanConditionKeys.get(randomNumberBoolCond);
					try {
						Constructor bcConstructor = availableBooleanConditions.get(bcKey).getConstructor();
						fcIf.setBooleanCondition((AbstractBooleanCondition)bcConstructor.newInstance());
						
					} catch (Exception e) {
						// TODO - handle this more nicely
						throw new RuntimeException("caught an exception", e);
					}
					
					// add the codeBlock
					CodeBlock ifBlock = initialNestedBlock(thisBlock);
					//ifBlock.setParentBlock(thisBlock);
					fcIf.setCodeBlock(ifBlock);
					
					addTo.add(fcIf);
				} catch (Exception e) {
					// TODO - handle this more nicely
					throw new RuntimeException("caught an exception", e);
				}
				
				
			}
		}
		return addTo;
	}
	
	private static void addAnIntFunction(List<AbstractStatement> addTo) {
		int randomNumber = random.nextInt(intFunctionKeys.size());
		
		String key = intFunctionKeys.get(randomNumber);
		try {
			Constructor constructor = availableIntFunctions.get(key).getConstructor();
			addTo.add((AbstractStatement)constructor.newInstance());
			//addTo.add((AbstractIntFunction)availableIntFunctions.get(key).newInstance());
		} catch (Exception e) {
			// TODO - handle this more nicely
			throw new RuntimeException("caught an exception", e);
		}
	}
	
	private static int numberToDeleteFunctions(int currentNumberOfFunctions) {
		int maxNumToDelete = 5;
		// returns 0 - maxNum
		int randomNumToDelete = random.nextInt(maxNumToDelete + 1);
		// always deleting at least one function so we are deleting the returns function
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
	}
	
}
