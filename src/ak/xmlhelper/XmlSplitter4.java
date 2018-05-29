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

public class XmlSplitter4 {

	private String systemTempDirPath = System.getProperty("java.io.tmpdir");
	private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
	private File destinationDirectory = null;
	private String destinationDirectoryStr = null;


	public XmlSplitter4(String destinationDirectoryPath) {
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
			int count = 0;
			FileWriter fw = null;
			StringWriter sw = null;
			StringBuilder fileName = null;

			while(xsr.hasNext()) {
				int e = xsr.next();

				switch (e) {
				case XMLStreamConstants.START_ELEMENT:
					String localName = xsr.getLocalName();

					if (localName.equals(nodeNametoExtract)) {
						count++;

						if (count == nodeCount) {
							fileName = new StringBuilder();
							sw = new StringWriter();
							xswString = xof.createXMLStreamWriter(sw);
						}
					}

					if (count >= nodeCount) {

						if (xswString != null) {
							xswString.writeStartElement(localName);
						}

						int noAttr = xsr.getAttributeCount();
						if (noAttr > 0) {
							for (int i = 0; i < noAttr; i++) {
								String attrName = xsr.getAttributeLocalName(i);
								String attrValue = xsr.getAttributeValue(i);
								xswString.writeAttribute(attrName, attrValue);
								
								// Get file name
								if (localName.equals(condNodeForFilename)) {
									String elementText = null;
									if (condAttrsForFilename != null && !condAttrsForFilename.isEmpty()) {
										Map<String, String> nodeAttributes = new HashMap<String, String>();
										
										nodeAttributes.put(attrName, attrValue);
										if (nodeAttributes.entrySet().containsAll(condAttrsForFilename.entrySet())) {
											elementText = xsr.getElementText();
											fileName.append(this.destinationDirectoryStr);	
											fileName.append(elementText);
											fileName.append(".xml");
										}
									} else {
										elementText = xsr.getElementText();
										fileName.append(this.destinationDirectoryStr);	
										fileName.append(elementText);
										fileName.append(".xml");
									}
								}
							}
						}
					}
					break;
					
				case XMLStreamConstants.CHARACTERS:
					if (count >= nodeCount) {
						xswString.writeCharacters(xsr.getText());
					}
					break;
					
				case XMLStreamConstants.END_ELEMENT:
					String localNameEnd = xsr.getLocalName();

					if (count >= nodeCount) {
						xswString.writeEndElement();
					}

					if (localNameEnd.equals(nodeNametoExtract)) {
						count --;

						if (count == 0) {
							xswString.writeEndElement(); // Closing record element
							
							fw = new FileWriter(fileName.toString());
							fw.write(sw.toString());
							xswFile = xof.createXMLStreamWriter(fw);
							xswString.flush();
							xswFile.flush();
							fileName = null;
						} 
					}

					break;
				default:
					break;
				}
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
