import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

public class PreprocessorReuters {

	private final String FILEPATH = System.getProperty("user.dir"); // dir of project saved
	private final String INPUT = File.separator + "reuters21578.tar"; // name of folder
	private ArrayList<File> files = new ArrayList<File>();

	protected ArrayList<String> content, topicsList, titlesList, textList;
	protected TreeMap<Integer, String> output;
	protected File outputFile;
	private final String OUTPUT = File.separator + "reuters_output.txt";

	public PreprocessorReuters() throws IOException {
		content = new ArrayList<String>();
		topicsList = new ArrayList<String>();
		titlesList = new ArrayList<String>();
		textList = new ArrayList<String>();
		output = new TreeMap<Integer, String>();

		File input;
		files = getsgmFileName();

		// Get each file from file array
		for (int i = 0; i < files.size(); i++) {
			input = files.get(i);

			// Parse each file
			Document doc = Jsoup.parse(input, "UTF-8");

			// Get each Reuter doc to iterate through
			Elements reuters = doc.select("reuters");
			for (Element r : reuters) {

				// Get Reuters doc from its tag, convert to HTML format
				Elements reut = r.getElementsByTag("reuters");
				String reuter = reut.html();

				// Cleaning/removing unwanted tags
				String noDateline = reuter.replaceAll("(?s)<dateline>.*?</dateline>","");
				String noCompanies = noDateline.replaceAll("(?s)<companies>.*?</companies>","");
				String noDate = noCompanies.replaceAll("(?s)<date>.*?</date>","");
				String noPlaces = noDate.replaceAll("(?s)<places>.*?</places>","");
				String noPeople = noPlaces.replaceAll("(?s)<people>.*?</people>","");
				String noOrg = noPeople.replaceAll("(?s)<orgs>.*?</orgs>","");
				String noExchange = noOrg.replaceAll("(?s)<exchanges>.*?</exchanges>","");
				String noUnknown = noExchange.replaceAll("(?s)<unknown>.*?</unknown>","");

				// Remove title, topic tags since they'll be processed separately
				String noTitle = noUnknown.replaceAll("(?s)<title>.*?</title>","");
				String noTopic = noTitle.replaceAll("(?s)<topics>.*?</topics>","");

				// Extract topics
				Elements topics = r.getElementsByTag("topics");

				String topic = topics.text();
				if (topic.equals("")) {
					topic = "N/A";
				} else {
					topic = topic.replaceAll("<[^>]*>"," "); //TODO - separate many topics
				}
				topicsList.add(topic);

				// Extract titles
				Elements titles = r.getElementsByTag("title"); 
				String title = titles.text();
				if (title.equals("")) {
					title = "N/A";
				} else {
					title = titles.text();
				}
				titlesList.add(title);


				// Extract body
				Elements text = r.getElementsByTag("text"); 
				String body = text.text();
				if (body.equals("")) {
					body = "N/A";
				} else {
					body = text.text();
				}


				// Create a whitelist of tags to ignore when cleaning
				Whitelist w = new Whitelist();
				w.addAttributes("text", "body");
				String cleanText = Jsoup.clean(noTopic, w);
				String finalText = Jsoup.parse(cleanText).text();

				// Remove unwanted text from end of body
				finalText = finalText.replaceAll("Reuter$", "");
				finalText = finalText.replaceAll("Blah blah blah.$", "");
				finalText = finalText.replaceAll("\\*+", "N/A");

				textList.add(finalText);
			}

			// Combine topics + title + body
			for (int j = 0; j < reuters.size(); j++) {

				content.add(topicsList.get(j) + " ééé " + titlesList.get(j) + "---" + textList.get(j));
			}

			// Add document to treemap
			for (int j = 0; j < content.size(); ++j) {

				output.put(j, content.get(j));
			}

			write(output);

		}

	}



	/*
	 * Code modified from this website:
	 * https://stackoverflow.com/questions/1384947/java-find-txt-files-in-specified-
	 * folder
	 */
	public ArrayList<File> getsgmFileName() {

		// directory of all the .sgm files
		String directory = FILEPATH + INPUT;

		// store all the names of the .sgm files into an ArrayList
		ArrayList<File> textFiles = new ArrayList<File>();

		// create a file instance
		File dir = new File(directory);

		// for each files in the directory
		for (File file : dir.listFiles()) {

			// if it's a .sgm file, add it to the textFiles ArrayList
			if (file.getName().endsWith((".sgm"))) {
				textFiles.add(file);
				// array of file path
				// C:\Users\viet_\Documents\CSI\4TH YR 2018-2019\Winter 2019\CSI4107_Search
				// Engine\reuters21578.tar\reut2-000.sgm
			}
		}

		return textFiles;
	} // end of function getsgmFileName


	/*
	 * Site used: https://bukkit.org/threads/saving-loading-hashmap.56447/ Modified
	 * from site to write a treemap to a text file
	 */
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

		PreprocessorReuters p = new PreprocessorReuters();
		ArrayList<File> a = p.getsgmFileName();

	}

}
