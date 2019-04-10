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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.WhitespaceTokenizer;

public class MachineLearning {

	PreprocessorReuters rp;

	protected HashMap<String, ArrayList<String>> tokenList;

	protected TreeMap<String, String> corpus; // key: docID associated to value: description
	protected TreeMap<String, String> titles; // key: docID associated to value: title
	protected TreeMap<String, ArrayList<String>> topics; // FINAL: key: docID associated to value: list of topics

	protected HashMap<String, ArrayList<String>> trainingSet; // Training set
	protected HashMap<String, ArrayList<String>> testSet; // Test set

	protected TreeMap<String, ArrayList<String>> assignedTopics; // result of classification
	protected TreeMap<String, String> unassignedDocs; // docs with no topics

	protected ArrayList<String> content;
	protected TreeMap<String, String> output;

	protected File outputFile;
	private final String FILEPATH = System.getProperty("user.dir");
	private final String STOPWORDS = File.separator + "stopwords.txt"; // stopword file
	private final String OUTPUT = File.separator + "assigned_reuters_output.txt";

	public MachineLearning() {
		
		content = new ArrayList<String>();
		output = new TreeMap<String, String>();

		try {
			rp = new PreprocessorReuters();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		corpus = new TreeMap<String, String>();
		tokenList = new HashMap<String, ArrayList<String>>();
		titles = new TreeMap<String, String>();
		topics = new TreeMap<String, ArrayList<String>>();

		assignedTopics = new TreeMap<String, ArrayList<String>>();

		unassignedDocs = new TreeMap<String, String>();
		trainingSet = new HashMap<String, ArrayList<String>>();
		testSet = new HashMap<String, ArrayList<String>>();


		File file = rp.getOutputFile();
		try {
			this.read_reuters(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.tokenize();
		this.stopWordRemoval();
		this.normalization();
		this.stemming();

	}

	private void kNN() {
		HashMap<String, ArrayList<String>> test = getTestSet();

		// for each key in the hashMap
		for (String test_key : test.keySet()) {
			assignTopics(test_key);
		}

		// Reassign topics and write classified topics to file
		TreeMap<String, ArrayList<String>> t = addToTopics();

		// Combine topics + title + body
		for (String key : t.keySet()) {
			String topicString = String.join(" ", t.get(key));

			content.add(topicString + ":" + titles.get(key) + "---" + corpus.get(key));

			// Add document to treemap
			for (int j = 0; j < content.size(); j++) {

				output.put(key, content.get(j));
			}

		}

		write(output);

	} // end of k-NN function

	

	/*
	 * Site used:
	 * https://stackoverflow.com/questions/29061782/java-read-txt-file-to-hashmap-split-by 
	 * Modified reading a treemap from a text file using the 1st solution
	 * in StackOverflow
	 */
	private void read_reuters(File file) throws FileNotFoundException {

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

						if (topic.contains("N/A")) {
							unassignedDocs.put(docID, topic);

						} else {
							// If many topics, split on whitespace
							String[] t = topic.split(" ");

							for (int i = 0; i < t.length; i++) {

								// System.out.println(topic);
								topicsList.add(t[i]);

							}
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
				// System.out.println("ORIGINAL: " + topics);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Get training set
	public HashMap<String, ArrayList<String>> getTrainingSet() {
		HashMap<String, ArrayList<String>> tlist = gettokenList();

		for (int i = 0; i < tlist.size(); i++) {

			// Add docID only if it has a topic

			for (String assigned_key : topics.keySet()) {

				if (!unassignedDocs.containsKey(assigned_key)) {

					trainingSet.put(assigned_key, tlist.get(assigned_key));
				}
			}

		}
		return trainingSet;

	}

	// Get test set
	public HashMap<String, ArrayList<String>> getTestSet() {
		HashMap<String, ArrayList<String>> tlist = gettokenList();

		// for each key in the hashMap
		for (String unassigned_key : unassignedDocs.keySet()) {
			testSet.put(unassigned_key, tlist.get(unassigned_key));
		} // fin du loop

		return testSet;
	}

	
	
	/*
	 * Loop through all training set to find best similarity to that test document
	 */
	public void assignTopics(String test_key) {
		double tmp = 0.0;
		double max = 0.0;
		String max_key = "";
		ArrayList<String> topicsList;
		HashMap<String, ArrayList<String>> training = getTrainingSet();

		// for each key in the hashMap
		for (String train_key : training.keySet()) {
			tmp = Jaccard_Similarity(test_key, train_key);
			if (tmp > max) {
				max = tmp;
				max_key = train_key;
			}
		} // fin du loop

		if (topics.containsKey(max_key)) {
			topicsList = topics.get(max_key);
			assignedTopics.put(test_key, topicsList);

		} else {
			ArrayList<String> err = new ArrayList<String>();
			err.add("N/A");
			assignedTopics.put(test_key, err);
		}

	}

	/*
	 * Find the similarity between the 2 tokens, document A (training) and document
	 * B (test) Jaccard: | DOC A and DOC B|/|DOC A or DOC B| Code modified from:
	 * https://stackoverflow.com/questions/51113134/union-and-intersection-of-java-
	 * sets
	 */
	public double Jaccard_Similarity(String test_key, String train_key) {

		ArrayList<String> test_tokens = getTestSet().get(test_key);
		ArrayList<String> training_tokens = getTrainingSet().get(train_key);

		Set tokenSet_docA = new HashSet(); // all tokens corresponding docA
		Set tokenSet_docB = new HashSet(); // all tokens corresponding docB
		Set intersection, union; // intersection and union sets

		// put all tokens from test document in set, this will eliminate the duplicates
		for (int i = 0; i < test_tokens.size(); i++) {
			tokenSet_docA.add(test_tokens.get(i));
		}

		// put all tokens from training document in set, this will eliminate the
		// duplicates
		for (int i = 0; i < training_tokens.size(); i++) {
			tokenSet_docB.add(training_tokens.get(i));
		}

		// determine intersection set
		intersection = new TreeSet(tokenSet_docA);
		intersection.retainAll(tokenSet_docB);

		// determine union set
		union = new TreeSet(tokenSet_docA);
		union.addAll(tokenSet_docB);

		// Calculate Jaccard similarity
		double similarity = (double) intersection.size() / (double) union.size();

		return similarity;
	}

	// Add assigned topics to original topic list
	private TreeMap<String, ArrayList<String>> addToTopics() {

		for (String key : topics.keySet()) {

			// If key appears in list for topics that originally had no topics
			if (assignedTopics.containsKey(key)) {
				topics.replace(key, assignedTopics.get(key));
			}
		}
		return topics;
	}

	// Tokenization, stemming, stopword removal, normalization ------------------------------
	/*
	 * Tokenizes the course descriptions and returns a list of tokens
	 */
	private HashMap<String, ArrayList<String>> tokenize() {

		// Tokenize
		WhitespaceTokenizer simpleTokenizer = WhitespaceTokenizer.INSTANCE;
		String[] tokens = null;

		// for each key in the hashMap
		for (String rkey : corpus.keySet()) {
			tokens = simpleTokenizer.tokenize(corpus.get(rkey));
			ArrayList<String> tmp = new ArrayList<String>();

			for (int j = 0; j < tokens.length; j++) {

				int lastChar = tokens[j].length() - 1;
				String newToken = "";

				// Return tokens that end with a symbol
				if (!Character.isLetter(tokens[j].charAt(lastChar)) || tokens[j].contains("(")) {
					newToken = tokens[j].replaceAll("[^a-zA-Z0-9+/-]", "");
					tmp.add(newToken.toLowerCase());
				} else {

					tmp.add(tokens[j].toLowerCase());
				}
			}

			tokenList.put(rkey, tmp);

		} 

		return tokenList;

	} 

	/*
	 * Removes stopwords from the list of tokens 
	 * Returns list of tokens without stopwords 
	 */
	public HashMap<String, ArrayList<String>> stopWordRemoval() {

		ArrayList<String> tokens = new ArrayList<String>();

		// for each key in the hashMap
		for (String rkey : tokenList.keySet()) {
			tokens = tokenList.get(rkey);
			for (int j = 0; j < tokens.size(); j++) {

				// à travers de chaque mot du cours i
				String word = tokens.get(j);

				// si c'est un stopword, enlève le mot
				if (isStopWord(word) == true) {

					// enlever stop word du arrayList
					tokens.remove(word);
				}
				tokenList.replace(rkey, tokens);
			}

		} // fin du loop
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
	 * Stemming the list of tokens 
	 */
	private HashMap<String, ArrayList<String>> stemming() {

		// declare and initialize PorterStemmer
		PorterStemmer tmp = new PorterStemmer();

		// declare and initialize stemword
		String stemword = "";
		ArrayList<String> tokens = new ArrayList<String>();

		// for each key in the hashMap
		for (String rkey : tokenList.keySet()) {
			tokens = tokenList.get(rkey);
			for (int j = 0; j < tokens.size(); j++) {

				// à travers de chaque mot du cours i
				String word = tokens.get(j);

				// stem the word using the built-inn stem function
				// si c'est un stopword, enlève le mot
				stemword = tmp.stem(word);

				// replace word by stemword in arrayList
				tokens.set(j, stemword);
				tokenList.replace(rkey, tokens);
			}
		}

		return tokenList;
	}

	/*
	 * Normalization of the list of tokens 
	 */
	private HashMap<String, ArrayList<String>> normalization() {

		String word = "";
		ArrayList<String> tokens = new ArrayList<String>();

		// for each key in the hashMap
		for (String rkey : tokenList.keySet()) {
			tokens = tokenList.get(rkey);
			for (int j = 0; j < tokens.size(); j++) {

				// à travers de chaque mot du cours i
				word = tokens.get(j);

				word = word.replaceAll("^\"|\"$|\\d|[(){}:;.,'&/<>-]", "");

				// replace word by normalized word in arrayList
				tokens.set(j, word);
			}
		}

		return tokenList;

	}

	// Get list of tokens for each document
	public HashMap<String, ArrayList<String>> gettokenList() {
		return tokenList;
	}

	/*
	 * Write entire preprocessed reuters to file 
	 * Site used:
	 * https://bukkit.org/threads/saving-loading-hashmap.56447/ 
	 * Modified from site
	 * to write a treemap to a text file
	 */


	private void write(TreeMap<String, String> input) {

		outputFile = new File(FILEPATH + OUTPUT);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (String s : input.keySet()) {
				bw.write(s + "-" + input.get(s));
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
		}
	}



	public static void main(String[] args) {
		MachineLearning ml = new MachineLearning();

		ml.kNN();
		ml.addToTopics();


	}

}
