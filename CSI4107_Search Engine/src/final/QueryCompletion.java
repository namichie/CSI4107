import java.util.ArrayList;

public class QueryCompletion {
	
	BigramModel bm;
	DictionaryBuilder db;
	ArrayList<String> bigram;

	public QueryCompletion(DictionaryBuilder db) {
		this.db = db;
		bm = new BigramModel(this.db);
	}
	
	private void suggestWord() {
		bigram = bm.getBigram();
	}
	
	public static void main(String[] args) {

	}

}
