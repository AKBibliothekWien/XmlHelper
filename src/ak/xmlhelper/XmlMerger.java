package ak.xmlhelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

//import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlMerger {

	public boolean mergeElements(String sourceDirectory, String destinationFile, String parentElement, String elementToMerge, int elementLevel, String parentAttributes, String elementAttributes) {
		boolean isMergingSuccessful = false;

		File fSourceDirectory = new File(sourceDirectory);
		File fDestinationFile = new File(destinationFile);

		if (fSourceDirectory.getAbsolutePath().equals(fDestinationFile.getParent())) {
			System.err.println("WARNING: Stopped merging process.\nIt's not possible to save the destination file " + fDestinationFile.getAbsolutePath() + " in the source directory " + fSourceDirectory.getAbsolutePath() + ". Please specify another path for your destination file!");
			return isMergingSuccessful;
		}

		if (!fSourceDirectory.exists()) {
			System.err.println("WARNING: Stopped merging process.\nDirectory " + fSourceDirectory.getAbsolutePath() + " does not exist!");
			return isMergingSuccessful;
		}

		String fileName = null;

		try {
			// Get XML files that should be merged
			File[] files = fSourceDirectory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});
			
			// If we have no files to merge, stop the process
			if (files.length <= 0) {
				return true;
			}
			
			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			// Set SAX parser namespace aware (namespaceawareness)
			xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

			// Create OutputStream and PrintWriter
			OutputStream out = new BufferedOutputStream(new FileOutputStream(fDestinationFile.getAbsolutePath()));
			PrintWriter writer = new PrintWriter(out);

			// Set ContentHandler:
			XmlContentHandler xmlContentHandler = new XmlContentHandler(elementToMerge, elementLevel, elementAttributes, writer);
			xmlReader.setContentHandler(xmlContentHandler);

			// Add XML intro tag
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			// Open with given parent element (including optional user defined attributes)
			writer.println("<" + parentElement + ((parentAttributes != null && !parentAttributes.isEmpty()) ? " " + parentAttributes : "") + ">");

			// Sort files for proper iteration
			Arrays.sort(files);

			// Iterate over files that should be merged
			for (File xmlFile : files) {
				// Specify XML-file to parse
				fileName = xmlFile.getAbsolutePath();
				FileReader reader = new FileReader(xmlFile);
				InputSource inputSource = new InputSource(reader);

				// Start parsing
				xmlReader.parse(inputSource);
			}

			// Close given parent element
			writer.println("</" + parentElement + ">");
			
			// Close the writer
			if (writer!=null) { writer.close(); }

			isMergingSuccessful = true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File not found: " + fileName);
		} catch (IOException e) {
			System.err.println("Error when parsing file: " + fileName);
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("SAXException when parsing file: " + fileName);
			e.printStackTrace();
		}

		return isMergingSuccessful;
	}



	/**
	 * Content Handler for merging XML elements.
	 */
	private class XmlContentHandler implements ContentHandler {

		String elementToMerge;
		PrintWriter writer;
		String elementContent;
		String fullXmlString = "";
		int elementLevel = 0;
		int elementLevelCounter = 0;
		boolean withinElement = false;
		String elementAttributes = null;

		/**
		 * Constructor for the Content Handler that helps merging XML elements.
		 * 
		 * @param elementToMerge	String: The XML element that should be merged, e. g. "record"
		 * @param elementLevel		int: The level of the element if there are nested elements of the same name. For the top-level element, use 1.
		 * @param writer			PrintWriter: A writer that is responsible for writing the appropriate elements to a file.
		 */
		private XmlContentHandler(String elementToMerge, int elementLevel, String elementAttributes, PrintWriter writer) {
			this.elementToMerge = elementToMerge;
			this.elementLevel = elementLevel;
			this.elementAttributes = elementAttributes;
			this.writer = writer;
		}


		/**
		 * Encounters start of element.<br><br>
		 * {@inheritDoc}
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			// Clear the element content variable (= text of XML element). If not, there will be problems with html-encoded characters (&lt;) at character()-method:
			elementContent = "";

			if(localName.equals(elementToMerge)) {

				// We encounter the given element, so we count it's level
				elementLevelCounter = elementLevelCounter + 1;

				// We encounter the given element at the appropriate level
				if (elementLevelCounter == elementLevel) {

					// Set a variable that tells us that we are within the appropriate element
					withinElement = true;
				}
			}

			if (withinElement) {
				
				// Open the XML-start-tag and add it to a String variable
				fullXmlString += "<" + qName;

				// Loop over any XML attribute and add it the XML-start-tag
				for (int a = 0; a < atts.getLength(); a++) {
					String attQName = atts.getQName(a);
					String attValue = StringEscapeUtils.escapeXml10(atts.getValue(a));

					// Add the xsi namespace if appropriate (only if attQName contains "xsi" and the xsi-Namespace was not already defined):
					if (attQName.contains("xsi:") && atts.getIndex("xmlns:xsi") == -1) {
						fullXmlString += " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
					}
					fullXmlString += " " + attQName + "=\"" + attValue + "\"";
				}
				
				// Add user defined attributes to the "element to merge" (could also be namespaces).
				if (localName.equals(elementToMerge) && elementAttributes != null && !elementAttributes.isEmpty()) {
					fullXmlString += " " + elementAttributes;
				}

				// Close the XML-start-tag and add it to a String variable
				fullXmlString += ">";
			}
		}


		/**
		 * Encounters end of element.<br><br>
		 * {@inheritDoc}
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			// Get the text content of the XML element
			String content = elementContent.toString();

			if (withinElement) {
				// Add the escaped content to a String variable
				fullXmlString += StringEscapeUtils.escapeXml10(content);

				// Reset the content variable for a fresh start
				elementContent = "";

				// Add the XML-end-tag to a String variable
				fullXmlString += "</" + qName + ">";

			}

			if(localName.equals(elementToMerge) ) {

				// We encounter the end of the given element, so we decrease the level counter
				elementLevelCounter = elementLevelCounter - 1;

				if (elementLevelCounter < elementLevel) {

					// Set a variable that tells us that we are not within the appropriate element
					withinElement = false;

					// Write the XML string to the file if it's not empty
					if (!fullXmlString.trim().isEmpty()) {
						writer.println(fullXmlString);
					}

					// Reset the String for the full XML for a fresh start
					fullXmlString = "";
				}
			}
		}	


		/**
		 * Reads the content of the current XML element.<br><br>
		 * {@inheritDoc}
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {			
			elementContent += new String(ch, start, length);
		}


		// Other methods that are not used at the moment
		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void endDocument() throws SAXException {}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}
		
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void skippedEntity(String name) throws SAXException {}

	}

}
