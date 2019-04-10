import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.stemmer.PorterStemmer;

public class DictionaryBuilder {
	Preprocessor p;
	PreprocessorReuters rp;
	protected TreeMap<String, String> corpus; // key: docID associated to value: description
	protected TreeMap<String, String> titles; // key: docID associated to value: title
	protected TreeMap<String, ArrayList<String>> topics; // FINAL: key: docID associated to value: list of topics

	protected ArrayList<ArrayList<String>> tokenList; // List of tokens for all courses
	protected HashMap<String, Integer> keytofreq; // Hashmap of word to list of doc frequency
	protected HashMap<String, ArrayList<String>> keytodocID; // Hashmap of word to list of docIDs

	protected HashMap<String, ArrayList<String>> topictodocID; // Hashmap of topic to list of docIDs

	protected HashMap<String, HashMap<String, Double>> weightList; // VSM

	protected File inputFile, outputFile;
	private final String FILEPATH = System.getProperty("user.dir"); // dir of project saved
	private final String STOPWORDS = File.separator + "stopwords.txt"; // stopword file

	private final String INPUT = File.separator + "assigned_reuters_output.txt"; // reuters file
	private final String DICTIONARY = File.separator + "dictionary.txt"; // external dictionary
	private final String INDEX = File.separator + "inverted_index.txt"; // external inverted index
	private final String TOKENLIST = File.separator + "tokenlist.txt"; // external tokenlist

	ArrayList<String> wordsetList = new ArrayList<String>();


	// For U of O courses
	public DictionaryBuilder(boolean stopword, boolean normalization, boolean stemming) {

		p = new Preprocessor();
		corpus = new TreeMap<String, String>();
		tokenList = new ArrayList<ArrayList<String>>();
		keytofreq = new HashMap<String, Integer>();
		keytodocID = new HashMap<String, ArrayList<String>>();
		weightList = new HashMap<String, HashMap<String, Double>>();
		titles = new TreeMap<String, String>();

		File file = p.getOutputFile();
		try {
			this.read(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.tokenize();
		this.stopWordRemoval(stopword);
		this.normalization(normalization);
		this.stemming(stemming);
		this.buildInvertedIndex();
		this.vsmWeightList();

	}

	// Reuters constructor ---------------------------------------------------
	public DictionaryBuilder(boolean stopword, boolean normalization, boolean stemming, char type) {

		//TMPPP
		int i = 0;

		try {
			rp = new PreprocessorReuters();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		corpus = new TreeMap<String, String>();
		tokenList = new ArrayList<ArrayList<String>>();
		keytofreq = new HashMap<String, Integer>();
		keytodocID = new HashMap<String, ArrayList<String>>();
		weightList = new HashMap<String, HashMap<String, Double>>();
		titles = new TreeMap<String, String>();
		topics = new TreeMap<String, ArrayList<String>>();

		topictodocID = new HashMap<String, ArrayList<String>>();	

		File file = rp.getOutputFile();
		try {
			this.read_reuters(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		//generate external dictionary bc it does not exist
		if (i == 0) {
			this.tokenize();
			this.stopWordRemoval(stopword);
			this.normalization(normalization);
			this.stemming(stemming);
			this.buildInvertedIndex();
			this.vsmWeightList();
			try {
				this.assignTopicTodocID();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			generate_Dictionary_reuter(); //var dictionary
			generate_InvertedIndex_reuter(); //var invertedIndex
			generate_Tokenlist_reuter();
			this.vsmWeightList();
			try {
				this.assignTopicTodocID();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}





	}

	/*
	 * Site used:
	 * https://stackoverflow.com/questions/29061782/java-read-txt-file-to-hashmap-
	 * split-by Modified reading a treemap from a text file using the 1st solution
	 * in StackOverflow
	 */
	private void read_reuters(File file) throws FileNotFoundException {

		file = new File(FILEPATH + INPUT);

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		try {
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("-", 2);
				if (parts.length >= 2) {
					String docID = parts[0];
					String text = parts[1];

					// Separate topics, title and description
					/*
					 * cocoa:BAHIA COCOA REVIEW---Showers topic:title---text
					 */
					String title = "";
					String description = "";
					String topic = "";
					ArrayList<String> topicsList = new ArrayList<String>();
					int endTopic = text.indexOf(":");
					title = text.substring(0, endTopic);
					int endTitle = text.indexOf("---");

					// Add topics
					if (text.contains(":")) {
						topic = text.substring(0, endTopic);

						// If many topics, split on whitespace
						String[] t = topic.split(" ");

						for (int i = 0; i < t.length; i++) {

							// System.out.println(topic);
							topicsList.add(t[i]);

						}

						// Add title
						title = text.substring(endTopic + 1, endTitle);
						// System.out.println(title);

						// Add body
						description = text.substring(endTitle + 3);
						// System.out.println(description);

					}
					corpus.put(docID, description);
					titles.put(docID, title);
					topics.put(docID, topicsList);

				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(File file) throws FileNotFoundException {

		file = p.getOutputFile();

		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		try {
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("-", 2);
				if (parts.length >= 2) {
					String docID = parts[0];
					String text = parts[1];

					// Separate course title and description
					String courseTitle = "";
					String courseDescription = "";
					int start = text.indexOf(":");
					courseTitle = text.substring(0, start);

					if (text.contains(":")) {
						courseDescription = text.substring(start + 1);
					} else {
						courseDescription = text;
					}
					corpus.put(docID, courseDescription);
					titles.put(docID, courseTitle);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Tokenizes the course descriptions and returns a list of tokens
	 */
	private ArrayList<ArrayList<String>> tokenize() {

		// Tokenize
		WhitespaceTokenizer simpleTokenizer = WhitespaceTokenizer.INSTANCE;
		String[] tokens = null;

		// Get text only (tous de les cours) (ex: t[0]: description du premier cours)
		String[] t = corpus.values().toArray(new String[0]);

		// Tokenize for each course
		for (int i = 0; i < t.length; i++) {

			// tokens: tous les mots 'tokenized' le cours i
			tokens = simpleTokenizer.tokenize(t[i]);

			ArrayList<String> tmp = new ArrayList<String>();

			for (int j = 0; j < tokens.length; j++) {

				int lastChar = tokens[j].length() - 1;
				String newToken = "";

				// Return tokens that end with a symbol
				if (!Character.isLetter(tokens[j].charAt(lastChar)) || tokens[j].contains("(")) {
					newToken = tokens[j].replaceAll("[^a-zA-Z0-9+/-]", "");
					tmp.add(newToken.toLowerCase());
				} else {
					if (tokens[j].trim().length()>0) {
						tmp.add(tokens[j].toLowerCase());	
					}
				}

			}

			tokenList.add(i, tmp);
		}
		// System.out.println("Tokens: " + tokenList);
		return tokenList;

	} // fin tokenize()

	/*
	 * Removes stopwords from the list of tokens Returns list of tokens without
	 * stopwords if true Otherwise, returns original list of tokens
	 */
	public ArrayList<ArrayList<String>> stopWordRemoval(boolean stopword) {

		if (stopword == true) {

			// À travers de le arrayList
			for (int i = 0; i < tokenList.size(); i++) {

				// À travers du arrayList à l'intérieur du ArrayList
				// tokens du cours i
				for (int j = 0; j < tokenList.get(i).size(); j++) {

					// à travers de chaque mot du cours i
					String word = tokenList.get(i).get(j);

					// si c'est un stopword, enlève le mot
					if (isStopWord(word) == true) {

						// enlever stop word du arrayList
						tokenList.get(i).remove(word);
					}
				}
			}

			return tokenList;
		}
		return tokenList;
	}

	/*
	 * Site used: https://coderanch.com/t/631347/java/Search-word-text-file Used
	 * snippet of code provided in forum to read a text file that contains a
	 * stopword
	 */
	private boolean isStopWord(String word) {

		FileReader inputFile;
		try {
			inputFile = new FileReader(FILEPATH + STOPWORDS);
			BufferedReader br = new BufferedReader(inputFile);
			String line;

			try {
				while ((line = br.readLine()) != null) {
					if (line.contains(word))
						return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return false;

	}

	/*
	 * Stemming the list of tokens Returns list of stemmed tokens if true Otherwise,
	 * returns original list of tokens
	 */
	private ArrayList<ArrayList<String>> stemming(boolean stem) {

		if (stem == true) {

			// declare and initialize PorterStemmer
			PorterStemmer tmp = new PorterStemmer();

			// declare and initialize stemword
			String stemword = "";

			// À travers de le arrayList
			for (int i = 0; i < tokenList.size(); i++) {

				// À travers du arrayList à l'intérieur du ArrayList
				// tokens du cours i
				for (int j = 0; j < tokenList.get(i).size(); j++) {

					// à travers de chaque mot du cours i
					String word = tokenList.get(i).get(j);

					// stem the word using the built-inn stem function
					// si c'est un stopword, enlève le mot
					stemword = tmp.stem(word);

					// replace word by stemword in arrayList
					tokenList.get(i).set(j, stemword);
				}
			}
			// System.out.println("Stem: " + tokenList);

			return tokenList;
		}

		return tokenList;
	}

	/*
	 * Normalization of the list of tokens Returns list of normalized tokens if true
	 * Otherwise, returns original list of tokens
	 */
	private ArrayList<ArrayList<String>> normalization(boolean norm) {

		if (norm == true) {
			String word = "";

			// À travers de le arrayList
			for (int i = 0; i < tokenList.size(); i++) {

				// À travers du arrayList à l'intérieur du ArrayList
				// tokens du cours i
				for (int j = 0; j < tokenList.get(i).size(); j++) {

					// à travers de chaque mot du cours i
					word = tokenList.get(i).get(j);

					word = word.replaceAll("^\"|\"$|\\d|,|[(){}:;.,'&/<>-]", "");

					// replace word by normalized word in arrayList
					tokenList.get(i).set(j, word);
				}
			}
			// System.out.println("Norm: " + tokenList);

			return tokenList;
		}
		return tokenList;
	}

	/* DictionaryBuilder Getters */
	public ArrayList<String> getDictionary() {
		// Unique set of tokens
		Set<String> dictionary = new HashSet<String>();

		for (int i = 0; i < tokenList.size(); i++) {
			for (int j = 0; j < tokenList.get(i).size(); j++) {
				if (tokenList.get(i).get(j).trim().length()>0) {
					dictionary.add(tokenList.get(i).get(j));
				}
			}
		}

		wordsetList = new ArrayList<String>(dictionary);

		// Sort tokens
		Collections.sort(wordsetList);

		return wordsetList;
	}

	// Return number of total docIDs
	public int getTotalNumdocID() {
		return corpus.keySet().size();
	}

	// Return corpus
	public TreeMap<String, String> getCorpus() {
		return corpus;
	}

	// Return course description
	public String getDescription(String docID) {
		return corpus.get(docID);

	}

	// Return course titles
	public String getTitle(String docID) {
		return titles.get(docID);

	}

	// Get specific docID
	public String getdocID(int i) {
		ArrayList<String> docIDs = new ArrayList<String>(corpus.keySet());
		return docIDs.get(i);
	}

	// Get list of tokens for each document
	public ArrayList<ArrayList<String>> gettokenList() {
		return tokenList;
	}

	// Get a list of topics with the corresponding docIDs (similar to inverted
	// index)
	public HashMap<String, ArrayList<String>> gettopictodocID() {
		return topictodocID;
	}

	/**
	 * Module 4 - Inverted
	 * Index----------------------------------------------------------------------------
	 */
	private void buildInvertedIndex() {

		// 1 - put all unique words from tokenList into ArrayList
		wordsetList = getDictionary();

		// 2 - sort ArrayList
		Collections.sort(wordsetList);

		String word = "";
		int docfrequency;

		ArrayList<String> list;
		HashMap<String, Double> weights = new HashMap<String, Double>();

		for (int i = 0; i < wordsetList.size(); i++) {
			word = wordsetList.get(i);

			docfrequency = 0;
			list = new ArrayList<String>();

			weights = new HashMap<String, Double>();

			// get list of items in document i
			for (int j = 0; j < tokenList.size(); j++) {

				// if doc has the word from the set
				if (tokenList.get(j).contains(word)) {
					docfrequency++;
					list.add(getdocID(j));
				}
			}

			// link key to doc frequency
			keytofreq.put(word, docfrequency);

			// sort docID in list
			Collections.sort(list);

			// link key to a list of docID
			keytodocID.put(word, list);

		} // end of for loops

	}

	/**
	 * Module 7a - VSM Weight
	 * calculation----------------------------------------------------------------------------
	 */

	/*
	 * Calculate tf-idf weight
	 */
	public double calculateWeight(String docID, String term) {
		int tf = getTermFreq(docID, term);
		int df = keytofreq.get(term);
		int numDocs = tokenList.size();
		double idf = Math.log10(numDocs / df);

		return tf * idf; // weight
	}

	/*
	 * Return list of weights for VSM model
	 */
	private HashMap<String, HashMap<String, Double>> vsmWeightList() {

		// tous les mots uniques dans le dictionnaire
		//ArrayList<String> wordsetList = getDictionary();

		// 2 - sort ArrayList
		Collections.sort(wordsetList);

		String word = "";
		double weight;

		// initialise the HashMap within the weightList HashMap
		HashMap<String, Double> tmp = new HashMap<String, Double>();

		// got through all the unique words in the dictionary from wordsetList
		for (int i = 0; i < wordsetList.size(); i++) {
			word = wordsetList.get(i);
			weight = 0;

			// String: docID ; Double: tf*idf
			tmp = new HashMap<String, Double>();

			// get list of items in document i
			for (int j = 0; j < tokenList.size(); j++) {

				// if doc has the word from the set
				if (tokenList.get(j).contains(word)) {
					weight = calculateWeight(getdocID(j), word);
					tmp.put(getdocID(j), weight);

				}
			}

			weightList.put(word, tmp);

		} // end of for loops

		return weightList;
	}

	/* VSM Getters */
	public HashMap<String, ArrayList<String>> getInvertedIndex() {
		return keytodocID;
	}

	public HashMap<String, HashMap<String, Double>> getweightList() {
		return weightList;
	}

	/*
	 * Calculate term frequency of word in a document
	 */
	public int getTermFreq(String docID, String term) {

		String course = corpus.get(docID);
		String docIDToken[] = course.split(" ");
		int termfreq = 0;
		for (int i = 0; i < docIDToken.length; i++) {
			if (docIDToken[i].equals(term.toLowerCase())) {
				termfreq++;
			}
		}
		return termfreq;
	}

	public TreeMap<String, ArrayList<String>> getTopics() {
		return topics;
	}

	//TODO
	public String getTopic(String docID) {

		ArrayList<String> lst = topics.get(docID);
		StringBuilder res = new StringBuilder();
		for (String s : lst) {
			res.append(s + " ");
		}

		return res.toString();

	}


	// Write to a file
	private void writeDictionary(ArrayList<String> input) {

		outputFile = new File(FILEPATH + DICTIONARY);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (String s : input) {
				bw.write(s);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
		}

	}


	private void writeInvertedIndex(HashMap<String, ArrayList<String>> input) {

		outputFile = new File(FILEPATH + INDEX);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (String s : input.keySet()) {
				bw.write(s + "-");
				for (int i = 0; i < input.get(s).size(); i++) {

					bw.write(input.get(s).get(i) + " ");
				}
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
		}
	}
	
	private void writeTokenList(ArrayList<ArrayList<String>> input) {
		
		outputFile = new File(FILEPATH + TOKENLIST);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < input.size(); i++) {
				bw.write(i + "-");
				for (int j = 0; j < input.get(i).size(); j++) {

					bw.write(input.get(i).get(j) + " ");
				}
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
		}
		
	}

	//Read external files
	private void generate_Dictionary_reuter() {

		inputFile = new File(FILEPATH + DICTIONARY);

		String line;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

			while ((line = reader.readLine()) != null) {

				wordsetList.add(line);	
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ArrayList<String> getExternalDictionary_reuter() {
		return wordsetList;
	}

	private HashMap<String, ArrayList<String>> generate_InvertedIndex_reuter() {

		inputFile = new File(FILEPATH + INDEX);
		String line;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("-", 2);
				if (parts.length >= 2) {
					String word = parts[0];
					String docID = parts[1];

					ArrayList<String> docIDList = new ArrayList<String>();
					// If many docIDs, split on whitespace
					String[] d = docID.split(" ");

					for (int i = 0; i < d.length; i++) {

						docIDList.add(d[i]);

					}

					keytodocID.put(word, docIDList);
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return keytodocID;

	}
	
	private ArrayList<ArrayList<String>> generate_Tokenlist_reuter() {

		inputFile = new File(FILEPATH + TOKENLIST);
		String line;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("-", 2);
				if (parts.length >= 2) {
					String index = parts[0];
					String text = parts[1];

					ArrayList<String> tList = new ArrayList<String>();
					String[] d = text.split(" ");

					for (int i = 0; i < d.length; i++) {

						tList.add(d[i]);

					}

					tokenList.add(Integer.parseInt(index), tList);
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tokenList;
	}

	// Given a topic, creates list of docIDs with this topic
	private void assignTopicTodocID() throws FileNotFoundException {

		String FILEPATH = System.getProperty("user.dir");
		String INPUT = File.separator + "reuters21578.tar" + File.separator + "all-topics-strings.lc.txt";

		File file = new File(FILEPATH + INPUT);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String tmp;
		ArrayList<String> topicsList = new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>();

		// Read list of topics into ArrayList
		try {

			while ((tmp = br.readLine()) != null) {
				topicsList.add(tmp);

			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Get final list of topics
		TreeMap<String, ArrayList<String>> t = getTopics();
		String topic = "";
		String topic2 = "";
		int len = 0;
		for (int i = 0; i < topicsList.size(); i++) {

			topic = topicsList.get(i);

			list = new ArrayList<String>();

			// Compare each topic to final topic list
			for (String key : t.keySet()) {
				len = t.get(key).size();

				for (int j = 0; j < len; j++) {
					topic2 = t.get(key).get(j);

					if (topic.equals(topic2)) {

						list.add(key);

					}

				}

			}

			topictodocID.put(topic, list);
		}

	}

	public static void main(String[] args) throws IOException {

		DictionaryBuilder db = new DictionaryBuilder(true,true,false,'r');
		ArrayList<String> test = db.getDictionary();
		//System.out.println("dict: " + test);
		db.writeDictionary(test);

		HashMap<String, ArrayList<String>> t = db.getInvertedIndex();
		// System.out.println("Inv index: " + t);
		db.writeInvertedIndex(t);
		
		ArrayList<ArrayList<String>> tl = db.gettokenList();
		db.writeTokenList(tl);
		//db.assignTopicTodocID();
		/*HashMap<String, ArrayList<String>> t = db.gettopictodocID();
		System.out.println(t);*/

	}

}
