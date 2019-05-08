import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class VSM {
	
	/*
	 * 
	 * Module 7a - Vector Space Model (Weight calculation
	Purpose: Include term weights in the index. 
	Input: Index. Collection of documents. (or only the index if the frequencies have been already included). 
	Output: A weighted index.
	
	* Module 7b - Vector Space Model (Retrieval)
	Purpose: Implement the Vector Space Model for retrieval
	Input 
		1. A query from the user expressed with a list of terms.
		2. Selected collection.
	Output A set of ranked document ids (corresponding to unique documents in your collection).
	*
	*/

	
	DictionaryBuilder db; //access dictionary
	
	
	//constructor
	VSM(boolean stopWord, boolean normalization, boolean stemming)  {
		db = new DictionaryBuilder(stopWord, normalization, stemming);
		
	}
	

	//return arrayList in descending order (highest to lowest VSM score)
	public ArrayList<HashMap<String, Double>> rankDocs(String query) {
		HashMap<String, Double> scoreList = rankDocsCalculation(query);
		ArrayList<HashMap<String, Double>> res = sortByValue(scoreList);
		return res;
	}
	
	
	//calculate the weight of each documents (sum product for each document)
	public HashMap<String, Double> rankDocsCalculation(String query) {

		String word, docIDKey; //key in first HashMap
		double scoreValue; //value in second HashMap
		
		String queryTokenList[] = query.split(" "); 
		
		//key: docID ; value: accumulate weight of the document
		HashMap<String, Double> docScore = new HashMap<String, Double>(); 
		
		//traverse each word
		for (int i = 0; i<queryTokenList.length; i++) {
			
			
			if (db.getweightList().containsKey(queryTokenList[i])) {
				
				//traverse the docID for the word i
				//returns second hashMap at the word i => db.getweightList().get(queryTokenList[i])
			    Iterator it = db.getweightList().get(queryTokenList[i]).entrySet().iterator();
			   
			    while (it.hasNext()) {
			        HashMap.Entry pair = (HashMap.Entry)it.next();
			        //System.out.println("docID Key: " + pair.getKey() + "  weight value: " + pair.getValue());
			        
			        docIDKey = pair.getKey().toString();
			        word = queryTokenList[i];
			        scoreValue = Double.parseDouble(pair.getValue().toString());
					
					//if docID does NOT exists in docWeight HashMap
					if (!docScore.containsKey(docIDKey)) {
						
						//key: docID ; value: accumulate weight of the document
						docScore.put(docIDKey, scoreValue);
					
					} //end of if
					
					
					//if docID already exists in docWeight HashMap
					else {
						
						//update the weight value
						scoreValue = scoreValue + docScore.get(docIDKey);
						
						//key: docID ; value: accumulate weight of the document
						docScore.replace(docIDKey, scoreValue);
						
					} //end of else
			    
			    } //end of while loop
			} // end of if statement
		
		} //end of for loop
		
		
		//sort the HashMap in descending order (highest to lowest score)
		//docScore = sortByValue(docScore);
		
		return docScore;
	} //end of rankDocsCalculation
	
	
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
}
