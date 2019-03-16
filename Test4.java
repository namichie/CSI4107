import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class Test4 {
	private final String FILEPATH = System.getProperty("user.dir"); // dir of project saved
	private final String INPUT = File.separator + "reuters21578.tar" + File.separator + "reut2-000.sgm";

	protected ArrayList<String> ids, content, topicsList, titlesList, textList, reuters;
	protected TreeMap<Integer, String> output;
	protected File outputFile;
	private final String OUTPUT = File.separator + "reuters_output.txt";
	
	
	
	public Test4() throws IOException {

		ids = new ArrayList<String>();
		content = new ArrayList<String>();
		topicsList = new ArrayList<String>();
		titlesList = new ArrayList<String>();
		textList = new ArrayList<String>();
		reuters = new ArrayList<String>();

		output = new TreeMap<Integer, String>();



		File input = new File(FILEPATH + INPUT);
		//String file = FileUtils.readFileToString(input, "UTF-8");
		//System.out.println(file);
		
		Document doc = Jsoup.parse(input, "UTF-8");
		String reuters = doc.select("REUTERS").html();
		Document d2 = Jsoup.parse(reuters);
		
		Elements r = doc.select("reuters");
		for (Element t : r) {
			
			Elements reut = t.getElementsByTag("reuters");
			String reuter = reut.html();
			String noDateline = reuter.replaceAll("(?s)<dateline>.*?</dateline>","");
			String noCompanies = noDateline.replaceAll("(?s)<companies>.*?</companies>","");
			String noDate = noCompanies.replaceAll("(?s)<date>.*?</date>","");
			String noPlaces = noDate.replaceAll("(?s)<places>.*?</places>","");
			String noPeople = noPlaces.replaceAll("(?s)<people>.*?</people>","");
			String noOrg = noPeople.replaceAll("(?s)<orgs>.*?</orgs>","");
			String noExchange = noOrg.replaceAll("(?s)<exchanges>.*?</exchanges>","");
			String noUnknown = noExchange.replaceAll("(?s)<unknown>.*?</unknown>","");
			
			
			String noTitle = noUnknown.replaceAll("(?s)<title>.*?</title>","");
			String noTopic = noTitle.replaceAll("(?s)<topics>.*?</topics>","");

			//System.out.println(noTopic);
			
			//TODO = TOPICS
			Elements topics = t.getElementsByTag("topics");
			
			String topic = topics.text();
			if (topic.equals("")) {
				topic = "N/A";
			} else {
				topic = topic.replaceAll("<[^>]*>"," ");
			}
			topicsList.add(topic);
			//System.out.println(topic);
			
			//TODO - TITLE
			Elements titles = t.getElementsByTag("title"); 
			String title = titles.text();
			if (title.equals("")) {
				title = "N/A";
			} else {
				title = titles.text();
			}
			titlesList.add(title);

			//System.out.println(title);
			
			//TODO - BODY
			Elements text = t.getElementsByTag("text"); 
			String body = text.text();
			if (body.equals("")) {
				body = "N/A";
			} else {
				body = text.text();
			}
			//System.out.println(body);
			

			
			Whitelist w = new Whitelist();
			w.addAttributes("text", "body");
			String test1 = Jsoup.clean(noTopic, w);
			String test2 = Jsoup.parse(test1).text();
			System.out.println(test2);
			
			// Remove unwanted text from end of body
			test2 = test2.replaceAll("Reuter$", "");
			test2 = test2.replaceAll("Blah blah blah.$", "");
			test2 = test2.replaceAll("\\*+", "N/A");
			
			textList.add(test2);
		}
		
		for (int i = 0; i < r.size(); i++) {
			
		
			content.add(topicsList.get(i) + " ייי " + titlesList.get(i) + "---" + textList.get(i));
			
		}
			
			
			/*Elements topic = t.getElementsByTag("topics"); //.wholeText();//.text();
			//System.out.println(topic.html());
			
			Elements title = t.getElementsByTag("title"); 
			//System.out.println(title.html());
			
			Elements body = t.getElementsByTag("text"); 
			//String test = body.html();
			String b2 = reuter.replaceAll("(?s)<dateline>.*?</dateline>","");
			*/
			
			//System.out.println(b3);
			//System.out.println(body.html());
			
			//topic.text();
			
			
		
		
		
		//System.out.println(reuters);
		/*Elements topics = d2.select("topics");
		
		for (Element t : topics) {
			String topic = t.text();
			//System.out.println(topic);
		}*/
		
		/*Elements titles = d2.select("text > title");
		
		for (Element t : titles) {
			String title = t.text();
			//System.out.println(title);
			
		}
		
		Elements text = doc.select("body");
		
		for (Element t : text) {
			String body = t.text();
			//System.out.println(body);
		}*/
		
		
		/*for (Element b : reuters) {
			
			body = b.outerHtml();//b.toString(); // returns tags of reuters
			
			//System.out.println(body);
		}*/
		
		/*String[] topics = StringUtils.substringsBetween(body, "<topics>", "</topics>");
		for (int i = 0; i < topics.length; i++) {
			
			if (topics[i].equals("")) {
				topics[i] = "N/A";
				
			} else {
				topics[i] = topics[i].replaceAll("\\<.*?>"," ").trim(); 
				//topics[i] = Jsoup.parse(topics[i]).text();
			}
			//System.out.println(topics[i]);
			topicsList.add(i, topics[i]);
		}
		System.out.println(topics.length);*/
	

		
		// Get body
		/*String body = "";
		Elements reuters = doc.select("REUTERS");
		for (Element b : reuters) {
			
			body = b.outerHtml();//b.toString(); // returns tags of reuters
			
			System.out.println(body);
		}*/
		
		
		

	
	
	for (int j = 0; j < content.size(); ++j) {
		
		//content.add(j, topicsList.get(j) + "---" + textList.get(j));
		output.put(j, content.get(j));
	}



	write(output);
	}
	private void write(TreeMap<Integer, String> input) {

		outputFile = new File(FILEPATH + OUTPUT);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for (Integer s : input.keySet()) {
				bw.write(s + "-" + input.get(s));
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Test4 t = new Test4();

	}

}
