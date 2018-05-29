package ak.xmlhelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XmlSplitter2 {

	private String systemTempDirPath = System.getProperty("java.io.tmpdir");
	private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
	private File destinationDirectory = null;


	public XmlSplitter2(String destinationDirectoryPath) {
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

			// Use StAX parser and use Cursor style API as it is more efficient.
			FileInputStream fis = new FileInputStream(sourceFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader xsr;

			int counter = -1;
			boolean isRequestedElement = false;
			boolean isFilenameNode = false;
			//String fileNameNode = null;
			String fileName = null;
			//StringBuilder textContentSb = new StringBuilder("");
			StringBuilder elementSb = new StringBuilder("");
			//Map<String, String> fileNameAttrs;
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
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private String escapeXml(String string) {
		// Escape characters that are not allowed in XML:
		return string.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("\'", "&apos;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}


	private String unescapeXml(String string) {
		// Unescape escaped characters:
		return string.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&apos;", "\'").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
	}
	
}
