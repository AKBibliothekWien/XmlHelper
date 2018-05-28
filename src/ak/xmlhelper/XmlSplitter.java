package ak.xmlhelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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

			// TODO: Use StAX parser and use Cursor style API as it is more efficient.
			/*
			FileInputStream fis = new FileInputStream(sourceFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr;

			int counter = -1;
			boolean isRequestedElement = false;
			boolean isFilenameNode = false;
			String fileNameNode = null;
			String fileName = null;
			StringBuilder textContentSb = new StringBuilder("");
			StringBuilder elementSb = new StringBuilder("");
			Map<String, String> fileNameAttrs;
			FileWriter fileWriter = null;
			

			try {
				xsr = xif.createXMLStreamReader(bis);

				while(xsr.hasNext()){
					int eventType = xsr.next();
					
					String localName = null;
					String elementName = null;
					//String elementText = null;

					if (eventType == XMLStreamReader.START_ELEMENT) {
						localName = xsr.getLocalName();
						//String elementText = xsr.getElementText();
						
						if (localName.equals(nodeNametoExtract)) {
							counter = counter + 1;
						}

						if ((localName.equals(nodeNametoExtract) || isRequestedElement == true) && (counter >= nodeCount)) {

							// Set that this is the requested element
							isRequestedElement = true;
							isFilenameNode = false;

							// Get the element Name
							elementName = localName;
							if (elementName.isEmpty()) {
								elementName = xsr.getName().toString();
							}

							// Add opening tag (and attributes if some exist) to element string:
							elementSb.append("<" + elementName);
							int attributeCount = xsr.getAttributeCount();
							if (attributeCount > 0) { // The XML element has attributes

								for (int i = 0; i < attributeCount; i++) {
									String attributeName = xsr.getAttributeLocalName(i);
									String attributeValue = xsr.getAttributeValue(i);
									if (attributeName == null || attributeName.isEmpty()) {
										attributeName = xsr.getAttributeName(i).toString();
									}
									elementSb.append(" " + attributeName + "=\"" + this.escapeXml(attributeValue) + "\"");
								}

								Map<String, String> nodeAttributes = new HashMap<String, String>();
								if (localName.equals(condNodeForFilename)) {
									if (condAttrsForFilename != null && !condAttrsForFilename.isEmpty()) {
										for (int j = 0; j < attributeCount; j++) {
											String attributeName = xsr.getAttributeLocalName(j);
											String attributeValue = xsr.getAttributeValue(j);
											if (attributeName == null || attributeName.isEmpty()) {
												attributeName = xsr.getAttributeName(j).toString();
											}
											nodeAttributes.put(attributeName, attributeValue);
										}
										// Compare hashmaps
										isFilenameNode = nodeAttributes.entrySet().containsAll(condAttrsForFilename.entrySet());
									}
								}
								
							}  else {
								// There are no attrs to check, but the node name for getting the data the file name is right
								if (localName.equals(condNodeForFilename)) {
									isFilenameNode = true;
								}
							}
							elementSb.append(">");
							
							
							
						}
					}
					
					if (eventType == XMLStreamReader.END_ELEMENT) {
						
						
//						// Get the element Name
//						elementName = localName;
//						if (elementName.isEmpty()) {
//							elementName = xsr.getName().toString();
//						}
						
						
						if ((localName.equals(nodeNametoExtract) || isRequestedElement == true) && (counter >= nodeCount)) {
							// Add element text
							String elementText = xsr.getElementText();
							if (elementText != null && !elementText.trim().isEmpty()) {
								String trimmedElementText = elementText.trim();
								
								if (isFilenameNode) {
									fileName = trimmedElementText.replaceAll("\\W", "");
									isFilenameNode = false;
								}
								
								elementSb.append(this.escapeXml(unescapeXml(trimmedElementText)));
								
							}

							// Add closing tag
							elementSb.append("</" + elementName + ">");
						}
						
						
						if (localName.equals(nodeNametoExtract)) {
							counter = counter - 1;

							// End of element
							if (counter < nodeCount) {
								isRequestedElement = false;
								counter = -1; // Reset counter

								String elementStr = elementSb.toString();
								if (!elementStr.isEmpty()) {
									elementStr = elementStr.replaceAll("\\>\\s+\\<", "><"); // Remove whitespaces between XML tags

									if ((fileName != null && !fileName.isEmpty()) && this.destinationDirectory.exists()) {
										try {
											// Create file for final single element
											File file = new File(this.destinationDirectory.getAbsolutePath() + File.separator + fileName + ".xml");
											fileWriter = new FileWriter(file);
											fileWriter.write(elementStr);
											fileWriter.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}

								elementSb = new StringBuilder(""); // Reset element StringBuilder
								fileName = "";  // Reset filename String
							}
						}
						
					}
				}
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			*/







			// ORIGINAL CODE
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



	private String escapeXml(String string) {
		// Escape characters that are not allowed in XML:
		return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("\'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}


	private String unescapeXml(String string) {
		// Unescape escaped characters:
		return string.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
	}

	private class SplitterContentHandler implements ContentHandler {

		private String nodeNameToExtract;
		private int nodeCount = 0;
		private String fileNameNode;
		private Map<String, String> fileNameAttrs;
		private int counter = -1;
		private boolean isRequestedElement = false;
		//private String textContent;
		private StringBuilder textContentSb = new StringBuilder("");
		//private String element = "";
		private StringBuilder elementSb = new StringBuilder("");
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
		}


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			// TODO: Use StringBuilder instead of concatenation with "+" operator
			//this.textContent = "";
			this.textContentSb = new StringBuilder("");

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
				this.elementSb.append("<" + elementName);
				if (attrs != null) {
					for (int i = 0; i < attrs.getLength(); i++) {
						String attributeName = attrs.getLocalName(i);
						if ("".equals(attributeName)) {
							attributeName = attrs.getQName(i);
						}
						this.elementSb.append(" " + attributeName + "=\"" + this.escapeXml(attrs.getValue(i)) + "\"");
					}
				}
				this.elementSb.append(">");

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

			String textContent = this.textContentSb.toString();

			if (isFilenameNode) {
				this.fileName = textContent.replaceAll("\\W", "").trim(); // Remove all characters that are not A-Z, a-z or 0-9 for filename
				isFilenameNode = false; // Immediately reset to false as we now have already a filename. If not, we could end up with an empty filename if the filename-node is at the last position of the XML!
			}

			if ((localName.equals(this.nodeNameToExtract) || this.isRequestedElement == true) && this.counter >= this.nodeCount) {

				// Add text content
				this.elementSb.append(this.escapeXml(unescapeXml(textContent.trim())));

				// Reset text content string - MUST!
				this.textContentSb = new StringBuilder();

				// Add closing tag:
				String elementName = localName;
				if ("".equals(elementName)) {
					elementName = qName;
				}
				this.elementSb.append("</" + elementName + ">");

			}

			if (localName.equals(this.nodeNameToExtract)) {
				this.counter = this.counter - 1;

				// End of element
				if (this.counter < this.nodeCount) {
					this.isRequestedElement = false;
					this.counter = -1; // Reset counter

					String elementStr = this.elementSb.toString();
					if (!elementStr.isEmpty()) {
						elementStr = elementStr.replaceAll("\\>\\s+\\<", "><"); // Remove whitespaces between XML tags

						if ((this.fileName != null && !this.fileName.isEmpty()) && this.destinationDirectory.exists()) {
							try {
								// Create file for final single element
								File file = new File(this.destinationDirectory.getAbsolutePath() + File.separator + this.fileName + ".xml");
								this.fileWriter = new FileWriter(file);
								this.fileWriter.write(elementStr);
								this.fileWriter.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					this.elementSb = new StringBuilder(""); // Reset element StringBuilder
					this.fileName = "";  // Reset filename String
				}
			}
		}


		@Override
		public void characters(char[] ch, int start, int length) throws SAXException { // Get text content of nodes
			//this.textContent += new String(ch, start, length);
			//this.textContent = this.textContent.replaceAll("\\s+", " ");
			this.textContentSb.append(new String(ch, start, length));
		}


		private String escapeXml(String string) {
			// Escape characters that are not allowed in XML:
			return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("\'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		}


		private String unescapeXml(String string) {
			// Unescape escaped characters:
			return string.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		}


		// Unused methods of ContentHandler
		@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
		@Override public void processingInstruction(String target, String data) throws SAXException {}
		@Override public void skippedEntity(String name) throws SAXException {}
		@Override public void setDocumentLocator(Locator locator) {}
		@Override public void startDocument() throws SAXException {}
		@Override public void endDocument() throws SAXException {}
		@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {}
		@Override public void endPrefixMapping(String prefix) throws SAXException {}
	}
}