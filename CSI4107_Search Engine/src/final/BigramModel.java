import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.TreeSet;

import opennlp.tools.ngram.NGramGenerator;


/*
 * https://opennlp.apache.org/docs/1.8.0/apidocs/opennlp-tools/opennlp/tools/ngram/NGramGenerator.html
 * https://opennlp.apache.org/docs/1.8.1/apidocs/opennlp-tools/opennlp/tools/ngram/NGramUtils.html
 */


/*
 * 1. Create bigrams from Reuters collection
 * 2. From 1st term in bigram, find all 2nd terms for that 1st term that appeared most frequently
 * 3. Pass those 2nd terms to query completion for the given 1st term
 */


public class BigramModel {
	
	DictionaryBuilder db;
	ArrayList<ArrayList<String>> tokenList;
	ArrayList<String> text, bigram, firstTerm, secondTerm;
	
	public BigramModel(DictionaryBuilder db) {
		this.db = db;
		tokenList = db.gettokenList();
		text = new ArrayList<String>();
		bigram = new ArrayList<String>();
		firstTerm = new ArrayList<String>();
		secondTerm = new ArrayList<String>();
		
		this.createBigram();
		this.getIndividualTerm(bigram);
		
	}
	
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
		
		//TODO: https://stackoverflow.com/questions/29656071/java-arraylist-remove-multiple-element-by-index
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
		
		//System.out.println(bigram);
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
		
		/*System.out.println("firstT:" + firstTerm);
		System.out.println("sT:" + secondTerm);*/
		
	}
	 
	// term (suggested word from 2ndterm) : freq
	public HashMap<String, Integer> calculateMostFrequent() {
		
		HashMap<String, Integer> suggested = new HashMap<String, Integer>();
		int freq = 0;
		String term = "";
		String[] arr = null;

		for (int i = 0; i < bigram.size(); i++) {
			
			arr = bigram.get(i).split(" ");
			
			// showers == showers
			// suggested words: continued, 
			//TODO- where to increment freq? and if there are duplicate words, how to add them to map
			if (arr[0].equals(firstTerm.get(i))) {
				term = secondTerm.get(i);
				freq++;
			}
			
			suggested.put(term, freq);

		}
		
		System.out.println("suggested: " + suggested);
		
		return suggested;
	}
	
	
	public ArrayList<String> getFirstTerm() {
		return firstTerm;
	}
	
	public ArrayList<String> getSecondTerm() {
		return secondTerm;
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
	
	//https://stackoverflow.com/questions/3807213/easiest-way-or-lightlest-library-to-get-bigrams-and-trigrams-in-java/54470395
	public static void main(String[] args) {
		DictionaryBuilder db = new DictionaryBuilder(true, true, false, 'r', "kNN");

		BigramModel b = new BigramModel(db);
		ArrayList<String> bb = b.getBigram();
		System.out.println(bb);
		//System.out.println(bb.size());
		b.getIndividualTerm(bb);
		
		
		ArrayList<String> f = b.getFirstTerm();
		System.out.println("1st: " + f);
		//System.out.println(f.size());
		
		ArrayList<String> s = b.getSecondTerm();
		System.out.println(s);
		//System.out.println("2nd: " + s.size());

		b.calculateMostFrequent();
		
		
	}
}
