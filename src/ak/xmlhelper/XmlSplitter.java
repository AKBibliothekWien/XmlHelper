package ak.xmlhelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class XmlSplitter {

	private String systemTempDirPath = System.getProperty("java.io.tmpdir");
	private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
	private File destinationDirectory = null;
	private String destinationDirectoryStr = null;


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
		this.destinationDirectoryStr = this.destinationDirectory.getAbsolutePath() + File.separator;
	}

	public void split(String sourceFile, String nodeNametoExtract, int nodeCount, String condNodeForFilename, Map<String, String> condAttrsForFilename) {
		try {
			FileInputStream fis = new FileInputStream(sourceFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			XMLStreamWriter xswString = null;
			XMLStreamWriter xswFile = null;		
			XMLStreamReader xsr = xif.createXMLStreamReader(bis);
			int counter = 0;
			FileWriter fw = null;
			StringWriter sw = null;
			StringBuilder fileName = null;
			boolean isFileNameNode = false;

			while(xsr.hasNext()) {
				int e = xsr.next();

				switch (e) {
				case XMLStreamConstants.START_ELEMENT:
					String localName = xsr.getLocalName();

					if (localName.equals(nodeNametoExtract)) {
						counter++;

						if (counter == nodeCount) {
							fileName = new StringBuilder();
							sw = new StringWriter();
							xswString = xof.createXMLStreamWriter(sw);
						}
					}

					if (counter >= nodeCount) {
						
						// Write the opening XML element
						if (xswString != null) {
							xswString.writeStartElement(localName);
						}

						// Check if the opening XML element has attributes
						int noAttr = xsr.getAttributeCount();
						if (noAttr > 0) {
							// If we have attributes, write them to the opening XML element
							for (int i = 0; i < noAttr; i++) {
								String attrName = xsr.getAttributeLocalName(i);
								String attrValue = xsr.getAttributeValue(i);
								xswString.writeAttribute(attrName, attrValue);
								
								// Check if we encounter the file namenode, optionally with given attributes
								if (localName.equals(condNodeForFilename)) {
									if (condAttrsForFilename != null && !condAttrsForFilename.isEmpty()) {
										Map<String, String> nodeAttributes = new HashMap<String, String>();
										nodeAttributes.put(attrName, attrValue);
										if (nodeAttributes.entrySet().containsAll(condAttrsForFilename.entrySet())) {
											isFileNameNode = true;									
										}
									} else {
										isFileNameNode = true;
									}
								}
							}
						}
					}
					break;
					
				case XMLStreamConstants.CHARACTERS:
					if (counter >= nodeCount) {
						// Write the text content
						String textContent = xsr.getText();
						xswString.writeCharacters(textContent);
						
						// If we encounter the filename node, set the filename for the splitted XML file accordingly
						if (isFileNameNode) {
							fileName.append(this.destinationDirectoryStr);	
							fileName.append(textContent);
							fileName.append(".xml");
							isFileNameNode = false; // Reset the filename flag
						}
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					String localNameEnd = xsr.getLocalName();

					if (counter >= nodeCount) {
						// Write the closing XML element
						xswString.writeEndElement();
					}

					if (localNameEnd.equals(nodeNametoExtract)) {
						counter --;

						// We are at the end at the splitted element, so we write it to a file
						if (counter == 0) {
							fw = new FileWriter(fileName.toString());
							fw.write(sw.toString());
							xswFile = xof.createXMLStreamWriter(fw);
							xswString.flush();
							xswFile.flush();
							fw.flush();
							fileName = null;
						} 
					}

					break;
				default:
					break;
				}
			}
			
			if (xswString != null) {
				xswString.close();
			}
			if (xswFile != null) {
				xswFile.close();
			}
			if (fw != null) {
				fw.close();
			}
			if (sw != null) {
				sw.close();
			}
			if (bis != null) {
				bis.close();
			}
			if (fis != null) {
				fis.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
}
