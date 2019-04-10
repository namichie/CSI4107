import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	
	/*
	Thesaurus(boolean stopword, boolean normalization, boolean stemming) throws IOException {
		db = new DictionaryBuilder(stopword, normalization, stemming, 'r');
		//t = this.build_Automatic_Thesaurus();
		dictionary = new ArrayList<String>();
		invertedIndex = new HashMap<String, ArrayList<String>>();
		this.getDictionary();
		this.getInvertedIndex();
	} */
	
	Thesaurus(DictionaryBuilder db) {
		this.db = db;
		dictionary = new ArrayList<String>();
		uniqueTokens = db.getExternalDictionary_reuter();
		invertedIndex = new HashMap<String, ArrayList<String>>();
		keytodocID = db.getInvertedIndex();
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
		System.out.println("size: " + tot_tokens);

		
		Set tokenSet; // set of 2 words
		double similarity; // similarity between the 2 words in the set
		String tokenA, tokenB;
		
		//find the similarity between each words in each document
		for (int i=0; i<tot_tokens; i++) {
			tokenA = uniqueTokens.get(i);
			
			for (int j=0; j<tot_tokens; j++) {
				tokenB = uniqueTokens.get(j);
				tokenSet = new HashSet(); //reinitialize the set before adding it in the thesaurus
				tokenSet.add(tokenA); // add word at position i
				tokenSet.add(tokenB); // add word at position j
				
				//If the docset exists in thesaurus, which contains 2 docA and docB, then we already calculatd the similarity between doc i and doc j
				if (!thesaurus.containsKey(tokenSet)) {
					
					//IF token i == token j, THEN similarity = 1
					if (tokenA == tokenB) {
						thesaurus.put(tokenSet, 1.0);
					} 
					
					//ELSE Jaccard: |A and B|/|A or B| = |DOCi and DOCj|/|DOCi or DOCj|
					else {
						similarity = Jaccard_Similarity(tokenA, tokenB); // calculate the similarity
						thesaurus.put(tokenSet, similarity); //add to thesaurus
					} //end of else
					
				} //end of of statement
			System.out.println("i,j: " + i + " "+ j);	
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
		
		Set tokenSet_docA = new HashSet(); // all docID corresponding to token A
		Set tokenSet_docB = new HashSet(); // all docID corresponding to token A
		Set intersection, union; // intersection and union sets
		

		// put all docID containing tokenA in a set, this will eliminate the duplicates
		for (int i=0; i<docIDA.size(); i++) {
			tokenSet_docA.add(docIDA.get(i));
		}
		
		// put all docID containing tokenB in a set, this will eliminate the duplicates
		for (int i=0; i<docIDB.size(); i++) {
			tokenSet_docB.add(docIDB.get(i));
		}
		
		
		// determine intersection set
	    intersection = new TreeSet(tokenSet_docA);
	    intersection.retainAll(tokenSet_docB);	
	    
	    // determine union set
		union = new TreeSet(tokenSet_docA);
	    union.addAll(tokenSet_docB);	
	    
	    //Calculate Jaccard similarity
	    double similarity = (double) intersection.size()/(double) union.size();
	    
	    return similarity;
		
	}
	
	
	//find the most similar word with the parameter of this function
	public String getMaxSimilarity(String word) {
		String res = "";
		String tmp;
		Set hmKey = new HashSet();
		Double sim = 0.0;
		//ArrayList<String> uniqueTokens = getDictionary(); // list of unique tokens
		
		//loop trough all the distinct words
		for (int i=0; i<uniqueTokens.size(); i++) {
			
			//word at position i
			tmp = uniqueTokens.get(i);
			hmKey.add(word);
			hmKey.add(tmp);
			
			if (t.containsKey(hmKey) && word!=tmp && tmp!="") {
				//value of key is greater then current similarity
				if (sim<t.get(hmKey)) {
					sim = t.get(hmKey); //update similarity
					res = tmp; //most similar token to word
				}	
			}
		}
		
		return res;
	}
	
	
	// TEST
	public static void main(String[] args) throws IOException {

		/*HashMap<String, Double> tmp = new HashMap<String, Double>();
		tmp.put("a", 1.0);
		tmp.put("cat", 22.0);
			
		System.out.println(tmp.containsKey("cats"));*/
		//HashMap<Set, Double> test = t.build_Automatic_Thesaurus();
		
		/*double k = t.Jaccard_Similarity("brazil", "china");
		System.out.println(k);*/
		//t.getDictionary();
		//t.getInvertedIndex();
	}
	
}
