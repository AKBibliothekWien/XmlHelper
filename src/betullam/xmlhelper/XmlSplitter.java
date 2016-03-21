package betullam.xmlhelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class XmlSplitter {

	private String systemTempDirPath = System.getProperty("java.io.tmpdir");
	private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
	private File destinationDirectory = null;

	public XmlSplitter(String destinationDirectoryPath) {
		// Create destination directory. If destinationDirectoryPath is null, we use temp directory of OS:
		if (destinationDirectoryPath == null || destinationDirectoryPath.isEmpty()) {
			this.destinationDirectory = new File(tempDirPath);
		} else {
			this.destinationDirectory = new File(destinationDirectoryPath);
		}
		if (!this.destinationDirectory.exists()) {
			this.destinationDirectory.mkdir();
		}
	}

	public void split(String sourceFile, String nodeNametoExtract, int nodeCount, String condNodeForFilename, Map<String, String> condAttrsForFilename) {

		try {
			// Specify XML-file to parse
			FileReader reader = new FileReader(sourceFile);
			InputSource inputSource = new InputSource(reader);

			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			SplitterContentHandler cch = new SplitterContentHandler(this.destinationDirectory, nodeNametoExtract, nodeCount, condNodeForFilename, condAttrsForFilename);
			xmlReader.setContentHandler(cch);
			xmlReader.parse(inputSource);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public File getDestinationDirectory() {
		return this.destinationDirectory;
	}



	private class SplitterContentHandler implements ContentHandler {

		private String nodeNameToExtract;
		private int nodeCount = 0;
		private String fileNameNode;
		private Map<String, String> fileNameAttrs;
		private int counter = -1;
		private boolean isRequestedElement = false;
		private String textContent;
		private String element = "";
		//private String systemTempDirPath = System.getProperty("java.io.tmpdir");
		//private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
		private File destinationDirectory = null;
		private boolean isFilenameNode = false;
		private String fileName = "";
		private FileWriter fileWriter = null;


		private SplitterContentHandler(File destinationDirectory, String nodeNameToExtract, int nodeCount, String fileNameNode, Map<String, String> fileNameAttrs) {
			this.destinationDirectory = destinationDirectory;
			this.nodeNameToExtract = nodeNameToExtract;
			this.nodeCount = nodeCount;
			this.fileNameNode = fileNameNode;
			this.fileNameAttrs = fileNameAttrs;

			/*
			// Create destination directory. If destinationDirectoryPath is null, we use temp directory of OS:
			if (destinationDirectoryPath == null || destinationDirectoryPath.isEmpty()) {
				this.destinationDirectory = new File(tempDirPath);
			} else {
				this.destinationDirectory = new File(destinationDirectoryPath);
			}

			if (!this.destinationDirectory.exists()) {
				this.destinationDirectory.mkdir();
			}
			 */
		}

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
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {

			this.textContent = "";

			if (localName.equals(this.nodeNameToExtract)) {
				this.counter = this.counter + 1;
			}

			if ((localName.equals(this.nodeNameToExtract) || this.isRequestedElement == true) && this.counter >= this.nodeCount) {
				this.isRequestedElement = true;

				String elementName = localName;

				if ("".equals(elementName)) {
					elementName = qName;
				}

				// Add opening tag (and attributes if some exist) to element string:
				this.element += "<" + elementName;
				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String attributeName = attrs.getLocalName(i);
						if ("".equals(attributeName)) {
							attributeName = attrs.getQName(i);
						}
						this.element += " " + attributeName + "=\"" + this.escapeXml(attrs.getValue(i)) + "\"";
					}
				}
				this.element += ">";

				// Get filename node:
				isFilenameNode = false;
				Map<String, String> nodeAttributes = new HashMap<String, String>();
				if (localName.equals(this.fileNameNode)) {
					if (this.fileNameAttrs != null && !this.fileNameAttrs.isEmpty()) { // Check for attributes
						if (attrs != null && attrs.getLength() > 0) { // Attributes exist
							for (int j = 0; j < attrs.getLength(); j++) {
								// Get attribute name
								String attrName = attrs.getLocalName(j);
								if ("".equals(attrName)) {
									attrName = attrs.getQName(j);
								}
								// Get attribute value
								String attrValue = attrs.getValue(j);
								nodeAttributes.put(attrName, attrValue);
							}

							// Compare hashmaps:
							isFilenameNode = nodeAttributes.entrySet().containsAll(this.fileNameAttrs.entrySet());
						} else {
							// We want to check for attributes but they do not exist
							isFilenameNode = false;
						}
					} else {
						// No attrs to check
						isFilenameNode = true;
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {

			if (isFilenameNode) {
				this.fileName = this.textContent.replaceAll("\\W", "").trim(); // Remove all characters that are not A-Z, a-z or 0-9 for filename
			}

			if ((localName.equals(this.nodeNameToExtract) || this.isRequestedElement == true) && this.counter >= this.nodeCount) {

				// Add text content
				this.element += this.escapeXml(unescapeXml(this.textContent.trim()));

				// Reset text content string - MUST!
				this.textContent = "";

				// Add closing tag:
				String elementName = localName;
				if ("".equals(elementName)) {
					elementName = qName;
				}
				this.element += "</" + elementName + ">";

			}

			if (localName.equals(this.nodeNameToExtract)) {
				this.counter = this.counter - 1;

				// End of element
				if (this.counter < this.nodeCount) {
					this.isRequestedElement = false;
					this.counter = -1; // Reset counter

					if (this.element != null && !this.element.isEmpty()) {
						this.element = this.element.replaceAll("\\>\\s+\\<", "><"); // Remove whitespaces between XML tags
						//System.out.println(this.element); // Final single element


						if ((this.fileName != null && !this.fileName.isEmpty()) && this.destinationDirectory.exists()) {
							try {
								// Create file for final single element
								File file = new File(this.destinationDirectory.getAbsolutePath() + File.separator + this.fileName + ".xml");
								this.fileWriter = new FileWriter(file);
								this.fileWriter.write(this.element);
								this.fileWriter.close();
								//System.out.println("File written to: " + this.destinationDirectory.getAbsolutePath() + File.separator + this.fileName + ".xml");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					this.element = ""; // Reset element String
					this.fileName = "";  // Reset filename String
				}
			}

		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException { // Get text content of nodes
			this.textContent += new String(ch, start, length);
			this.textContent = this.textContent.replaceAll("\\s+", " ");
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void skippedEntity(String name) throws SAXException {}


		private String escapeXml(String string) {
			// Escape characters that are not allowed in XML:
			return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("\'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		}
		
		private String unescapeXml(String string) {
			// Unescape escaped characters:
			return string.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'").replaceAll("&lt;", "<").replaceAll("&gt;", ">");

		}
	}
}
