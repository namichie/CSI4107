package baseline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.stemmer.PorterStemmer;
//import opennlp.tools.util.normalizer.;

//  https://stackoverflow.com/questions/29061782/java-read-txt-file-to-hashmap-split-by
/*Dict. building steps:
 * Tokenization
 * Stemming
 * Normalization
 * Stopword
 * Return: terms to be indexed
 * 
 */
public class DictionaryBuilder {
	Preprocessor p;
	HashMap<String, String> corpus;
	ArrayList<ArrayList<String>> tokenList; 

	DictionaryBuilder() {

		// Instantiate Module 1
		p = new Preprocessor();
		corpus = new HashMap<String, String>();
		tokenList = new ArrayList<ArrayList<String>>();

	}

	/**
	 * Read formatted corpus for further processing
	 */
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
					String courseDescription = "";
					int start = text.indexOf(":");
					if (text.contains(":")) {
						courseDescription = text.substring(start + 1);
					} else {
						courseDescription = text;
					}
					corpus.put(docID, courseDescription);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, String> getCorpus() {
		return corpus;
	}

	private ArrayList<ArrayList<String>> tokenize() {

		// Tokenize
		WhitespaceTokenizer wst = WhitespaceTokenizer.INSTANCE;
		String[] tokens = null;
		// Get text only
		String[] t = getCorpus().values().toArray(new String[0]);

		// Tokenize for each course
		for (int i = 0; i < t.length; i++) {

			//tokens: tous les mots 'tokenized' le cours i
			tokens = wst.tokenize(t[i]);

			ArrayList<String> tmp = new ArrayList<String>();

			for (int j = 0; j < tokens.length; j++) {
				tmp.add(tokens[j]);
			}
			tokenList.add(i, tmp);
		}
		System.out.println("Original tokens -: " + tokenList.get(0));
		/*System.out.println("Original tokens -: " + tokenList.get(4));

		System.out.println("Original tokens ." + tokenList.get(101));*/ //TODO - tokenizes A.I. as A, ., I...

		return tokenList;

	}

	//TODO - think of other stopwords to add to list related to courses

	private ArrayList<ArrayList<String>> stopWordRemoval(boolean stopword) {
		// Remove stopwords in the list (tokens) within the AccessList (tokenList)

		//À travers de le arrayList
		for (int i = 0; i < tokenList.size(); i++) {

			//À travers du arrayList à l'intérieur du ArrayList
			//tokens du cours i
			for (int j = 0; j < tokenList.get(i).size(); j++) {

				//à travers de chaque mot du cours i
				String word = tokenList.get(i).get(j);

				//si c'est un stopword, enlève le mot
				if (isStopWord(word)==true) {

					//enlever stop word du arrayList
					tokenList.get(i).remove(word);
				}					
			}
		}
		System.out.println("No stop words: " + tokenList.get(0));

		return tokenList;
	}

	// https://coderanch.com/t/631347/java/Search-word-text-file

	private boolean isStopWord(String word) {

		FileReader inputFile;
		try {
			inputFile = new FileReader(
					"C:\\Users\\viet_\\Documents\\CSI\\4TH YR 2018-2019\\Winter 2019\\CSI4017_Search Engine\\src\\baseline\\stopwords.txt");
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

	private ArrayList<ArrayList<String>> stemming(boolean stem) {

		//declare and initialize PorterStemmer
		PorterStemmer tmp = new PorterStemmer();

		//declare and initialize stemword
		String stemword = "";

		//À travers de le arrayList
		for (int i = 0; i < tokenList.size(); i++) {

			//À travers du arrayList à l'intérieur du ArrayList
			//tokens du cours i
			for (int j = 0; j < tokenList.get(i).size(); j++) {

				//à travers de chaque mot du cours i
				String word = tokenList.get(i).get(j);

				//stem the word using the built-inn stem function
				//si c'est un stopword, enlève le mot
				stemword = tmp.stem(word);

				//replace word by stemword in arrayList
				tokenList.get(i).set(j, stemword);					
			}
		}
		System.out.println("Stemmed: " + tokenList.get(0));
		/*System.out.println("Stemmed: " + tokenList.get(4));
		System.out.println("Stemmed: " + tokenList.get(101));*/
		return tokenList;
	}


	private ArrayList<ArrayList<String>> normalization(boolean norm) {

		String normWord = "";

		//À travers de le arrayList
		for (int i = 0; i < tokenList.size(); i++) {

			//À travers du arrayList à l'intérieur du ArrayList
			//tokens du cours i
			for (int j = 0; j < tokenList.get(i).size(); j++) {

				//à travers de chaque mot du cours i
				String word = tokenList.get(i).get(j);

				// Normalize token

				//normWord = word.replaceAll("[.-]", "");


				//replace word by stemword in arrayList
				//tokenList.get(i).remove(j);	

			}
			//System.out.println(normWord);
		}

		System.out.println("Norm: " + tokenList.get(0));

		return tokenList;
	}

	public static void main(String[] args) throws FileNotFoundException {
		Preprocessor p = new Preprocessor();
		DictionaryBuilder db = new DictionaryBuilder();
		File file = p.getOutputFile();
		db.read(file);
		db.tokenize();
		//db.stopWordRemoval(true);
		db.stemming(true);
		//db.normalization(true);


	}
}
