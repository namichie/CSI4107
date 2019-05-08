import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import opennlp.tools.stemmer.PorterStemmer;

public class QueryPreprocessor {
	
	private String[] queryList; // Split query into a list of tokens 
	private final String FILEPATH = System.getProperty("user.dir"); //dir of project saved
	private final String OUTPUT = File.separator + "stopwords.txt"; //name of text file


	/*
	 * Returns stemmed query
	 */
	private String stemming(String qry) {
		queryList = qry.split(" ");
		String result = "";

		PorterStemmer tmp = new PorterStemmer();

		String stemword = "";

		for (int i = 0; i < queryList.length; i++) {
			String word = queryList[i];

			// Not bool operator
			if (!word.equals("AND") || !word.equals("OR") || !word.equals("AND_NOT")) {

				stemword = tmp.stem(word);
				queryList[i] = stemword;

			} else {
				queryList[i] = word;
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String s : queryList) {
			sb.append(s);
			sb.append(" ");
		}
		result = sb.toString();

		return result;

	}


	/*
	 * Returns normalized query
	 */
	private String normalization(String qry) {

		queryList = qry.split(" ");
		String result = "";
		String normword = "";

		for (int i = 0; i < queryList.length; i++) {
			String word = queryList[i];

			// Not bool operator
			if (!word.equals("AND") || !word.equals("OR") || !word.equals("AND_NOT")) {

				normword = word.replaceAll("[(){}:;.,'&/<>-]", "");
				queryList[i] = normword;

			} else {
				queryList[i] = word;
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String s : queryList) {
			sb.append(s);
			sb.append(" ");
		}
		result = sb.toString();

		return result;

	}

	/*
	 * Returns and removes stopwords from a query
	 */
	private String stopWordRemoval(String qry) {

		queryList = qry.split(" ");
		String stopword = "";
		String result = "";

		ArrayList<String> qlist = new ArrayList<String>(Arrays.asList(queryList));

		for (int i = 0; i < qlist.size(); i++) {

			String word = qlist.get(i);

			// Check if word in query is a stopword and remove
			if (isStopWord(word) == true) {
				stopword = word;
				qlist.remove(stopword);
				
			}
		}
		
		queryList = qlist.toArray(new String[0]);
		
		StringBuilder sb = new StringBuilder();
		for (String s : queryList) {
			sb.append(s);
			sb.append(" ");
		}
		result = sb.toString();

		return result;

	}

	/*
	 * Copied from website: https://coderanch.com/t/631347/java/Search-word-text-file 
	 * Used snippet of code provided in forum to read a text file that contains a stopword
	 */
	private boolean isStopWord(String word) {

		FileReader inputFile;
		try {
			
			inputFile = new FileReader(FILEPATH + OUTPUT);

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

	/* Getters */
	public String getStemmedQuery(String q) {
		return stemming(q);
	}
	
	public String getNormalizedQuery(String q) {
		return normalization(q);
	}
	
	public String getNoStopWordQuery(String q) {
		return stopWordRemoval(q);
	}
}
