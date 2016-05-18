package betullam.xmlhelper;

import java.io.File;
import java.util.ArrayList;
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
				.desc("Merge multiple XML files to one single XML file. args:"
						+ "\n 1. PathToFiles"
						+ "\n 2. ElementToMerge (e. g. \"record\")"
						+ "\n 3. NewFile"
						+ "\n 4. NewXmlParentElement (e. g. \"collection\")")
				.hasArgs()
				.numberOfArgs(4)
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
								return;
							}
						}
					}
					System.out.println("All XML files are valid."); // We got this far, everything seems to be OK
				} else {
					if (file.canRead() && file.getName().endsWith(".xml")) {
						if(!validator.validateXML(file.getAbsolutePath())) {
							System.err.println("XML file not valid: " + file.getAbsolutePath());
							return;
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
				String newFile = (mergeArgs[2] != null) ? mergeArgs[2] : null;
				String newXmlParentElement = (mergeArgs[3] != null) ? mergeArgs[3] : null;

				if (pathToFiles != null && elementToMerge != null && newFile != null && newXmlParentElement != null) {
					XmlMerger xmlm = new XmlMerger(); // Start merging
					boolean isMergingSuccessful = xmlm.mergeElementNodes(pathToFiles, newFile, newXmlParentElement, elementToMerge, 0);

					if (isMergingSuccessful) {
						System.out.println("Merging files was successful.");
					} else {
						System.err.println("Error while merging files!");
						return;
					}
				}
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
						File[] arrFilesToSplit = fileToSplit.listFiles();
						for (File f : arrFilesToSplit) {
							if (f.isFile() && f.canRead() && f.getName().endsWith(".xml")) {
								filesToSplit.add(f);
							}
						}
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
			}


			}
		} catch (ParseException e) {
			e.printStackTrace();
		}






	}

}
