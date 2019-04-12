
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;

public class MainPage extends JFrame {

	/*
	 * 
	 * Module 2 - User Interface
	Purpose: Allow a user to access the search engine capabilities.
	Input: None.
	Output: Set of choices + query string.

	 * Module 5 - Corpus access
	Purpose: Access documents from the corpus.
	Input: Set of document IDs.
	Output: Set of corresponding documents (including title, excerpt line, link to full content).
	 *
	 */

	private JFrame frame;
	private JTextField query;
	private JTable table;
	Boolean_Model bmodel;
	VSM vsm;
	DictionaryBuilder reutersDB;
	String[] topicList = getTopicList();
	Probabilistic pmodel;
	QueryPreprocessor qprocessor  = new QueryPreprocessor();
	HashMap<String, HashMap<String, Integer>> queryMemory = new HashMap<String, HashMap<String, Integer>>(); 


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainPage window = new MainPage();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public MainPage() throws IOException {
		initialize();
		Probabilistic readFile = new Probabilistic();

		//if text file exists, then add info in queryMemory
		if (readFile.isqueryMemoryExist()) {
			queryMemory = readFile.readFile();
		}

		//if text file does NOT exists, then create file in project directory
		else {
			readFile.writeToFile(queryMemory);
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Vanilla Search Engine");
		frame.getContentPane().setForeground(Color.BLACK);
		frame.setBounds(100, 100, 900, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblSearchEngine = new JLabel("Search Engine");
		lblSearchEngine.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSearchEngine.setForeground(Color.BLACK);
		lblSearchEngine.setBounds(29, 36, 285, 19);
		frame.getContentPane().add(lblSearchEngine);

		JLabel lblQuery = new JLabel("Query:");
		lblQuery.setBounds(27, 82, 49, 14);
		frame.getContentPane().add(lblQuery);

		query = new JTextField();
		query.setHorizontalAlignment(SwingConstants.LEFT);
		query.setBounds(69, 79, 671, 20);
		frame.getContentPane().add(query);
		query.setColumns(10);

		JButton btnSearch = new JButton("Search");
		JCheckBox stopWordAns = new JCheckBox("Stopword removal");
		stopWordAns.setBounds(29, 184, 142, 25);
		frame.getContentPane().add(stopWordAns);

		JCheckBox stemmingAns = new JCheckBox("Stemming");
		stemmingAns.setBounds(29, 217, 113, 25);
		frame.getContentPane().add(stemmingAns);

		JCheckBox normalizationAns = new JCheckBox("Normalization");
		normalizationAns.setBounds(29, 250, 113, 25);
		frame.getContentPane().add(normalizationAns);

		JLabel lblTypeOfModel = new JLabel("Type of Model:");
		lblTypeOfModel.setBounds(29, 141, 89, 14);
		frame.getContentPane().add(lblTypeOfModel);

		JComboBox modelType = new JComboBox();
		modelType.setModel(new DefaultComboBoxModel(new String[] {"Boolean", "VSM", "Probabilistic", "Bigram"}));
		modelType.setBounds(128, 137, 118, 22);
		frame.getContentPane().add(modelType);


		JLabel lblDocumentCollection = new JLabel("Document Collection:");
		lblDocumentCollection.setBounds(320, 141, 130, 14);
		frame.getContentPane().add(lblDocumentCollection);

		JComboBox docCollection = new JComboBox();
		docCollection.setModel(new DefaultComboBoxModel(new String[] {"U of O courses", "Reuters"}));
		docCollection.setBounds(454, 137, 118, 22);
		frame.getContentPane().add(docCollection);

		JComboBox classifier = new JComboBox();
		classifier.setModel(new DefaultComboBoxModel(new String[] {"k-NN", "Naive Bayes"}));
		classifier.setBounds(734, 137, 124, 22);
		frame.getContentPane().add(classifier);

		JLabel lblClassifierResults = new JLabel("Classifier Results:");
		lblClassifierResults.setBounds(611, 141, 113, 14);
		frame.getContentPane().add(lblClassifierResults);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(29, 333, 827, 319);
		frame.getContentPane().add(scrollPane);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(320, 188, 285, 104);
		frame.getContentPane().add(scrollPane_1);

		JList list = new JList(topicList);
		list.setBounds(320, 188, 285, 104);
		scrollPane_1.setViewportView(list);


		String[] columnNames = {"Doc ID", "Title", "Excerpt", "Score", "Topics"};
		Object[][] data = {
		};

		//Bouton "Search" - execute query
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				ArrayList<String> docIDList;
				boolean stopWord, stemming, normalization;
				String qryInput = query.getText(); //by default, keep user input

				//modification to query (user input) if boxes are checked
				//remove stop words
				if (docCollection.getSelectedItem().toString().equals("U of O courses")) {
					if (stopWordAns.isSelected()) { 
						stopWord = true; 
						qryInput = qprocessor.getNoStopWordQuery(qryInput);	

					} else { 
						stopWord = false; 
					}

					//stem words
					if (stemmingAns.isSelected()) { 
						stemming = true; 
						qryInput = qprocessor.getStemmedQuery(qryInput); 

					} else { 
						stemming = false; 
					}

					//normalize words
					if (normalizationAns.isSelected()) { 
						normalization = true; 
						qryInput = qprocessor.getNormalizedQuery(qryInput);

					} else { 
						normalization = false; 

					}
				} else {
					/*
					qryInput = qprocessor.getNoStopWordQuery(qryInput);
					qryInput = qprocessor.getStemmedQuery(qryInput); 
					qryInput = qprocessor.getNormalizedQuery(qryInput); */
					stopWord = true; 
					stemming = false;
					normalization = true; 
				}

				//Classifier: kNN or NB
				if (classifier.getSelectedItem().toString().equals("k-NN")) {
					reutersDB = new DictionaryBuilder(true,true,false,'r', "kNN");
				} else if (classifier.getSelectedItem().toString().equals("Naive Bayes")) {
					reutersDB = new DictionaryBuilder(true,true,false,'r', "NB");
				}
				
				//BOOLEAN MODEL
				if (modelType.getSelectedItem().toString().equals("Boolean")) {
					
					if (docCollection.getSelectedItem().toString().equals("U of O courses")) {
						//créer dictionnaire avec les valeurs
						bmodel = new Boolean_Model(stopWord, normalization, stemming);
						
					} else if (docCollection.getSelectedItem().toString().equals("Reuters")) {
						bmodel = new Boolean_Model(reutersDB); 
						
					}

					if (qryInput.contains("*")) {
						//convert input into standard infix format without wildcard
						qryInput = bmodel.wildcardToInfixFormat(qryInput);
					}
				
					//multiple words with AND/OR/AND_NOT operand
					if (totalNumWord(qryInput)==1) {

						//from the boolean model, get the list of course ID to display
						docIDList = filterDocbyTopic(bmodel.singleWordQuery(qryInput), list, reutersDB, docCollection.getSelectedItem().toString());

					} else {
						docIDList = filterDocbyTopic(bmodel.postfixEval(bmodel.infixToPostfix(qryInput)), list, reutersDB, docCollection.getSelectedItem().toString());
					}

					String[][] description = new String[docIDList.size()][5];
					String topic = "";
					//get the list of description for each course ID
					for (int i = 0; i<docIDList.size(); i++) {
						
						if (docCollection.getSelectedItem().toString().equals("U of O courses")) {
							topic = "";
						} else {
							topic = bmodel.db.getTopic(docIDList.get(i));
						}
						description[i][0] = docIDList.get(i); //courseID
						description[i][1] = bmodel.db.getTitle(docIDList.get(i)); //title
						description[i][2] = bmodel.db.getDescription(docIDList.get(i)); //description
						description[i][4] = topic; 

						
					}

					//update table
					table.setModel(new DefaultTableModel(description, columnNames));
					table.getColumnModel().getColumn(0).setPreferredWidth(100);
					table.getColumnModel().getColumn(0).setMinWidth(100);
					table.getColumnModel().getColumn(0).setMaxWidth(200);
					scrollPane.setViewportView(table);

				}

				//VSM MODEL-------------------------------------------------------------
				if (modelType.getSelectedItem().toString().equals("VSM")) {

					if (docCollection.getSelectedItem().toString().equals("U of O courses")) {
						//créer dictionnaire avec les valeurs
						vsm = new VSM(stopWord, normalization, stemming);
						
					} else if (docCollection.getSelectedItem().toString().equals("Reuters")) {
						vsm = new VSM(reutersDB); 
						qryInput = vsm.queryExpansion(qryInput);
					}
					
					
					ArrayList<HashMap<String, Double>> res = vsm.rankDocs(qryInput);
					docIDList = filterDocbyTopic(vsm.getDocIDs(res), list, reutersDB, docCollection.getSelectedItem().toString());
					ArrayList<Double> scores = vsm.getScore(res);

					String[][] description = new String[docIDList.size()][5];

					String topic = "";
					//get the list of description for each course ID
					for (int i = 0; i<docIDList.size(); i++) {
						
						if (docCollection.getSelectedItem().toString().equals("U of O courses")) {
							topic = "";
						} else {
							topic = vsm.db.getTopic(docIDList.get(i));
						}
						
						description[i][0] = docIDList.get(i); //courseID
						description[i][1] = vsm.db.getTitle(docIDList.get(i));
						description[i][2] = vsm.db.getDescription(docIDList.get(i));
						description[i][3] = scores.get(i).toString();
						description[i][4] = topic; 
					}

					//update table
					table.setModel(new DefaultTableModel(description, columnNames));
					table.getColumnModel().getColumn(0).setPreferredWidth(100);
					table.getColumnModel().getColumn(0).setMinWidth(100);
					table.getColumnModel().getColumn(0).setMaxWidth(200);
					scrollPane.setViewportView(table);
				}

				//Probabilistic MODEL-------------------------------------------------------------
				if (modelType.getSelectedItem().toString().equals("Probabilistic")) {
					ArrayList<Double> scores_Prob;

					//user wrote query previously and opened document, use RSV scoring
					if (queryMemory.containsKey(qryInput)) {
						pmodel = new Probabilistic(queryMemory, stopWord, normalization, stemming);
						ArrayList<HashMap<String, Double>> tmp_RSV = pmodel.rank_RSV(qryInput);
						docIDList = pmodel.rank_RSV_Doc(tmp_RSV);
						scores_Prob = pmodel.rank_RSV_Scores(tmp_RSV);
					}

					//query not in memory, use idf scoring
					else {
						pmodel = new Probabilistic(stopWord, normalization, stemming);
						ArrayList<HashMap<String, Double>> tmp_IDF = pmodel.rank_IDF(qryInput);
						docIDList = pmodel.rank_IDF_Doc(tmp_IDF);
						scores_Prob = pmodel.rank_IDF_Scores(tmp_IDF);
					}

					String[][] description = new String[docIDList.size()][4];

					//get the list of description for each course ID
					for (int i = 0; i<docIDList.size(); i++) {
						description[i][0] = docIDList.get(i); //courseID
						description[i][1] = pmodel.db.getTitle(docIDList.get(i));
						description[i][2] = pmodel.db.getDescription(docIDList.get(i));
						description[i][3] = scores_Prob.get(i).toString();
					}

					//update table
					table.setModel(new DefaultTableModel(description, columnNames));
					table.getColumnModel().getColumn(0).setPreferredWidth(100);
					table.getColumnModel().getColumn(0).setMinWidth(100);
					table.getColumnModel().getColumn(0).setMaxWidth(200);
					scrollPane.setViewportView(table);
				}


				//Bigram MODEL-------------------------------------------------------------
				if (modelType.getSelectedItem().toString().equals("Bigram")) {

				}
			}
		});

		btnSearch.setBounds(750, 78, 106, 23);
		frame.getContentPane().add(btnSearch);

		//empty table when user runs main page (UI)
		table = new JTable(data, columnNames);
		table.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"Doc ID", "Title", "Excerpt", "Score", "Topics"
				}
				));
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(200);
		table.setBounds(29, 333, 827, 319);	
		scrollPane.setViewportView(table);


		JButton btnViewDetails = new JButton("View Details");

		//Bouton "View Details" - open Description.java
		btnViewDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Description d = new Description();
				d.openWindow(); //open description form
				String description = ""; //description in new description window

				//docID selected by user
				String docID = table.getModel().getValueAt(table.getSelectedRow(), 0).toString();

				//get description of docID
				//each model calls their description function
				if (modelType.getSelectedItem().toString().equals("Boolean")) {
					description = bmodel.db.getDescription(docID);
				}

				if (modelType.getSelectedItem().toString().equals("VSM")) {
					description = vsm.db.getDescription(docID);
				}

				if (modelType.getSelectedItem().toString().equals("Probabilistic")) {
					description = pmodel.db.getDescription(docID);
					addToQueryMemory(query.getText(), docID); //save opened docID to the query
					pmodel.writeToFile(queryMemory); //add it to text file
				}

				d.descriptionTxt.setText(description);
			}
		});
		btnViewDetails.setBounds(719, 299, 137, 23);
		frame.getContentPane().add(btnViewDetails);

		JLabel lblpleaseLeaveA = new JLabel("(Please leave a space between each word/parentheses)");
		lblpleaseLeaveA.setBounds(29, 58, 347, 14);
		frame.getContentPane().add(lblpleaseLeaveA);
		
		JButton btnViewThesaurus = new JButton("View Thesaurus");
		btnViewThesaurus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Popup code: JOptionPane.showMessageDialog(null, "Please select Reuters", "InfoBox: automatic thesaurus visualization", JOptionPane.INFORMATION_MESSAGE);
				
				String[] uniqueTokens = getDictionary(reutersDB.getDictionary());
				VisualizeThesaurus v = new VisualizeThesaurus(uniqueTokens, reutersDB);
				v.openWindow(); //open description form
				
				
			}
		});
		btnViewThesaurus.setBounds(719, 269, 137, 23);
		frame.getContentPane().add(btnViewThesaurus);

	}

	//HashMap<String, HashMap<String, Integer>>
	//add to queryMemory docID and number of times user opened document to the query
	public void addToQueryMemory(String query, String docID) {

		//HashMap inside the first hashmap
		HashMap<String, Integer> docIDFreq = queryMemory.get(query);

		//if query exists
		if (queryMemory.containsKey(query)) {

			// if docID exists
			if (docIDFreq.containsKey(docID)) {
				//update the number of times user opened document
				Integer freq = getFrequency(query, docID) + 1;
				queryMemory.get(query).replace(docID, freq);
			}

			// if docID does NOT exists
			else {
				queryMemory.get(query).put(docID, 1); //1: first time doc has been viewed by user
			}
		}

		//if query does NOT exists
		else {
			HashMap<String,Integer> newHM = new HashMap<String,Integer>();
			newHM.put(docID, 1);
			queryMemory.put(query, newHM);
		}
	}


	//HashMap<String, HashMap<String, Integer>>
	//get the number of times user opened document based on this query for that docID
	public Integer getFrequency(String query, String docID) {

		//for this query, get each docID (relevant doc) with the freq
		HashMap<String, Integer> docIDFreq = queryMemory.get(query);

		//number of times user opened document based on this query for that docID
		if (docIDFreq.containsKey(docID)) return docIDFreq.get(docID);

		//never opened or not data not found
		else return 0; 
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


	public String[] getDictionary(ArrayList<String> uniqueTokens) {
		String[] res = new String[uniqueTokens.size()];
		
		for (int i=0; i<uniqueTokens.size(); i++) {
			res[i] = uniqueTokens.get(i);
		}
		
		return res;
	}

	//return list of topics from external text file (where the user chooses topic(s) form topic list)
	public String[] getTopicList() throws FileNotFoundException {
		ArrayList<String> topics = new ArrayList<String>();
		String[] res = new String[topics.size()];
		String tmp;
		String FILEPATH = System.getProperty("user.dir"); // dir of project saved
		String INPUT = File.separator + "reuters21578.tar" + File.separator + "all-topics-strings.lc.txt";

		File file = new File(FILEPATH + INPUT);
		BufferedReader br = new BufferedReader(new FileReader(file));

		try {
			//read by line
			while ((tmp = br.readLine()) != null) {
				topics.add(tmp);	
			}

			//add topics from arrayList to primitive array
			res = new String[topics.size()];
			for (int i=0; i<topics.size(); i++) {
				res[i] = topics.get(i);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}


	/*
	 * For the reuters, this function returns the list of reuters with the selected topics
	 */
	public ArrayList<String> filterDocbyTopic(ArrayList<String> inputList, JList list, DictionaryBuilder db, String type){
		ArrayList<String> filteredList = new ArrayList<String>();
		int[] index = list.getSelectedIndices(); //selected indexes
		List selected_topic = list.getSelectedValuesList(); //selected topics
		String topic = "";

		if (type.equals("Reuters")) {
			if (index.length > 0) {
				HashMap<String, ArrayList<String>> topictodocID = db.gettopictodocID(); //inverted index for topics
				ArrayList<String> docIDs = new ArrayList<String>();
				for (int i=0; i<selected_topic.size(); i++) { //iterate through each selected topics
					topic = selected_topic.get(i).toString(); //topic selected by user

					if (topictodocID.containsKey(topic)) { //if topic exists in hashmap
						docIDs = topictodocID.get(topic); //list of docIDs from the inverted index for topics

						for (int j=0; j<inputList.size(); j++) {
							if (docIDs.contains(inputList.get(j)) && !filteredList.contains(inputList.get(j))) {
								filteredList.add(inputList.get(j));
							}
						} //end of for loop (j)
					}

				} //end of for loop (i)
				return filteredList;

			} else {
				return inputList;
			}
		} 
		return inputList;
		
	} //end of filterDocbyTopic function
}
