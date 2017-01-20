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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlCleaner {

	List<File> originalFiles = new ArrayList<File>();
	List<String> cleanedFileNames = new ArrayList<String>();
	String cleanedFileName = null;
	Map<String, File> fileMap = new HashMap<String, File>();

	public boolean cleanXml(String pathToXmlFile, boolean saveToSameFile) {
		boolean isCleaningDone = false;

		if (pathToXmlFile == null || pathToXmlFile == "") {
			System.out.println("You need to specify the path to the XML file(s) (folder or single file) you want to clean. Use an absolute path!");
			System.out.println("\tExample: /home/myhomefolder/myxmlfiles OR /home/myhomefolder/xml_file_to_clean.xml");
			return isCleaningDone;
		}
		
		System.out.println("Beginning XML cleaning. Please wait ...");

		File xmlFile = new File(pathToXmlFile);

		if (xmlFile.isFile()) {
			if (xmlFile.getAbsolutePath().endsWith(".xml")) {
				/*originalFiles.add(xmlFile);
				String cleanedFileName = xmlFile.getAbsolutePath() + "_clean";
				cleanedFileName = xmlFile.getAbsolutePath().replace(".xml", "") + "_clean.xml";
				cleanedFileNames.add(cleanedFileName);*/
				
				String cleanedFileName = xmlFile.getAbsolutePath() + "_clean";
				cleanedFileName = xmlFile.getAbsolutePath().replace(".xml", "") + "_clean.xml";
				fileMap.put(cleanedFileName, xmlFile);
			}
		} else if (xmlFile.isDirectory()) {
			File[] xmlFiles = xmlFile.listFiles();
			for (File xFile : xmlFiles) {
				if (xFile.getAbsolutePath().endsWith(".xml")) {
					/*originalFiles.add(xFile);
					String cleanedFileName = xFile.getAbsolutePath() + "_clean";
					cleanedFileName = xFile.getAbsolutePath().replace(".xml", "") + "_clean.xml";
					cleanedFileNames.add(cleanedFileName);*/
					
					String cleanedFileName = xFile.getAbsolutePath() + "_clean";
					cleanedFileName = xFile.getAbsolutePath().replace(".xml", "") + "_clean.xml";
					fileMap.put(cleanedFileName, xFile);
				}
			}
		}
		

		for (Entry<String, File> file : fileMap.entrySet()) {
			String cleanedFileName = file.getKey();
			File originalFile = file.getValue();
			this.cleanedFileName = cleanedFileName;
						
			BufferedReader reader = null;
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(cleanedFileName), 1024*1024);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			PrintWriter writer = null;

			try {
				reader = new BufferedReader(new FileReader(originalFile));
				writer = new PrintWriter(out);
				String line;
				
				// Iterate over each line in the XML file:
				while ((line = reader.readLine())!=null) {

					// Remove unicode characters in XML that are not allowed:
					line = line.replaceAll("[\\x{0fffe}-\\x{0ffff}]", " ");
					line = line.replaceAll("[\\x{0000}-\\x{0008}\\x{000E}-\\x{001F}\\x{009F}]", "");

					// Remove characters listed at https://www.w3.org/TR/REC-xml/#charsets
					line = line.replaceAll("[\\x{7F}-\\x{84}]", "");
					line = line.replaceAll("[\\x{86}-\\x{9F}]", "");
					line = line.replaceAll("[\\x{FDD0}-\\x{FDEF}]", "");
					line = line.replaceAll("[\\x{1FFFE}-\\x{1FFFF}]", "");
					line = line.replaceAll("[\\x{2FFFE}-\\x{2FFFF}]", "");
					line = line.replaceAll("[\\x{3FFFE}-\\x{3FFFF}]", "");
					line = line.replaceAll("[\\x{4FFFE}-\\x{4FFFF}]", "");
					line = line.replaceAll("[\\x{5FFFE}-\\x{5FFFF}]", "");
					line = line.replaceAll("[\\x{6FFFE}-\\x{6FFFF}]", "");
					line = line.replaceAll("[\\x{7FFFE}-\\x{7FFFF}]", "");
					line = line.replaceAll("[\\x{8FFFE}-\\x{8FFFF}]", "");
					line = line.replaceAll("[\\x{9FFFE}-\\x{9FFFF}]", "");
					line = line.replaceAll("[\\x{AFFFE}-\\x{AFFFF}]", "");
					line = line.replaceAll("[\\x{BFFFE}-\\x{BFFFF}]", "");
					line = line.replaceAll("[\\x{CFFFE}-\\x{CFFFF}]", "");
					line = line.replaceAll("[\\x{DFFFE}-\\x{DFFFF}]", "");
					line = line.replaceAll("[\\x{EFFFE}-\\x{EFFFF}]", "");
					line = line.replaceAll("[\\x{FFFFE}-\\x{FFFFF}]", "");
					line = line.replaceAll("[\\x{10FFFE}-\\x{10FFFF}]", "");

					// Remove high and low surrogate encoded characters (in decimal style) in the format of e. g. &#56385;
					// Surrogate ranges are:
					//	hex = D800-DBFF and DC00-DFFF
					//	dec =  55296 - 56319 and 56320 - 57343
					Pattern p = Pattern.compile("&#\\d+;");
					Matcher m = p.matcher(line);
					while (m.find()) {
						String unicodeChar = m.group();
						int unicodeNumber = Integer.valueOf(unicodeChar.replaceAll("[&#;]", ""));
						if (unicodeNumber >= 55296 && unicodeNumber <= 57343) {
							line = line.replace(unicodeChar, "");
						}
					}

					// Remove wrong XML tag notations
					line = line.replaceAll("<subfield code=\"\"\"", "<subfield code=\"\"");
					line = line.replaceAll("<subfield code=\"<\"", "<subfield code=\"\"");

					// Trim lines to save some disk space - CAN PRODUCE XML ERRORS!!!:
					//line = line.trim();

					// Write the cleaned line to a new file:
					writer.println(line);
				}

				isCleaningDone = true;
			} catch (Exception e) {
				isCleaningDone = false;
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace());
			}
			finally {
				writer.flush();
				if (writer!=null) {
					writer.close();
				}
				if (reader!=null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (saveToSameFile) {
					File cleanedFile = new File(cleanedFileName);
					originalFile.delete();
					cleanedFile.renameTo(originalFile);
				}
			}
		}
		
		return isCleaningDone;
	}


	public String getCleanedFile() {
		return this.cleanedFileName;
	}

}
