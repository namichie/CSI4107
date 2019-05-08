import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.TreeSet;

import opennlp.tools.ngram.NGramGenerator;


/*
 * 1. Create bigrams from Reuters collection
 * 2. From 1st term in bigram, find all 2nd terms for that 1st term that appeared most frequently
 * 3. Pass those 2nd terms to query completion for the given 1st term
 */


public class BigramModel {

	DictionaryBuilder db;
	ArrayList<ArrayList<String>> tokenList;
	ArrayList<String> text, bigram, firstTerm, secondTerm; 
	HashMap<String, Integer> suggestions; // List of suggested words and its frequency


	public BigramModel(DictionaryBuilder db) {
		this.db = db;
		tokenList = db.gettokenList();
		text = new ArrayList<String>();
		bigram = new ArrayList<String>();
		firstTerm = new ArrayList<String>();
		secondTerm = new ArrayList<String>();
		suggestions = new HashMap<String, Integer>();
		this.createBigram();
		this.getIndividualTerm(bigram);

	}

	//Site used: https://stackoverflow.com/questions/29656071/java-arraylist-remove-multiple-element-by-index
	// Modified the LAST solution to remove empty strings from bigram list
	// Create bigram from Reuters tokenized text
	private void createBigram() {

		NGramGenerator n = new NGramGenerator();

		// Combine all arraylists of tokens into one
		for (int i = 0; i < tokenList.size(); i++) {
			text.addAll(tokenList.get(i));
		}

		// Create list of bigrams
		bigram = (ArrayList<String>) NGramGenerator.generate(text, 2, " ");

		ArrayList<Integer> toRemove = new ArrayList<Integer>();

		String token = "";

		for (int i = 0; i < bigram.size(); i++) {
			token = bigram.get(i);

			// Find empty string, single words to remove from bigram
			if (token.matches("\\s+") || token.startsWith(" ") || token.endsWith(" ") || totalNumWord(token) == 1) { 				
				toRemove.add(i);
			} 
		}

		// Sort list of indices
		TreeSet<Integer> sortIndex = new TreeSet<Integer>();
		sortIndex.addAll(toRemove);

		// Iterator to traverse backwards from end of list
		ListIterator<Integer> iter = toRemove.listIterator(toRemove.size());

		// Remove specified indices from bigram 
		while (iter.hasPrevious()) {
			int index = iter.previous();
			bigram.remove(index);
		}
	}

	public ArrayList<String> getBigram() {
		return bigram;
	}

	// Get 1st and 2nd terms separately from bigram
	private void getIndividualTerm(ArrayList<String> bigram)  {

		String[] arr = null;

		String first = "";
		String second = "";

		for (String s : bigram) {

			arr = s.split(" ");
			first = arr[0];
			second = arr[1];
			firstTerm.add(first);
			secondTerm.add(second);

		}
	}


	// term (suggested word from 2ndterm) : freq
	public ArrayList<String> suggest(String queryWord) {

		String term = "";
		String[] arr = null;

		ArrayList<HashMap<String, Integer>> result = new ArrayList<HashMap<String, Integer>>();

		for (int i = 0; i < bigram.size(); i++) {

			arr = bigram.get(i).split(" ");

			//bigram term == first term in bigram
			if (arr[0].equals(firstTerm.get(i)) && firstTerm.get(i).equals(queryWord)) {
				term = secondTerm.get(i);

				if (suggestions.containsKey(term)) {
					int updateFreq = suggestions.get(term) + 1;
					suggestions.replace(term, updateFreq);
				} else {
					suggestions.put(term, 1);
				}

			}

		}

		//System.out.println("suggested: " + suggestions);

		result = sortByValue(suggestions);

		ArrayList<String> res = getDocIDs(result);
		//System.out.println("res: " + result);

		return res;
	}

	// term (suggested word from 2ndterm) : freq
	public String[] suggest_UI(String queryWord) {

		String term = "";
		String[] arr = null;

		ArrayList<HashMap<String, Integer>> result = new ArrayList<HashMap<String, Integer>>();

		for (int i = 0; i < bigram.size(); i++) {

			arr = bigram.get(i).split(" ");

			//bigram term == first term in bigram
			if (arr[0].equals(firstTerm.get(i)) && firstTerm.get(i).equals(queryWord)) {
				term = secondTerm.get(i);

				if (suggestions.containsKey(term)) {
					int updateFreq = suggestions.get(term) + 1;
					suggestions.replace(term, updateFreq);
				} else {
					suggestions.put(term, 1);
				}

			}

		}

		//System.out.println("suggested: " + suggestions);

		result = sortByValue(suggestions);

		String[] res = getDocIDs_UI(result);
		//System.out.println("res: " + result);

		return res;
	}

	public ArrayList<String> getFirstTerm() {
		return firstTerm;
	}

	public ArrayList<String> getSecondTerm() {
		return secondTerm;
	}

	public HashMap<String, Integer> getSuggestions() {
		return suggestions;
	}

	//count total number of words in phrase
	//Code copied from:
	//http://www.java67.com/2016/09/3-ways-to-count-words-in-java-string.html
	public int totalNumWord(String sentence) { 
		if (sentence == null || sentence.isEmpty()) { 
			return 0; //no word
		} 
		String[] words = sentence.split("\\s+"); 
		int tot = words.length;
		return tot; //total num of words in phrase
	} //end of countWordsUsingSplit function


	//sort the HashMap in descending order (highest to lowest score)
	public ArrayList<HashMap<String, Integer>> sortByValue(HashMap<String, Integer> scoreToDocID) { 

		//result in descending order
		ArrayList<HashMap<String, Integer>> result = new ArrayList<HashMap<String, Integer>>();

		//tmp HashMap to insert in the result ArrayList
		HashMap<String, Integer> tmp = new HashMap<String, Integer>();

		//put all the scores from HashMap args into an primitive array
		Integer[] score = scoreToDocID.values().toArray(new Integer[0]);

		//put all the scores from args into an arrayList
		//an arrayList allows us to call a function to sort all the elements
		ArrayList<Integer> scores = new ArrayList<Integer>();

		//key, which is the docID
		String key;

		//go through all the scores of the primitive array and add it to the arrayList
		for (int i = 0; i <score.length; i++) {
			scores.add(score[i]);
		}

		//arrayList can sort all the scores
		Collections.sort(scores);

		//descending order
		Collections.reverse(scores);

		for (int j = 0; j < scores.size(); j++) {
			//get key associated to the score
			key = getKeyFromValue(scoreToDocID, scores.get(j)).toString();
			//System.out.println(scores.get(j) + " ; getKey: " + key);

			//remove key from args
			//If there are duplicate values, we want to get all the different docID 
			scoreToDocID.remove(key);

			//reinitialise HashMap to nothing
			tmp = new HashMap<String, Integer>();

			//add it to the result arrayList in descending order
			//because we sorted in descending order the arrayList scores
			tmp.put(key, scores.get(j));
			result.add(j, tmp);	
		}

		return result;
	} 


	// return key from value from
	// Code token from this website: 
	//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	public static Object getKeyFromValue(HashMap hm, Object value) {

		//for each key in the hashMap
		for (Object object : hm.keySet()) {

			//if value from this key is equal to the value object from the args
			if (hm.get(object).equals(value)) {

				//return key
				return object;
			}
		}

		//if not found, return null
		return null;
	}

	// get docID (key) in arrayList from the HashMap
	// Code modified from this website: 
	//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	public ArrayList<String> getDocIDs(ArrayList<HashMap<String, Integer>> scoretoDocID) {
		ArrayList<String> docIDs = new ArrayList<String>();
		HashMap<String, Integer> hm;

		for (int i = 0; i < scoretoDocID.size(); i++) {

			// get hashMap at index i
			hm = scoretoDocID.get(i);

			// get key from hashMap
			for (String key : hm.keySet()) {
				docIDs.add(key);
			}
		}
		return docIDs;
	}
	
	
	// get docID (key) in arrayList from the HashMap
	// Code modified from this website: 
	//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	public String[] getDocIDs_UI(ArrayList<HashMap<String, Integer>> scoretoDocID) {

		HashMap<String, Integer> hm;
		String[] res = new String[scoretoDocID.size()];

		for (int i = 0; i < scoretoDocID.size(); i++) {

			// get hashMap at index i
			hm = scoretoDocID.get(i);

			// get key from hashMap
			for (String key : hm.keySet()) {
				res[i] = key;
			}
		}
		return res;
	}

	public static void main(String[] args) {
		DictionaryBuilder db = new DictionaryBuilder(true, true, false, 'r', "kNN");

		BigramModel b = new BigramModel(db);
		ArrayList<String> bb = b.getBigram();		
		System.out.println(b.suggest("year"));
		System.out.println(b.suggest_UI("year")[0]);
		
		
	}
}
