package betullam.xmlhelper;

import java.io.File;

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
				.desc("Merge multiple XML files to one single XML file. args:\n 1. PathToFiles\n 2. ElementToMerge (e. g. \"record\")\n  3. NewFile\n  4. NewXmlParentElement (e. g. \"collection\")")
				.hasArgs()
				.numberOfArgs(4)
				//.argName("args")
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
				System.out.println("\nStart validating XML files.");

				String path = cmd.getOptionValue("v");
				File file = new File(path);
				XmlValidator validator = new XmlValidator();
				
				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (File fileInList : fileList) {
						if (fileInList.isFile() && fileInList.canRead() && fileInList.getName().endsWith(".xml")) {
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
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}






	}

}
