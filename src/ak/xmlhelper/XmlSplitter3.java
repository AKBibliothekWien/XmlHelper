package ak.xmlhelper;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XmlSplitter3 {

	private String systemTempDirPath = System.getProperty("java.io.tmpdir");
	private String tempDirPath = systemTempDirPath + File.separator + "xmlSplitted";
	private File destinationDirectory = null;


	public XmlSplitter3(String destinationDirectoryPath) {
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
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			XMLEventWriter xewString = null;
			XMLEventWriter xewFile = null;
			XMLEventReader xer = xif.createXMLEventReader(bis);
			int count = 0;
			int recordCounter = 0;
			FileWriter fw = null;
			StringWriter sw = null;
			BufferedWriter bw = null;
			String fileName = "TEST";
			boolean isFilenameNode = false;

			while(xer.hasNext()) {
				XMLEvent e = xer.nextEvent();

				if (e.isStartElement()) {
					StartElement startElement = e.asStartElement();
					String localName = startElement.getName().getLocalPart();

					if (localName.equals(nodeNametoExtract)) {
						count++;

						if (count == nodeCount) {
							recordCounter++;
							sw = new StringWriter();
							xewString = xof.createXMLEventWriter(sw);
						}
					}

					if (count >= nodeCount) {

						if (xewString != null) {
							xewString.add(e);
						}
																		
						// Get file name
						if (localName.equals(condNodeForFilename)) {

							Map<String, String> nodeAttributes = new HashMap<String, String>();

							if (condAttrsForFilename != null && !condAttrsForFilename.isEmpty()) {

								Iterator<?> attrs = startElement.getAttributes();
								while (attrs.hasNext()) {
									Attribute attr = (Attribute)attrs.next();
									nodeAttributes.put(attr.getName().getLocalPart(), attr.getValue());
									if (nodeAttributes.entrySet().containsAll(condAttrsForFilename.entrySet())) {
										isFilenameNode = true;
									}
								}
							} else {
								isFilenameNode = true;
							}
						}
					}

				} else if (e.isCharacters()) {

					if (count >= nodeCount) {
						xewString.add(e);

						if (isFilenameNode) {
							fileName = e.asCharacters().getData();
							isFilenameNode = false;
						}
					}

				} else if (e.isEndElement()) {
					String localName = ((EndElement)e).getName().getLocalPart();

					if (count >= nodeCount) {
						xewString.add(e);
					}

					if (localName.equals(nodeNametoExtract)) {
						count --;

						if (count == 0) {
							//System.out.println("File name: " + fileName);
							//System.out.println(sw.toString());
							fw = new FileWriter(this.destinationDirectory + File.separator + fileName + ".xml");
							fw.write(sw.toString());
							bw = new BufferedWriter(fw);
							xewFile = xof.createXMLEventWriter(bw);
							xewString.flush();
							xewFile.flush();
							fileName = null;
							//break; // Stops after the first <record> element
						} 
					}
				} else if (xewString != null && count >= nodeCount) {
					xewString.add(e);
				}
			}

			if (xewString != null) {
				xewString.close();
			}

			//System.out.println(sw);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}


}
