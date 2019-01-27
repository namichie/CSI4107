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

	public Preprocessor() {
		ids = new ArrayList<String>();
		content =  new ArrayList<String>();
		output = new HashMap<String, String>(); 

		// TODO - file path is hard-coded right now
		//File input = new File("C:\\Users\\viet_\\Documents\\CSI\\4TH YR 2018-2019\\Winter 2019\\CSI4107_Search Engine\\src\\baseline\\csi_courses.txt");

		try {
			//Document doc = Jsoup.parse(input, "UTF-8", "https://catalogue.uottawa.ca/en/courses/csi/");
			Document doc = Jsoup.connect("https://catalogue.uottawa.ca/en/courses/csi/").get();

			Elements docIDs = doc.select("div.courseblock > p.courseblocktitle");

			// Add course IDs, titles and descriptions to list
			for (Element course : docIDs) {
				String title = course.text();

				// Extract course id from title
				ids.add(title.substring(4, 8));

				// Courses with no course description
				if (course.nextElementSibling().is("p.courseblockextra")) {

					// Index of courses that don't have a text description (added N/A for now)
					content.add(docIDs.indexOf(course), "N/A"); 

				// Courses with course description
				} else if (course.nextElementSibling().is("p.courseblockdesc")) {

					// Filter name of course and exclude credits/units from title
					String courseName = "";
					int end = title.indexOf("(");
					if (title.contains("units")) {
						courseName = title.substring(9, end - 1);
					} else {
						courseName = title.substring(9);
					}

					// Add course title and description to list
					content.add(docIDs.indexOf(course), courseName + ":" + course.nextElementSibling().text());
				}
			}

			// Add ID, title and description to hash map
			for (int i = 0; i < ids.size(); i++) {
				// Filter out French courses
				if (ids.get(i).charAt(1) != '5' && ids.get(i).charAt(1) != '7') {
					output.put(ids.get(i), content.get(i));
				}
			}

			write(output);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//TODO - code from https://bukkit.org/threads/saving-loading-hashmap.56447/
	private void write(HashMap<String, String> input) {

		outputFile = new File(
				"C:\\Users\\viet_\\Documents\\CSI\\4TH YR 2018-2019\\Winter 2019\\CSI4107_Search Engine\\src\\baseline\\csi_courses_output.txt");

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
