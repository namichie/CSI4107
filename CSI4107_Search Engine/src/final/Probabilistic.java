import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Probabilistic {

	/*
	 * Optional Module - Relevance feedback
 Either implicitly (with clicks) or explicitly (with checkbox) capture the documents
 looked at by the user for a particular query. This module creates a "relevance
 memory", keeping track of relevant documents per query.
	 * 
	 * 
	 * Optional Module - Probabilistic Relevance Retrieval Model
 A query, and information (perhaps null) about which documents are relevant for
 this query (the relevance memory).
	 *
	 */


	HashMap<String, HashMap<String, Integer>> queryMemory; //relevance memory, data saved in text file and this variable
	DictionaryBuilder db; //reference to dictionary
	Boolean_Model bmodel; //reference to boolean model
	VSM vsm; //reference to vector space model
	String query; //user input

	private final String FILEPATH = System.getProperty("user.dir"); //dir of project saved
	private final String OUTPUT = File.separator + "queryMemory.txt"; //name of text file



	/*
	 * different constructors depending if which functions we need to call from another class
	 */

	//args: none
	Probabilistic() {}

	// args: queryMemory ; query ; stopWord ; normalization ; stemming
	Probabilistic(HashMap<String, HashMap<String, Integer>> queryMemory, String query, boolean stopWord,
			boolean normalization, boolean stemming) {
		this.queryMemory = queryMemory;
		query = this.query;
		db = new DictionaryBuilder(stopWord, normalization, stemming);
		vsm = new VSM(stopWord, normalization, stemming);
		bmodel = new Boolean_Model(stopWord, normalization, stemming);
	}

	// args: queryMemory ; stopWord ; normalization ; stemming
	Probabilistic(HashMap<String, HashMap<String, Integer>> queryMemory, boolean stopWord, boolean normalization,
			boolean stemming) {
		this.queryMemory = queryMemory;
		db = new DictionaryBuilder(stopWord, normalization, stemming);
		vsm = new VSM(stopWord, normalization, stemming);
		bmodel = new Boolean_Model(stopWord, normalization, stemming);
	}

	// args: stopWord ; normalization ; stemming
	Probabilistic(boolean stopWord, boolean normalization, boolean stemming) {
		db = new DictionaryBuilder(stopWord, normalization, stemming);
		bmodel = new Boolean_Model(stopWord, normalization, stemming);
	}

	// args: DictionaryBuilder
	Probabilistic(DictionaryBuilder db) {
		this.db = db;
	}


	/*
	 * -------------------- Query exists in memory -----------------------
	 * We are going to use the HashMap queryMemory where it takes the relevant documents
	 * based on the user input
	 */

	// return a list of docIDs with highest to lowest RSV score
	public ArrayList<String> rank_By_RSV(String query) {
		ArrayList<String> res = new ArrayList<String>(); // docID with highest to lowest RSV score
		ArrayList<String> D = getRelevantDocs(query); // relevant doc set

		// String: word ; ArrayList: list of docIDs that contain that word
		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex();

		// String: word; Double[pw , rw]
		HashMap<String, Double[]> wordTo_pw_rw = new HashMap<String, Double[]>();

		// String: word; Double: cw
		HashMap<String, Double> wordTo_cw = new HashMap<String, Double>();

		int N = db.getTotalNumdocID(); // total number of documents

		// each word goes into the string array
		String tokenList[] = query.split(" ");

		// calculate p, r, c
		// loop through each word and calculate pw, rw, and cw
		for (int i = 0; i < tokenList.length; i++) {

			// store pw and rw values with the word in wordTo_pw_rw HashMap
			Double[] pw_rw = calculate_pw_rw(tokenList[i], D, N);
			wordTo_pw_rw.put(tokenList[i], pw_rw);

			// store cw values in wordTo_cw HashMap
			double cw = calculate_cw(pw_rw[0], pw_rw[1]);
			wordTo_cw.put(tokenList[i], cw);
		}

		// key: docID ; Double: score using RSV
		HashMap<String, Double> docToRSV = RSV_calculation(wordTo_cw, tokenList);

		// arrayList has the docID sorted from highest to lowest
		ArrayList<HashMap<String, Double>> tmp = new ArrayList<HashMap<String, Double>>();
		tmp = sortByValue(docToRSV);

		// docID in order that is displayed in the UI
		res = getDocIDs(tmp);

		return res;
	}

	// return an array list of docIDs with its corresponding score into a hashMap 
	// the array list goes from highest to lowest RSV score
	public ArrayList<HashMap<String, Double>> rank_RSV(String query) {
		ArrayList<HashMap<String, Double>> res = new ArrayList<HashMap<String, Double>>(); // docID with highest to
		// lowest RSV score
		ArrayList<String> D = getRelevantDocs(query); // relevant doc set

		// String: word ; ArrayList: list of docIDs that contain that word
		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex();

		// String: word; Double[pw , rw]
		HashMap<String, Double[]> wordTo_pw_rw = new HashMap<String, Double[]>();

		// String: word; Double: cw
		HashMap<String, Double> wordTo_cw = new HashMap<String, Double>();

		int N = db.getTotalNumdocID(); // total number of documents

		// each word goes into the string array
		String tokenList[] = query.split(" ");

		// calculate p, r, c
		// loop through each word and calculate pw, rw, and cw
		for (int i = 0; i < tokenList.length; i++) {

			// store pw and rw values with the word in wordTo_pw_rw HashMap
			Double[] pw_rw = calculate_pw_rw(tokenList[i], D, N);
			wordTo_pw_rw.put(tokenList[i], pw_rw);

			// store cw values in wordTo_cw HashMap
			double cw = calculate_cw(pw_rw[0], pw_rw[1]);
			wordTo_cw.put(tokenList[i], cw);
		}

		// key: docID ; Double: score using RSV
		HashMap<String, Double> docToRSV = RSV_calculation(wordTo_cw, tokenList);

		// arrayList has the docID sorted from highest to lowest
		res = sortByValue(docToRSV);

		return res;
	}


	//return a list of docID in same order as the HashMap
	public ArrayList<String> rank_RSV_Doc(ArrayList<HashMap<String, Double>> tmp) {

		//create ArrayList
		ArrayList<String> res = new ArrayList<String>();

		//get the docIDs from the HashMap in the order of the ArrayList
		res = getDocIDs(tmp);

		return res;
	}


	//return a list of scores in same order as the HashMap
	public ArrayList<Double> rank_RSV_Scores(ArrayList<HashMap<String, Double>> tmp) {

		//create ArrayList
		ArrayList<Double> res = new ArrayList<Double>();

		//get the scores from the HashMap in the order of the ArrayList
		res = getScore(tmp);

		return res;
	}


	// calculate pw and rw and return pw and rw in a list
	public Double[] calculate_pw_rw(String word, ArrayList<String> D, int N) {

		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex(); //inverted index
		ArrayList<String> wordToDocID = hm.get(word); //get the docIDs associated to that word
		int dfw = wordToDocID.size(); //document frequency of that word
		double pw, rw; 

		// list of docIDs that are in the relevant document list
		// wordToDocID: list of docIDs that contain the word
		// D: relevant docIDs that contain the word
		ArrayList<String> Dw = bmodel.intersection(wordToDocID, D);

		//Formulas seened in class:
		// pw = (|Dw| + 0.5) / (|D| + 1)
		pw = (Dw.size() + 0.5) / (D.size() + 1);

		// rw = (dfw - |Dw| + 0.5) / (|N| - |D| + 1)
		rw = (dfw - Dw.size() + 0.5) / (N - D.size() + 1);

		// add pw and rw values in an array
		Double[] res = { pw, rw };

		return res;
	}


	// return the value cw based on the args pw and rw
	public double calculate_cw(double pw, double rw) {
		double res;

		//Formula seened in class:
		//cw = Odds(pw)/Odds(rw) = (pw * (1 - rw)) / (rw * (1 - pw))
		res = (pw * (1 - rw)) / (rw * (1 - pw));
		return res;
	}


	// returns the relevant documents based on the query and value saved in memory
	public ArrayList<String> getRelevantDocs(String query) {

		// relevant document ArrayList
		ArrayList<String> relevantDocIDs = new ArrayList<String>();

		// hashMap (key:docID, value:number times user opened doc)
		//for that query, get the docID and the number of times opened by the user
		HashMap<String, Integer> docIDToFreq = queryMemory.get(query);

		// get key from hashMap
		for (String key : docIDToFreq.keySet()) {
			// add every key (docID) into the arrayList
			relevantDocIDs.add(key);
		}

		return relevantDocIDs;
	}


	// calculates the score using RSV for each document
	public HashMap<String, Double> RSV_calculation(HashMap<String, Double> wordTo_cw, String[] tokenList) {

		// String: word ; ArrayList: list of docIDs that contain that word
		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex();

		//return HashMap where key: docID and value: score using RSV
		HashMap<String, Double> docToscores = new HashMap<String, Double>();

		double cw = 0; //declare and initialize value

		// loop each word in the query
		for (int i = 0; i < tokenList.length; i++) {

			// get the cw for this word
			cw = wordTo_cw.get(tokenList[i]);

			// if the word exists in the dictionary
			if (hm.containsKey(tokenList[i])) {

				// get list of all the docID containing this word in the ArrayList
				ArrayList<String> docID = db.getInvertedIndex().get(tokenList[i]);

				// go through all the docID containing this word in the ArrayList
				for (int j = 0; j < docID.size(); j++) {

					// if docID already exists in docToscores, update the score
					if (docToscores.containsKey(docID.get(j)) && cw > 0) {
						double update_score = docToscores.get(docID.get(j)) + Math.log(cw);
						docToscores.put(docID.get(j), update_score);
					}

					// add new docID already exists in docToscores
					else if (!docToscores.containsKey(docID.get(j)) && cw > 0) {
						docToscores.put(docID.get(j), Math.log(cw));
					}
				}
			}
		}

		return docToscores;
	}


	/*
	 * -------------------- Query does NOT exist in memory -----------------------
	 * If the query does not exist in the memory (user has never opened a document based on this query),
	 * then rank by calculating idf (seen in class - bootstrapping approach)
	 */


	//return the docID from highest to lowest score in ArrayList
	public ArrayList<String> rank_By_IDF(String query) {
		String tokenList[] = query.split(" "); //split all words from query
		double idf; //idf

		ArrayList<String> res = new ArrayList<String>(); //returned result
		HashMap<String, Double> wordToidf = new HashMap<String, Double>(); //key:word and value:idf
		HashMap<String, Double> docToscores = new HashMap<String, Double>(); //key:docID and value:score using idf
		ArrayList<HashMap<String, Double>> tmp = new ArrayList<HashMap<String, Double>>(); //store tmp data

		// associate the word to the idf
		for (int i = 0; i < tokenList.length; i++) {
			idf = idf_calculation(tokenList[i]); //calculate idf
			wordToidf.put(tokenList[i], idf); // word associated to idf
		}

		// call the function to calculate the ranking for all the documents
		docToscores = score_calculation(wordToidf, tokenList);

		// sort documents by highest score
		if (docToscores.size() > 0) {
			// score from highest to lowest in ArrayList
			// where tmp has the doc associated with its score
			tmp = sortByValue(docToscores);

			// docID in order that is displayed in the UI
			res = getDocIDs(tmp);
		}
		return res;
	}


	// return an array list of docIDs with its corresponding score into a hashMap 
	// the array list goes from highest to lowest idf score
	public ArrayList<HashMap<String, Double>> rank_IDF(String query) {
		String tokenList[] = query.split(" "); //split all words from query
		double idf; //idf

		HashMap<String, Double> wordToidf = new HashMap<String, Double>(); //key:word and value:idf
		HashMap<String, Double> docToscores = new HashMap<String, Double>(); //key:docID and value:score using idf
		ArrayList<HashMap<String, Double>> res = new ArrayList<HashMap<String, Double>>(); //returned result

		// associate the word to the idf
		for (int i = 0; i < tokenList.length; i++) {
			idf = idf_calculation(tokenList[i]);
			wordToidf.put(tokenList[i], idf); // word associated to idf
		}

		// call the function to calculate the ranking for all the documents
		docToscores = score_calculation(wordToidf, tokenList);

		// score from highest to lowest in ArrayList
		// where tmp has the doc associated with its score
		res = sortByValue(docToscores);

		return res;
	}


	//return a list of docID in same order as the HashMap
	public ArrayList<String> rank_IDF_Doc(ArrayList<HashMap<String, Double>> tmp) {

		ArrayList<String> res = new ArrayList<String>();
		res = getDocIDs(tmp);

		return res;
	}


	//return a list of scores in same order as the HashMap
	public ArrayList<Double> rank_IDF_Scores(ArrayList<HashMap<String, Double>> tmp) {

		ArrayList<Double> res = new ArrayList<Double>();
		res = getScore(tmp);

		return res;
	}


	// calculates the idf for one word
	public double idf_calculation(String word) {
		double idf = 0; // log(N/df)
		int N = db.getTotalNumdocID(); // total number of documents
		int df; // number of document that have the word
		// String: word ; ArrayList: list of docIDs that contain that word
		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex();

		// if the word exists in the dictionary
		if (hm.containsKey(word)) {
			df = hm.get(word).size(); // number of docs that contain that word
			idf = Math.log(N / df);
			return idf;
		}

		// if the word does NOT exists in the dictionary
		// df = 0, which means that idf = 0
		else {
			return 0;
		}
	}


	// calculates the score for each document
	public HashMap<String, Double> score_calculation(HashMap<String, Double> wordToidf, String[] tokenList) {

		// String: word ; ArrayList: list of docIDs that contain that word
		HashMap<String, ArrayList<String>> hm = db.getInvertedIndex();
		HashMap<String, Double> docToscores = new HashMap<String, Double>();
		double idf = 0;

		// loop each word in the query
		for (int i = 0; i < tokenList.length; i++) {

			// get the idf for this word
			idf = wordToidf.get(tokenList[i]);

			// if the word exists in the dictionary
			if (hm.containsKey(tokenList[i])) {

				// get list of all the docID containing this word in the ArrayList
				ArrayList<String> docID = db.getInvertedIndex().get(tokenList[i]);

				// go through all the docID containing this word in the ArrayList
				for (int j = 0; j < docID.size(); j++) {

					// if docID already exists in docToscores, update the score
					if (docToscores.containsKey(docID.get(j)) && idf > 0) {
						double update_score = docToscores.get(docID.get(j)) + idf;
						docToscores.put(docID.get(j), update_score);
					}

					// add new docID already exists in docToscores
					else if (!docToscores.containsKey(docID.get(j)) && idf > 0) {
						docToscores.put(docID.get(j), idf);
					}
				}
			}
		}

		return docToscores;
	}


	/*
	 *
	 * Extra helper functions being called
	 * 
	 */

	// sort the HashMap in descending order (highest to lowest score)
	public ArrayList<HashMap<String, Double>> sortByValue(HashMap<String, Double> scoreToDocID) {

		// result in descending order
		ArrayList<HashMap<String, Double>> result = new ArrayList<HashMap<String, Double>>();

		// tmp HashMap to insert in the result ArrayList
		HashMap<String, Double> tmp = new HashMap<String, Double>();

		// put all the scores from HashMap args into an primitive array
		Double[] score = scoreToDocID.values().toArray(new Double[0]);

		// put all the scores from args into an arrayList
		// an arrayList allows us to call a function to sort all the elements
		ArrayList<Double> scores = new ArrayList<Double>();

		// key, which is the docID
		String key;

		// go through all the scores of the primitive array and add it to the arrayList
		for (int i = 0; i < score.length; i++) {
			scores.add(score[i]);
		}

		// arrayList can sort all the scores
		Collections.sort(scores);

		// descending order
		Collections.reverse(scores);

		for (int j = 0; j < scores.size(); j++) {
			// get key associated to the score
			key = getKeyFromValue(scoreToDocID, scores.get(j)).toString();
			// System.out.println(scores.get(j) + " ; getKey: " + key);

			// remove key from args
			// If there are duplicate values, we want to get all the different docID
			scoreToDocID.remove(key);

			// reinitialise HashMap to nothing
			tmp = new HashMap<String, Double>();

			// add it to the result arrayList in descending order
			// because we sorted in descending order the arrayList scores
			tmp.put(key, scores.get(j));
			result.add(j, tmp);
		}

		// return HashMap with descending order scores with its associated docID
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


	// Read text file and save the info into a HashMap 
	// Code copied and modified from this website:
	// https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
	public HashMap<String, HashMap<String, Integer>> readFile() throws IOException {
		HashMap<String, HashMap<String, Integer>> queryMemory = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> tmp;

		File file = new File(FILEPATH + OUTPUT);

		BufferedReader br = new BufferedReader(new FileReader(file));

		String qryInput;
		try {

			//read by line
			while ((qryInput = br.readLine()) != null) {

				/*
				 * sentence ex: system operating ייי 8421 ייי 2 "ייי" allows us to seperate the
				 * user input, docID, number of times doc was chosen tokenList[0]: user input
				 * tokenList[1]: docID tokenList[2]: number of times doc was chosen
				 */
				String tokenList[] = qryInput.split(" ייי ");

				//if query, which is the key, exists in hashMap
				if (queryMemory.containsKey(tokenList[0])) {
					tmp = new HashMap<String, Integer>(); //initialize hashMap
					tmp = queryMemory.get(tokenList[0]); //get the current hashMap for that query
					tmp.put(tokenList[1], Integer.parseInt(tokenList[2])); //add info to that hashMap
					queryMemory.replace(tokenList[0], tmp); //replace old hashMap to new one
				}

				//if query, which is the key, does NOT exists in hashMap
				else {
					tmp = new HashMap<String, Integer>(); //initialize hashMap
					tmp.put(tokenList[1], Integer.parseInt(tokenList[2])); //add value in hashMap
					queryMemory.put(tokenList[0], tmp); //associate that hashMap to the query
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return queryMemory;
	}


	// Save relevant document to a file
	// Code copied and modified from this website:
	// https://stackoverflow.com/questions/26188532/iterate-through-nested-hashmap
	public void writeToFile(HashMap<String, HashMap<String, Integer>> queryMemory) {

		//file saved at FILEPATH + OUTPUT location
		File memoryFile = new File(FILEPATH + OUTPUT);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(memoryFile));
			for (Map.Entry<String, HashMap<String, Integer>> queryEntry : queryMemory.entrySet()) {
				String query = queryEntry.getKey(); //get key of HashMap

				for (Map.Entry<String, Integer> docIDEntry : queryEntry.getValue().entrySet()) {
					String docID = docIDEntry.getKey(); //get key inside hashMap, which is the docID
					Integer freq = docIDEntry.getValue(); //get value inside hashMap, which is the number of times user opened doc

					//seperate value by "ייי" because we know that there are no english words that contain accents
					bw.write(query + " ייי " + docID + " ייי " + freq); 

					//next line
					bw.newLine(); 
				}
			}

			bw.flush();
			bw.close();
		} catch (Exception e) {
		}
	}


	// Check if the text file exits
	// Copied code from this website:
	// https://alvinalexander.com/java/java-file-exists-directory-exists
	public boolean isqueryMemoryExist() {
		File tmpDir = new File(FILEPATH + OUTPUT);
		boolean exists = tmpDir.exists();
		return exists; 
	}

}
