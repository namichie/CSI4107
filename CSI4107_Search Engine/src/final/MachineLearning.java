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
	protected HashMap<String, ArrayList<String>> topictodocID; // Hashmap of topic to list of docIDs
	protected HashMap<String, ArrayList<String>> topictodocID_NB; // Hashmap of training set topic to list of docIDs


	protected HashMap<String, ArrayList<String>> trainingSet; // Training set (docID, list of tokens)
	protected HashMap<String, ArrayList<String>> testSet; // Test set (docID, list of tokens)


	protected TreeMap<String, ArrayList<String>> assignedTopics; // result of classification (docID, list of topics)
	protected TreeMap<String, String> unassignedDocs; // docs with no topics (docID, N/A)

	// WRITE TO FILE
	protected ArrayList<String> content;
	protected TreeMap<String, String> output;

	protected File outputFile;
	private final String FILEPATH = System.getProperty("user.dir");
	private final String STOPWORDS = File.separator + "stopwords.txt"; // stopword file
	private final String OUTPUT = File.separator + "assigned_reuters_output.txt"; //text file output
	private final String dirKNN = File.separator + "kNN"; //kNN folder
	private final String dirNB = File.separator + "Naive_Bayes"; // NB folder

	// Algo - kNN or NB
	public MachineLearning(String algo) {

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

		topictodocID = new HashMap<String, ArrayList<String>>();	
		topictodocID_NB = new HashMap<String, ArrayList<String>>();	

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

		try {
			this.assignTopicTodocID();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.assignTopicTodocID_NB();

		if (algo.equals("kNN")) {
			kNN();

		} else if (algo.equals("NB")) {
			NaiveBayes();
		}

	}

	/*
	 * Sites used:
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

								topicsList.add(t[i]);

							}
						}

						// Add title
						title = text.substring(endTopic + 1, endTitle);

						// Add body
						description = text.substring(endTitle + 3);

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




	//------------------------------ kNN ---------------------------------
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

		write(output, "kNN");

	} // end of k-NN function




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

		Set<String> tokenSet_docA = new HashSet<String>(); // all tokens corresponding docA
		Set<String> tokenSet_docB = new HashSet<String>(); // all tokens corresponding docB
		Set<String> intersection, union; // intersection and union sets

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
		intersection = new TreeSet<String>(tokenSet_docA);
		intersection.retainAll(tokenSet_docB);

		// determine union set
		union = new TreeSet<String>(tokenSet_docA);
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

		// Get list of topics
		TreeMap<String, ArrayList<String>> t = topics;

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

	//------------------------------ Naive Bayes ---------------------------------
	private void NaiveBayes() {
		HashMap<String, ArrayList<String>> test = getTestSet();
		HashMap<String, ArrayList<String>> training = getTrainingSet();

		//1. Calculate priors
		HashMap<String, Double> priors = calculatePriors(training);

		//2. Calculate posterior
		HashMap<String, HashMap<String, Double>> posteriors = calculatePosteriors();

		//3. Determine class
		determineClass(priors, posteriors, test);

		//4 Output results in Naive Bayes' folder

		// Reassign topics and write classified topics to file
		TreeMap<String, ArrayList<String>> t = addToTopics();

		// Combine topics + title + body
		for (String key : t.keySet()) {
			String topicString = String.join(" ", t.get(key));

			content.add(topicString + ":" + titles.get(key) + "---" + corpus.get(key));
			System.out.println(topicString + ":" + titles.get(key) + "---" + corpus.get(key));

			// Add document to treemap
			for (int j = 0; j < content.size(); j++) {

				output.put(key, content.get(j));
			}

		}
		write(output, "NB");

	}


	/* 
	 * String: topic
	 * Double: prior probability
	 * */
	private HashMap<String, Double> calculatePriors(HashMap<String, ArrayList<String>> training) {
		HashMap<String, Double> priors = new HashMap<String, Double>();

		int topic_num = 0;
		int tot_docs = training.size(); //dénominateur - total number of docs

		if (tot_docs>0) {
			//loop through each topics 
			for (String topic : topictodocID_NB.keySet()) {

				//System.out.println("topic prior: " + topic);

				if (!topic.equals("N/A")) {
					topic_num = topictodocID_NB.get(topic).size();
					if (topic_num!=0) {
						double res = (double)topic_num/(double)tot_docs;
						priors.put(topic, res);
					}
				}
			} //end of for loop
		} //end of if statement
		return priors;

	} //end of function calculatePriors



	/* 
	 * String: topic
	 * HashMap: String:word ; Double: posterior probability
	 * */
	private HashMap<String, HashMap<String, Double>> calculatePosteriors() {
		HashMap<String, HashMap<String, Double>> posteriors = new HashMap<String, HashMap<String, Double>>();
		int tmp;
		String doc;

		HashMap<String, Double> freq = new HashMap<String, Double>();
		ArrayList<String> tokensInDoc = new ArrayList<String>();
		HashMap<String, Double> value = new HashMap<String, Double>();
		int total_tokens_per_topic; //dénominateur

		//loop through each topics and the
		for (String topic : topictodocID_NB.keySet()) {
			total_tokens_per_topic = 0; //reinitialize
			value = new HashMap<String, Double>(); //reinitialize
			freq = new HashMap<String, Double>(); //reinitialize

			if (!topic.equals("N/A")) {

				//loop through all documents corresponding to that topic 
				tmp = topictodocID_NB.get(topic).size(); //nombre de doc

				//loop through all documents of that topic
				for(int j=0; j<tmp;j++) {

					//if there's at least one document, then put words in uniqueTokensPerTopic set
					if(tmp>0) {

						doc = topictodocID_NB.get(topic).get(j); //doc j for topic i

						if(tokenList.containsKey(doc)) {

							tokensInDoc = tokenList.get(doc);
							total_tokens_per_topic = total_tokens_per_topic + tokensInDoc.size(); //dénominateur

							//update freq HashMap
							for(int k=0; k<tokensInDoc.size();k++) {
								String word = tokensInDoc.get(k);

								if(freq.containsKey(word)) {
									freq.replace(word, freq.get(word)+1.0); //incrémenter
								} else {
									freq.put(word, 1.0); //add to hashMap
								}
							}

						}	
					} //end of if statement
				} //end of for loop j - fin de looper through all documents of that topic

				//calculer les posteriors
				for (String word : freq.keySet()) {
					freq.replace(word, ((double)freq.get(word)/(double)total_tokens_per_topic)+0.1); //divide freq by total words for that topic
				}

				posteriors.put(topic, freq); //add posterior probabilities for that topic
			}

		} //end of for loop per topics

		return posteriors;
	}


	/* 
	 * priors, posteriors, test set
	 * 
	 * Priors:
	 * 		String: topic
	 * 		Double: prior probability
	 * 
	 * Posteriors:
	 * 		String: topic
	 * 		HashMap: String:word ; Double: posterior probability
	 * */
	private void determineClass(HashMap<String, Double> priors, HashMap<String, HashMap<String, Double>> posteriors, 
			HashMap<String, ArrayList<String>> test) {

		double prob, maxProb; //default value
		String maxTopic;
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<String> topicsList = new ArrayList<String>();

		//loop through all documents in the test set
		for (String docID : test.keySet()) {

			prob = -100.0; //reinitialize
			maxProb = -1000.0; //reinitialize
			maxTopic = "";
			tokens = test.get(docID); //get all tokens for that document

			//loop through all topics to find max probability
			for (String topic : posteriors.keySet()) {

				//loop through all tokens of docID
				for(int i=0; i<tokens.size(); i++) {

					String word = tokens.get(i); //mot

					if(posteriors.containsKey(topic)) { //sujet existe

						if (posteriors.get(topic).containsKey(word)) { //mot existe
							if(prob == -100.0) {
								prob = posteriors.get(topic).get(word);
							} else {
								prob = prob*posteriors.get(topic).get(word);
							}					
						} else { //mot n'existe pas
							if(prob == -100.0) {
								prob = 0.1;
							} else {
								prob = prob*0.1; //smoothing cuz word does not exist in hashMap
							}	
						}

					}

				} //end of loop through all tokens of docID

				if(priors.containsKey(topic)) {
					prob = prob*priors.get(topic); //multiply by prior of topic
				} else {
					prob = 0;
				}

				//compare maxProb
				if(maxProb<prob) {
					maxProb = prob;
					maxTopic = topic;

					// Get docID of maxTopic to get list of topics
					ArrayList<String> id = topictodocID_NB.get(maxTopic);

					topicsList = topics.get(id.get(0));
				}


			} //end of loop through all topics

			assignedTopics.put(docID, topicsList);

		} //end of loop through all documents in the test set
		System.out.println(assignedTopics);

	} //end of determineClass function


	// Given a topic, creates list of docIDs with this topic for training set
	private void assignTopicTodocID_NB() {
		ArrayList<String> topicsList = new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>();

		HashMap<String, ArrayList<String>> train = getTrainingSet();


		// Get list of topics
		TreeMap<String, ArrayList<String>> t = topics;

		for (String id : t.keySet()) {

			// if training set has same key as topic
			if (train.containsKey(id)) {
				topicsList.addAll(t.get(id));
			}
		}


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

			topictodocID_NB.put(topic, list);
		}
		System.out.println(topictodocID_NB);
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


	private void write(TreeMap<String, String> input, String algo) {

		if (algo.equals("kNN")) {
			outputFile = new File(FILEPATH + dirKNN + OUTPUT);
		} else if (algo.equals("NB")) {
			outputFile = new File(FILEPATH + dirNB + OUTPUT);
		}

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
		MachineLearning ml = new MachineLearning("NB");
		//HashMap<String, ArrayList<String>> training = ml.getTrainingSet(); 
		//ml.NaiveBayes();
		//ml.assignTopicTodocID_NB();
		/*ml.calculatePriors(training);
		ml.kNN();
		ml.addToTopics();*/


	}

}
