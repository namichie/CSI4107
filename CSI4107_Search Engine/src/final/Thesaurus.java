import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Thesaurus {

	/*
	 * Module 9a - Automatic Thesaurus Construction
	 * Purpose: Build a thesaurus from the Reuters Collection.
	 * Input: Reuters Collection. Dictionary of Terms.
	 * Output: A structure that captures the similarity between all pairs of terms.

	 * Similarity measure: Jaccard
	 * Unit of comparison: by document
	 */

	DictionaryBuilder db;
	HashMap<Set, Double> t;

	protected File inputFile;

	HashMap<String, ArrayList<String>> keytodocID = new HashMap<String, ArrayList<String>>();
	HashMap<String, ArrayList<String>> invertedIndex;
	ArrayList<String> dictionary;
	ArrayList<String> uniqueTokens = new ArrayList<String>();


	Thesaurus(DictionaryBuilder db) {
		this.db = db;
		dictionary = new ArrayList<String>();
		uniqueTokens = db.getExternalDictionary_reuter();
		invertedIndex = new HashMap<String, ArrayList<String>>();
		keytodocID = db.getInvertedIndex();
		this.t = build_Automatic_Thesaurus();
	}

	/*
	 * Purpose: Build a thesaurus from the Reuters Collection.
	 * Input: Reuters Collection. Dictionary of Terms.
	 * Output: A structure that captures the similarity between all pairs of terms.
	 * 
	 * Similarity measure: Jaccard
	 * Unit of comparison: by document
	 * Find the similarity of each tokens in each documents, like seen in class, shown below
	 * 	
	 * 			D1	D2	D3	D4....				token1 token2 token3...
	 * token 1[					]		token 1[					]
	 * token 2[					]  => 	token 2[					]
	 * token 3[					]		token 3[					]
	 * 
	 */	
	public HashMap<Set, Double> build_Automatic_Thesaurus() {
		// key: set of the 2 documents ; value: similarity of the 2 documents in the set
		HashMap<Set, Double> thesaurus = new HashMap<Set, Double>();

		//ArrayList<String> uniqueTokens = getDictionary(); // list of unique tokens
		int tot_tokens = uniqueTokens.size(); // total number of unique tokens

		Set<String> tokenSet; // set of 2 words
		double similarity; // similarity between the 2 words in the set
		String tokenA, tokenB;

		//find the similarity between each words in each document
		for (int i=0; i<tot_tokens; i++) {
			tokenA = uniqueTokens.get(i);

			for (int j=0; j<tot_tokens; j++) {
				tokenB = uniqueTokens.get(j);
				tokenSet = new HashSet<String>(); //reinitialize the set before adding it in the thesaurus
				tokenSet.add(tokenA); // add word at position i
				tokenSet.add(tokenB); // add word at position j

				//If the docset exists in thesaurus, which contains 2 docA and docB, then we already calculatd the similarity between doc i and doc j
				if (!thesaurus.containsKey(tokenSet)) {

					//IF token i == token j, THEN similarity = 1
					if (tokenA.equals(tokenB)) {
						thesaurus.put(tokenSet, 1.0);
					} 

					//ELSE Jaccard: |A and B|/|A or B| = |DOCi and DOCj|/|DOCi or DOCj|
					else {
						similarity = Jaccard_Similarity(tokenA, tokenB); // calculate the similarity
						thesaurus.put(tokenSet, similarity); //add to thesaurus
					} //end of else

				} //end of of statement

			} //end of foor loop j

		} //end of foor loop i

		//System.out.println(thesaurus);
		return thesaurus;
	}


	/*
	 * Find the similarity between the 2 tokens, tokenA and tokenB
	 * Jaccard: | token A and token B|/|token A or token B|
	 * Code modified from: https://stackoverflow.com/questions/51113134/union-and-intersection-of-java-sets
	 */
	public double Jaccard_Similarity(String tokenA, String tokenB) {

		//****Besoin d'appeler getInvertedIndex() dans le constructeur
		//HashMap<String, ArrayList<String>> keytodocID = getInvertedIndex(); // get inverted index

		// find the list of documents corresponding to token A
		ArrayList<String> docIDA = keytodocID.get(tokenA);

		// find the list of documents corresponding to token B
		ArrayList<String> docIDB = keytodocID.get(tokenB);

		Set<String> tokenSet_docA = new HashSet<String>(); // all docID corresponding to token A
		Set<String> tokenSet_docB = new HashSet<String>(); // all docID corresponding to token A
		Set<String> intersection, union; // intersection and union sets


		// put all docID containing tokenA in a set, this will eliminate the duplicates
		for (int i=0; i<docIDA.size(); i++) {
			tokenSet_docA.add(docIDA.get(i));
		}

		// put all docID containing tokenB in a set, this will eliminate the duplicates
		for (int i=0; i<docIDB.size(); i++) {
			tokenSet_docB.add(docIDB.get(i));
		}


		// determine intersection set
		intersection = new TreeSet<String>(tokenSet_docA);
		intersection.retainAll(tokenSet_docB);	

		// determine union set
		union = new TreeSet<String>(tokenSet_docA);
		union.addAll(tokenSet_docB);	

		//Calculate Jaccard similarity
		double similarity = (double) intersection.size()/(double) union.size();

		return similarity;

	}


	//find the most similar word with the parameter of this function
	public String getMaxSimilarity(String word) {
		String res = "";
		String tmp;
		double sim = -1000.0;
		//ArrayList<String> uniqueTokens = getDictionary(); // list of unique tokens

		//loop trough all the distinct words
		for (int i=0; i<uniqueTokens.size(); i++) {

			//word at position i
			tmp = uniqueTokens.get(i); //all external tokens

			Set<String> hmKey = new HashSet<String>();
			hmKey.add(word);
			hmKey.add(tmp);

			if (t.containsKey(hmKey) && !word.equals(tmp) && ! tmp.equals("")) {
				//value of key is greater then current similarity

				if (sim < t.get(hmKey)) {
					sim = t.get(hmKey); //update similarity
					res = tmp; //most similar token to word
				}

			}
		}
		return res;
	}


	public String[][] get15Results(String selectedWord){
		ArrayList<HashMap<String, Double>> sort = new ArrayList<HashMap<String, Double>>();
		HashMap<String, Double> top15 = getTopFifteen(selectedWord);

		sort = sortByValue(top15);

		ArrayList<String> words = getDocIDs(sort);
		ArrayList<Double> similarity = getScore(sort);

		String[][] res = new String[words.size()][2];

		for (int i = 0; i<words.size(); i++) {

			res[i][0] = words.get(i); //courseID
			res[i][1] = similarity.get(i).toString();
		}

		return res;
	}




	public HashMap<String, Double> getTopFifteen(String selectedWord) {

		HashMap<String, Double> top15 = new HashMap<String, Double>();
		ArrayList<String> zeroValues = new ArrayList<String>();

		int counter = 0;
		double value;
		String key;

		for (Set<?> setKey : t.keySet()) {

			Set<String> dup = new HashSet<String>();
			dup.addAll((Collection<? extends String>) setKey);

			//if selected word in set
			if(dup.contains(selectedWord)) {

				dup.remove(selectedWord); //remove selected word 

				if (!dup.isEmpty()) {
					key = dup.stream().findFirst().get().toString();


					value = t.get(setKey);

					if(counter<16) { //keep adding in the hashMap
						if (value == 0.0) {
							zeroValues.add(key);
						}

						top15.put(key, t.get(setKey));
						counter++;

					} else { //remove least similar, add another

						if (value > 0.0) { //similarity greater than 0
							if(zeroValues.size()>0) { //similarity of zero
								String tmp = zeroValues.get(0);
								top15.remove(tmp); //remove zero similarity
								zeroValues.remove(tmp); //remove at position 0
								top15.put(key, value); //add new non-zero similarity set
							} else { //nothing that have similarity of zero

								String removeSet = getMinValue(top15);
								if(value<top15.get(removeSet)) {
									top15.remove(removeSet);
									top15.put(key, value);
								}

							}
						}

					}

				}
			} 
		}
		System.out.println(top15);
		return top15;
	}


	public String getMinValue(HashMap<String, Double> top15) {

		String key = "";
		double minVal = 0.0;
		double tmp;
		boolean first = true;
		for (String hmkey : top15.keySet()) {

			tmp = top15.get(hmkey);

			if (first) {
				key = hmkey;
				minVal = tmp;	
				first = false;
			}

			if (minVal>tmp) {
				key = hmkey;
				minVal = tmp;
			}
		}

		return key;
	}


	//sort the HashMap in descending order (highest to lowest score)
	public ArrayList<HashMap<String, Double>> sortByValue(HashMap<String, Double> scoreToDocID) { 

		//result in descending order
		ArrayList<HashMap<String, Double>> result = new ArrayList<HashMap<String, Double>>();

		//tmp HashMap to insert in the result ArrayList
		HashMap<String, Double> tmp = new HashMap<String, Double>();

		//put all the scores from HashMap args into an primitive array
		Double[] score = scoreToDocID.values().toArray(new Double[0]);

		//put all the scores from args into an arrayList
		//an arrayList allows us to call a function to sort all the elements
		ArrayList<Double> scores = new ArrayList<Double>();

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
			tmp = new HashMap<String, Double>();

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
	public static Object getKeyFromValue(HashMap<String, Double> hm, Object value) {

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

	// Code modified from this website: 
	//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	public ArrayList<String> getDocIDs(ArrayList<HashMap<String, Double>> scoretoDocID) {
		ArrayList<String> docIDs = new ArrayList<String>();
		HashMap<String, Double> hm;

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


	// get scores in arrayList
	// Code modified from this website: 
	//http://www.java2s.com/Code/Java/Collections-Data-Structure/GetakeyfromvaluewithanHashMap.htm
	public ArrayList<Double> getScore(ArrayList<HashMap<String, Double>> scoretoDocID) {
		ArrayList<Double> scores = new ArrayList<Double>();
		HashMap<String, Double> hm;

		for (int i = 0; i < scoretoDocID.size(); i++) {

			// get hashMap at index i
			hm = scoretoDocID.get(i);

			// get key from hashMap
			for (String key : hm.keySet()) {
				scores.add(hm.get(key));
			}
		}
		return scores;
	}


}


