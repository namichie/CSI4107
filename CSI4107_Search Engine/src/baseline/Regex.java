package baseline;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;

public class Regex {

	 
	public static String StripHtml(String html) {
	    return Jsoup.clean(html, Whitelist.none());
	}
	

        public static void main(String[] args) {
        	
        	Document doc;
			try {
				doc = Jsoup.connect("https://catalogue.uottawa.ca/en/courses/csi/").get();
				
				String html = doc.html();
				//System.out.println(html);
				//String regex = ""
				//html.replaceAll(regex, "");
				//String rep = "<(?:[^>=]|='[^']*'|=\"\"[^\"\"]*\"\"|=[^'\"\"][^\\s>]*)*>";
				String rep = "s/<[^>]*>//g";
				// the pattern we want to search for
    		    Pattern p = Pattern.compile(rep, Pattern.MULTILINE);
    		    Matcher m = p.matcher(html);

    		    // print all the matches that we find
    		    while (m.find())
    		    {
    		      System.out.println(m.group(1));
    		    }
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	/*String stringToSearch = "<p>Yada yada yada <code>foo</code> yada yada ...\n"
        		      + "more here <code>bar</code> etc etc\n"
        		      + "and still more <code>baz</code> and now the end</p>\n";

        		    // the pattern we want to search for
        		    Pattern p = Pattern.compile(" <code>(\\w+)</code> ", Pattern.MULTILINE);
        		    Matcher m = p.matcher(stringToSearch);

        		    // print all the matches that we find
        		    while (m.find())
        		    {
        		      System.out.println(m.group(1));
        		    }*/

        	
        }
    }


