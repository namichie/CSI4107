import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Preprocessor {

	protected ArrayList<String> ids, content;
	protected TreeMap<String, String> output; 
	protected File outputFile;
	private final String FILEPATH = System.getProperty("user.dir");
	private final String OUTPUT = File.separator + "csi_courses_output.txt";
	
	public Preprocessor() {
		ids = new ArrayList<String>();
		content =  new ArrayList<String>();
		output = new TreeMap<String, String>(); 

		try {
			Document doc = Jsoup.connect("https://catalogue.uottawa.ca/en/courses/csi/").get();

			Elements docIDs = doc.select("div.courseblock > p.courseblocktitle");

			// Add course IDs, titles and descriptions to list
			for (Element course : docIDs) {
				String title = course.text();

				// Extract course id from title
				ids.add(title.substring(4, 8));

				// Courses with no course description
				if (course.nextElementSibling().is("p.courseblockextra")) {

					String courseName = "";
					int end = title.indexOf("(");
					if (title.contains("units")) {
						courseName = title.substring(9, end - 1);
					} else {
						courseName = title.substring(9);
					}
					
					// Index of courses that don't have a text description (added N/A for now)
					content.add(docIDs.indexOf(course), courseName + ":" + "N/A"); 

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

	/* Site used: https://bukkit.org/threads/saving-loading-hashmap.56447/
	 * Modified from site to write a treemap to a text file 
	 */
	private void write(TreeMap<String, String> input) {
	
		outputFile = new File(FILEPATH + OUTPUT);

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

	
	public File getOutputFile() {
		return outputFile;
	}

}