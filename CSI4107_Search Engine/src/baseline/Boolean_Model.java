import java.util.*;

public class Boolean_Model {

	DictionaryBuilder db;
	private ArrayList<String> docID_Res = new ArrayList<String>();
	
	Boolean_Model(boolean stopword, boolean normalization, boolean stemming) {

		db = new DictionaryBuilder(stopword, normalization, stemming);

	}

	/* Sites used: 
	 * http://interactivepython.org/runestone/static/pythonds/BasicDS/InfixPrefixandPostfixExpressions.html
	 * https://www.geeksforgeeks.org/stack-set-2-infix-to-postfix/
	 * Modified/inspired by these sites to translate a string from infix to postfix
	 */
	public String infixToPostfix(String exp) {

		// initializing empty String for result
		ArrayList<String> res = new ArrayList<String>();
		String tokenList[] = exp.split(" ");

		// initializing empty stack
		Stack<String> stack = new Stack<>();

		for (int i = 0; i < tokenList.length; ++i) {
			// If the scanned character is an operand, add it to output.
			if (isOperand(tokenList[i])) {
				res.add(tokenList[i]);
			}

			// If the scanned character is an '(', push it to the stack.
			else if (tokenList[i].equals("(")) {
				stack.push(tokenList[i]);
			}

			// If the scanned character is an ')', pop and output from the stack
			// until an '(' is encountered.
			else if (tokenList[i].equals(")")) {
				while (!stack.isEmpty() && !stack.peek().equals("("))
					res.add(stack.pop());

				if (!stack.isEmpty() && !stack.peek().equals("("))
					return "Invalid Expression";
				else
					stack.pop();
			} else // an operator is encountered
			{
				while (!stack.isEmpty() && Prec(tokenList[i]) <= Prec(stack.peek()))
					res.add(stack.pop());
				stack.push(tokenList[i]);
			}
		}

		// pop all the operators from the stack
		while (!stack.isEmpty())
			res.add(stack.pop());

		return String.join(" ", res);
	} // end of infixToPostfix function

	/* Site used: https://www.geeksforgeeks.org/stack-set-2-infix-to-postfix/
	 * Modified precedence of operators function to return boolean instead of math operators
	 */
	private int Prec(String ch) { 
		switch (ch) 
		{ 
		case "OR": 

			return 1; 

		case "AND": 

			return 2; 

		case "AND_NOT": 
			return 3; 
		} 
		return -1; 
	} 

	/* 
	 * Site used: http://interactivepython.org/runestone/static/pythonds/BasicDS/InfixPrefixandPostfixExpressions.html
	 * Modified/inspired by this site to evaluate a postfix expression of a string
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> postfixEval(String postfixExpr) {
		Stack<Object> operandStack = new Stack<Object>();
		String tokenList[] = postfixExpr.split(" ");
		ArrayList<String> docID_Res = new ArrayList<String>();

		for (String token: tokenList) {

			//if word (not operand), push in stack
			if (isOperand(token)) { 
				operandStack.push(token);
			}

			else {

				//STRING - operand 2
				if (operandStack.peek() instanceof String) {
					String operand2 = (String) operandStack.pop();
					ArrayList<String> op2List = getList(operand2);

					//STRING - operand 1
					if (operandStack.peek() instanceof String) {
						String operand1 = (String) operandStack.pop();
						ArrayList<String> op1List = getList(operand1);

						docID_Res = performBooleanOperation(token, op1List, op2List);
						operandStack.push(docID_Res);
					}

					//ArrayList - operand 1
					else {
						ArrayList<String> op1List = (ArrayList<String>) operandStack.pop();

						docID_Res = performBooleanOperation(token, op1List, op2List);
						operandStack.push(docID_Res);
					}

				}

				//ArrayList - operand 2
				else {
					ArrayList<String> op2List = (ArrayList<String>) operandStack.pop();

					//STRING - operand 1
					if (operandStack.peek() instanceof String) {
						String operand1 = (String) operandStack.pop();
						ArrayList<String> op1List = getList(operand1);

						docID_Res = performBooleanOperation(token, op1List, op2List);
						operandStack.push(docID_Res);
					}

					//ArrayList - operand 1
					else {
						ArrayList<String> op1List = (ArrayList<String>) operandStack.pop();

						docID_Res = performBooleanOperation(token, op1List, op2List);
						operandStack.push(docID_Res);
					}
				}

			}
		}
		//return la liste de docIDs
		return docID_Res;
	} //end of postfixEval function


	// Check if operand a boolean operator or ( ) or not
	public boolean isOperand(String name) {

		if (!name.equals("AND") && !name.equals("AND_NOT") && !name.equals("OR") && !name.equals("(") && !name.equals(")")) {
			return true;
		}
		return false;
	} 


	/* Site used: http://interactivepython.org/runestone/static/pythonds/BasicDS/InfixPrefixandPostfixExpressions.html
	 * Modified/inspired the doMath function of this site to evaluate with boolean operators instead of math
	 */
	private ArrayList<String> performBooleanOperation(String op, ArrayList<String> p1, ArrayList<String>  p2) {
		if (op.equals("OR")) { 
			return union(p1, p2); }
		else if (op.equals("AND")) { 
			return intersection(p1, p2); }
		else {
			return andNot(p1, p2); }
	} 


	/*
	 * Return a merged list of docIDs using AND
	 */
	protected ArrayList<String> intersection(ArrayList<String> p1, ArrayList<String> p2) {

		Collections.sort(p1);
		Collections.sort(p2);
		
		ArrayList<String> answer = new ArrayList<String>();

		int ptr1 = 0;
		int ptr2 = 0;

		while ( ptr1 != p1.size() && ptr2 != p2.size() ) {

			if (Integer.parseInt(p1.get(ptr1)) < Integer.parseInt(p2.get(ptr2))) {
				ptr1++;

			} else if (Integer.parseInt(p2.get(ptr2)) < Integer.parseInt(p1.get(ptr1))) {
				ptr2++;

			} else {
				answer.add(p1.get(ptr1));
				ptr1++;
				ptr2++;
			}

		}
		return answer;
	}


	/*
	 * Return a merged list of docIDs using OR
	 */
	private ArrayList<String> union(ArrayList<String> p1, ArrayList<String> p2) {

		Collections.sort(p1);
		Collections.sort(p2);

		ArrayList<String> answer = new ArrayList<String>();

		for (int ptr1 = 0; ptr1 < p1.size(); ptr1++) {
			answer.add(p1.get(ptr1));
		}

		for (int ptr2 = 0; ptr2 < p2.size(); ptr2++) {

			if (! answer.contains(p2.get(ptr2))) {
				answer.add(p2.get(ptr2));
			}
		}

		return answer;
	}

	/*
	 * Return a merged list of docIDs using AND NOT
	 */
	private ArrayList<String> andNot(ArrayList<String> p1, ArrayList<String> p2) {

		Collections.sort(p1);
		Collections.sort(p2);

		//tous les éléments dans p1 qui ne sont pas dans p2
		for (int i = 0; i < p2.size(); i++) {

			//if l'élément i de p1 est dans p2, supprime l'élément de p1
			if(p1.contains(p2.get(i))){
				p1.remove(p2.get(i));
			}
		}
		return p1;
	} //end of andNot function

	/*
	 * Get list of docIDs for an operand
	 * ex: operand = word retourne les docIDs [4106, 5155, 5180, 5183, 5185, 5387, 5390]
	 */
	private ArrayList<String> getList(String operand){
		
		ArrayList<String> emptyList = new ArrayList<String>();

		if (!db.keytodocID.containsKey(operand)) { 
			return emptyList; 
		}
		else { return db.keytodocID.get(operand); }
	}
	
	// Get the resulting docIDs
	public ArrayList<String> getdocIDs() {
		return docID_Res;
	}


	
	/*
	 * Returns a list of docIDs for 1-word query
	 * Uses inverted index to obtain the docIDs
	 */
	public ArrayList<String> singleWordQuery(String word) {

		ArrayList<String> answer = new ArrayList<String>();

		if (! word.contains("AND") && ! word.contains("OR") && ! word.contains("AND_NOT") && db.keytodocID.containsKey(word)) {
			return answer = db.keytodocID.get(word);
		}
		return answer;
	}
	
	/* Site used: http://www.java67.com/2016/09/3-ways-to-count-words-in-java-string.html
	 * Used/modified "Solution 1 - Counting word using String.split() method" from this site
	 */
	public int totalNumWord(String sentence) { 
		if (sentence == null || sentence.isEmpty()) { 
			return 0; //no word
		} 
		String[] words = sentence.split("\\s+"); 
		int tot = words.length;
		return tot; //total num of words in phrase
	} 
	
	/** Optional Module - Wildcard management---------------------------------------------------------------------------- */

	/* Site used: https://stackoverflow.com/questions/5238491/check-if-string-contains-only-letters
	 * Modified snippet of 1st solution in Stackoverflow to check if a string contains letters or boolean operators
	 */
	public boolean isWildcard(String name) {

		if (name.matches("[a-zA-Z*]+") && !name.equals("AND") && !name.equals("AND_NOT") && !name.equals("OR")) {
			return true;
		}
		return false;
	} 

	/*
	 * Converts the wildcard query to infix format with all possibilities of the terms
	 */
	public String wildcardToInfixFormat(String exp) { 
		String res = "";
		String tokenList[] = exp.split(" ");

		//one word
		if (tokenList.length == 1) {
			res = wildcardOptions(exp);
		}

		//mutli-word with at least one boolean operator
		for (int i = 0; i<tokenList.length; i++) {
			if (tokenList[i].contains("*")) {

				if (i == 0) res = wildcardOptions(tokenList[i]);
				else if (i >= 1) res = res + " ( " + wildcardOptions(tokenList[i]) + " )";

			}

			else {
				res = res + " " + tokenList[i];
			}
		}

		return res;
	}

	/*
	 * Returns the resulting string of the wildcard based on location of the * in the word
	 */
	private String wildcardOptions(String wildcard) {
		String res = "";
		String wordExcludingWildcard = "";
		String firstChar = wildcard.substring(0, 1);
		String lastChar = wildcard.substring(wildcard.length() - 1);
		ArrayList<String> wordsetList = db.getDictionary();

		if (firstChar.equals("*"))
			wordExcludingWildcard = wildcard.substring(1, wildcard.length());
		if (lastChar.equals("*"))
			wordExcludingWildcard = wildcard.substring(0, wildcard.length() - 1);

		for (int i = 0; i < wordsetList.size(); i++) {
			String word = wordsetList.get(i); // word from dictionary

			// wildcard at beginning of a word (ex: *pad)
			if (firstChar.equals("*")) {
				if (word.indexOf(wordExcludingWildcard) != -1) {

					if (word.substring(word.length() - wordExcludingWildcard.length(), word.length())
							.equals(wordExcludingWildcard)) {

						if (res.equals(""))
							res = word;
						else if (!res.contains("OR"))
							res = res + " OR " + word;
						else
							res = "( " + res + " )" + " OR " + word;
					}
				}
			}

			// wildcard at end of a word (ex: timeeeee*)
			else if (lastChar.equals("*")) {
				// if wildcard in word
				if (word.indexOf(wordExcludingWildcard) != -1) {

					if (word.substring(0, wordExcludingWildcard.length()).equals(wordExcludingWildcard)) {

						if (res.equals(""))
							res = word;
						else if (!res.contains("OR"))
							res = res + " OR " + word;
						else
							res = "( " + res + " )" + " OR " + word;
					}
				}
			}

			// wildcard in middle of a word
			else {
				String firstPart = wildcard.substring(0, wildcard.indexOf("*"));
				String secPart = wildcard.substring(wildcard.indexOf("*") + 1, wildcard.length());
				Boolean firstPartSatisfied, lastPartSatisfied;

				if ((word.indexOf(firstPart) != -1) && (word.indexOf(secPart) != -1)) {

					firstPartSatisfied = word.substring(0, firstPart.length()).equals(firstPart);
					lastPartSatisfied = word.substring(word.length() - secPart.length(), word.length()).equals(secPart);

					if (firstPartSatisfied && lastPartSatisfied) {
						if (res.equals(""))
							res = word;
						else if (!res.contains("OR"))
							res = res + " OR " + word;
						else
							res = "( " + res + " )" + " OR " + word;
					}
				}
			}
		}
		return res;
	}
	
}
