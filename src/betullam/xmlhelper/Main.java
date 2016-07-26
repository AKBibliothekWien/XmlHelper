package betullam.xmlhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class Main {

	// CLI options
	static Options options = new Options();
	static OptionGroup optionGroup = new OptionGroup();

	public static void main(String[] args) {


		CommandLineParser clParser = new DefaultParser();

		// v: validate
		Option oValidate = Option
				.builder("v")
				.required(true)
				.longOpt("validate")
				.desc("Validate XML file(s)")
				.hasArg()
				.argName("PathToFileOrDir")
				.build();

		// m: merge
		Option oMerge= Option
				.builder("m")
				.required(true)
				.longOpt("merge")
				.desc("Merge multiple XML files to one single XML file. Works only wiht XML-files containing one single XML element. args:"
						+ "\n 1. PathToFiles"
						+ "\n 2. ElementToMerge (e. g. \"record\")"
						+ "\n 3. ElementLevel (for nested elements with same name. 0 for top level, 1 for first level, ...)"
						+ "\n 4. NewFile"
						+ "\n 5. NewXmlParentElement (e. g. \"collection\")")
				.hasArgs()
				.numberOfArgs(5)
				.build();

		// s: split
		Option oSplit= Option
				.builder("s")
				.required(true)
				.longOpt("split")
				.desc("Split one XML file into multiple single XML files. args:"
						+ "\n 1. File(s)ToSplit"
						+ "\n 2. NodeToExtractName (e. g. \"record\")"
						+ "\n 3. NodeToExtractCount"
						+ "\n 4. ConditionNodeForFilename (e. g. \"controllfield\")"
						+ "\n 5. ConditionAttrsForFilename (e. g. \"attr1=value1,attr2=value2,...\" or \"null\" if none)"
						+ "\n 6. DestinationDir")
				.hasArgs()
				.numberOfArgs(6)
				.build();		

		// c: count
		Option oCount= Option
				.builder("c")
				.required(true)
				.longOpt("count")
				.desc("Count XML elements in a XML file. args:"
						+ "\n 1. Path to XML file"
						+ "\n 2. Name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use \"name\"."
						+ "\n 3. Name of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"...\" /> tags, use \"attr\"."
						+ "\n 4. Value of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"value\" /> tags, use \"value\".")
				.hasArgs()
				.numberOfArgs(4)
				.build();

		// l: cLean
				Option oClean= Option
						.builder("l")
						.required(true)
						.longOpt("clean")
						.desc("Clean XML syntax (removing forbidden Unicode characters) in a given XML-file. args:"
								+ "\n 1. Path to XML file")
						.hasArgs()
						.numberOfArgs(1)
						.build();
				
		// h: help
		Option oHelp = Option
				.builder("h")
				.required(true)
				.longOpt("help")
				.desc("Show help")
				.build();

		optionGroup.addOption(oValidate);
		optionGroup.addOption(oMerge);
		optionGroup.addOption(oSplit);
		optionGroup.addOption(oCount);
		optionGroup.addOption(oClean);
		optionGroup.addOption(oHelp);
		options.addOptionGroup(optionGroup);


		try {
			CommandLine cmd = clParser.parse(options, args, true);
			String selectedMainOption = optionGroup.getSelected();

			if (args.length <= 0 || cmd.hasOption("h")) {
				HelpFormatter helpFormatter = new HelpFormatter();
				helpFormatter.printHelp("BetullamXmlHelper", "", options, "", true);
				return;
			}

			// Switch between main options
			switch (selectedMainOption) {
			case "v": {

				String path = cmd.getOptionValue("v");
				File file = new File(path);
				XmlValidator validator = new XmlValidator();

				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (File fileInList : fileList) {
						if (fileInList.isFile() && fileInList.canRead() && fileInList.getName().endsWith(".xml")) {
							System.out.print("Validating XML file " + fileInList.getName() + "                                                                        \r");
							if(!validator.validateXML(fileInList.getAbsolutePath())) {
								System.err.println("XML file not valid: " + fileInList.getAbsolutePath());
								return; // TODO: break instead of return?
							}
						}
					}
					System.out.println("All XML files are valid."); // We got this far, everything seems to be OK
				} else {
					if (file.canRead() && file.getName().endsWith(".xml")) {
						if(!validator.validateXML(file.getAbsolutePath())) {
							System.err.println("XML file not valid: " + file.getAbsolutePath());
							return; // TODO: break instead of return?
						}
					}
					System.out.println("XML file is valid."); // We got this far, everything seems to be OK
				}

				break;
			}

			case "m": {
				System.out.println("\nStart merging XML files.");

				String[] mergeArgs = cmd.getOptionValues("m");
				String pathToFiles = (mergeArgs[0] != null) ? mergeArgs[0] : null;
				String elementToMerge = (mergeArgs[1] != null) ? mergeArgs[1] : null;
				int elementLevel = (mergeArgs[2] != null) ? Integer.valueOf(mergeArgs[2]) : 0;
				String newFile = (mergeArgs[3] != null) ? mergeArgs[3] : null;
				String newXmlParentElement = (mergeArgs[4] != null) ? mergeArgs[4] : null;

				if (pathToFiles != null && elementToMerge != null && newFile != null && newXmlParentElement != null) {
					XmlMerger xmlm = new XmlMerger(); // Start merging
					boolean isMergingSuccessful = xmlm.mergeElementNodes(pathToFiles, newFile, newXmlParentElement, elementToMerge, elementLevel);

					if (isMergingSuccessful) {
						System.out.println("Merging files was successful.");
					} else {
						System.err.println("Error while merging files!");
						return;
					}
				}

				break;
			}

			case "s": {

				System.out.println("\nStart splitting XML file.");
				String[] splitArgs = cmd.getOptionValues("s");
				List<File> filesToSplit = new ArrayList<File>();


				String strFileToSplit = (splitArgs[0] != null) ? splitArgs[0] : null;
				String nodeToExtractName = (splitArgs[1] != null) ? splitArgs[1] : null;
				int nodeToExtractCount = (splitArgs[2] != null) ? Integer.valueOf(splitArgs[2]) : 0;
				String conditionNodeForFilename = (splitArgs[3] != null) ? splitArgs[3] : null;
				Map<String, String> conditionAttrsForFilename = new HashMap<String, String>();
				String destinationDir = (splitArgs[5] != null) ? splitArgs[5] : null;

				if (strFileToSplit != null && nodeToExtractName != null && conditionNodeForFilename != null && destinationDir != null) {

					XmlSplitter xmls = new XmlSplitter(destinationDir);

					// Check if file or path:
					File fileToSplit = new File(strFileToSplit);
					boolean isDir = false;
					if (fileToSplit.isDirectory()) {
						isDir = true;
					}

					if (isDir) {
						if (fileToSplit != null && fileToSplit.canRead()) {
							// Get a sorted list (by filename) of all XML files:
							List<File> filteredFiles = (List<File>)FileUtils.listFiles(fileToSplit, new String[] {"xml"}, true); // Get all xml-files recursively
							for (File f : filteredFiles) {
								if (f.isFile() && f.canRead() && f.getName().endsWith(".xml")) {
									filesToSplit.add(f);
								}
							}
							Collections.sort(filesToSplit); // Sort oldest to newest
						}	

						/*
						File[] arrFilesToSplit = fileToSplit.listFiles();
						for (File f : arrFilesToSplit) {
							if (f.isFile() && f.canRead() && f.getName().endsWith(".xml")) {
								filesToSplit.add(f);
							}
						}
						 */
					}

					String strConditionAttrsForFilename = (splitArgs[4] != null) ? splitArgs[4] : null;
					if (strConditionAttrsForFilename != null && !strConditionAttrsForFilename.equals("null") && !strConditionAttrsForFilename.isEmpty()) {
						String[] arrConditionAttrsForFilename = strConditionAttrsForFilename.split("\\s*,\\s*");
						for (String conditionAttrForFilename : arrConditionAttrsForFilename) {
							String[] attrValuePair = conditionAttrForFilename.split("\\s*=\\s*");
							if (attrValuePair.length == 2) {
								String attr = attrValuePair[0];
								String value = attrValuePair[1];
								conditionAttrsForFilename.put(attr, value);
							}
						}
					}

					if (isDir && !filesToSplit.isEmpty()) {
						// Split XMLs. Files will be overwritten by newer files with same name:
						for (File fileForSplitting : filesToSplit) {
							System.out.print("Splitting file " + fileForSplitting.getAbsolutePath() + "                                                        \r");
							xmls.split(fileForSplitting.getAbsolutePath(), nodeToExtractName, nodeToExtractCount, conditionNodeForFilename, conditionAttrsForFilename);
						}
					} else if (!isDir){
						System.out.print("Splitting file " + fileToSplit.getAbsolutePath() + "                                                        \r");
						xmls.split(fileToSplit.getAbsolutePath(), nodeToExtractName, nodeToExtractCount, conditionNodeForFilename, conditionAttrsForFilename);
					}
				}

				break;
			}

			case "c": {

				System.out.println("\nStart counting XML elements.");
				String[] countArgs = cmd.getOptionValues("c");

				String xmlFile = (countArgs[0] != null) ? countArgs[0] : null;
				String tagName = (countArgs[1] != null) ? countArgs[1] : null;
				String attrName = (countArgs[2] != null && !countArgs[2].equals("null")) ? countArgs[2] : null;
				String attrValue = (countArgs[3] != null && !countArgs[3].equals("null")) ? countArgs[3] : null;

				if (xmlFile != null && tagName != null) {
					XmlCounter xmlc = new XmlCounter();
					int noOfElements = xmlc.count(xmlFile, tagName, attrName, attrValue, true);
					System.out.print("                                                                                              \r");
					System.out.print("Total elements: " + noOfElements + "\n");
				}
				break;
			}
			
			case "l": {
				
				//System.out.println("\nStart cleaning XML file.");
				String[] cleanArgs = cmd.getOptionValues("l");

				String xmlFile = (cleanArgs[0] != null) ? cleanArgs[0] : null;
				XmlCleaner xmlcl = new XmlCleaner();
				xmlcl.cleanXml(xmlFile);
				System.out.println("Path to cleaned file: " + xmlcl.getCleanedFile());
				
				break;
			}

			}
		} catch (ParseException e) {
			e.printStackTrace();
		}






	}

}
