package baseline;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Preprocessor {

	ArrayList<String> ids, content;
	HashMap<String, String> output; 
	File outputFile;

//TODO - docID, title, description. can remove html src code manually...
	public Preprocessor() {
		ids = new ArrayList<String>();
		content =  new ArrayList<String>();
		output = new HashMap<String, String>(); //hashmap of hashmap?
		
		// TODO - file path is hard-coded right now
		//File input = new File("C:\\Users\\viet_\\Documents\\CSI\\4TH YR 2018-2019\\Winter 2019\\CSI4017_Search Engine\\src\\baseline\\csi_courses.txt");

		try {
			//Document doc = Jsoup.parse(input, "UTF-8", "https://catalogue.uottawa.ca/en/courses/csi/");
			Document doc = Jsoup.connect("https://catalogue.uottawa.ca/en/courses/csi/").get();

			Elements docIDs = doc.select("div.courseblock > p.courseblocktitle");
			
			// Add course IDs to list
			for (Element txt : docIDs) {
				String title = txt.text();
				
				// Extract course id from title
				ids.add(title.substring(4, 8)); //TODO - filter french courses starting with 5 or 7
			}

			// Add course descriptions to list
			for (Element description : docIDs) {	
				
				// Courses with no course description
				if (description.nextElementSibling().is("p.courseblockextra")) {
					// Index of courses that don't have a text description (added N/A for now)
					content.add(docIDs.indexOf(description), "N/A"); 
				
				// Courses with course description
				} else if (description.nextElementSibling().is("p.courseblockdesc")) {
					content.add(docIDs.indexOf(description), description.nextElementSibling().text());
					
				}
			}

			// Write id and description to file
			for (int i = 0; i < ids.size(); i++) {
				output.put(ids.get(i), content.get(i));
			}

			write(output);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//TODO - code from https://bukkit.org/threads/saving-loading-hashmap.56447/
	private void write(HashMap<String, String> input) {

		outputFile = new File(
				"C:\\Users\\viet_\\Documents\\CSI\\4TH YR 2018-2019\\Winter 2019\\CSI4017_Search Engine\\src\\baseline\\csi_courses_output.txt");

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

	public File getInputFile() {
		return outputFile;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	

	public static void main(String[] args) {

		Preprocessor p = new Preprocessor();

	}
}
