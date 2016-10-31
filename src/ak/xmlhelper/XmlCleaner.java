package ak.xmlhelper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class XmlCleaner {

	String cleanedFileName = null;
	
	public boolean cleanXml(String pathToXmlFile) {
		boolean isCleaningDone = false;

		if (pathToXmlFile == null || pathToXmlFile == "") {
			System.out.println("You need to specify the path to the XML file you want to clean. Use an absolute path!");
			System.out.println("Example: /home/myhomefolder/xml_file_to_clean.xml");
			return isCleaningDone;
		}

		System.out.println("Beginning XML cleaning. Please wait ...");

		File xmlFile = new File(pathToXmlFile);

		cleanedFileName = xmlFile.getAbsolutePath() + "_clean";
		if (xmlFile.getAbsolutePath().endsWith(".xml")) {
			cleanedFileName = xmlFile.getAbsolutePath().replace(".xml", "") + "_clean.xml";
		}

		BufferedReader reader = null;
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(cleanedFileName), 1024*1024);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		PrintWriter writer = null;

		try {
			reader = new BufferedReader(new FileReader(xmlFile));
			writer = new PrintWriter(out);

			String line;
			//int lineCounter = 0;
			// Iterate over each line in the XML file:
			while ((line = reader.readLine())!=null) {

				/*
				// Find bad signs:
				//Pattern p = Pattern.compile("[^\\x09\\x0A\\x0D\\x20-\\xD7EF\\xE000-\\xFFFD\\x10000-x10FFFF]");
				Pattern p = Pattern.compile("[\\x{0fffe}-\\x{0ffff}]");
				Matcher m = p.matcher(line);
				lineCounter = lineCounter+1;
				if (m.find()) {
				    System.out.println("Found bad sign on line " + lineCounter);
				}
				 */

				// Remove unicode characters in XML that are not allowed:
				line = line.replaceAll("[\\x{0fffe}-\\x{0ffff}]", " ");
				line = line.replaceAll("[\\x{0000}-\\x{0008}\\x{000E}-\\x{001F}\\x{009F}]", "");
				line = line.replaceAll("<subfield code=\"\"\"", "<subfield code=\"\"");
				line = line.replaceAll("<subfield code=\"<\"", "<subfield code=\"\"");

				// Trim lines to save some disk space - CAN PRODUCE XML ERRORS!!!:
				//line = line.trim();

				// Write the cleaned line to a new file:
				writer.println(line);
				//System.out.println("XML cleaning done!");

			}

			isCleaningDone = true;
		} catch (Exception e) {
			isCleaningDone = false;
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
		finally {
			writer.flush();
			if (writer!=null) writer.close();
			if (reader!=null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		return isCleaningDone;
	}
	
	
	public String getCleanedFile() {
		return this.cleanedFileName;
	}

}
