package ak.xmlhelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class XmlMerger {

	public boolean mergeElements(String sourceDirectory, String destinationFile, String parentNode, String mergeNode, int mergeNodeLevel, String parentNodeAttr, String mergeNodeAttr) {
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

			// If we should write custom attributes to the merge node, get them now
			List<CustomAttribute> customMergeNodeAttrs = null;
			if (mergeNodeAttr != null && !mergeNodeAttr.isEmpty()) {

				customMergeNodeAttrs = new ArrayList<CustomAttribute>();
				String[] mergeNodeAttrSplitted = mergeNodeAttr.split("\\s*;\\s*");

				for (String customMergeAttr : mergeNodeAttrSplitted) {
					String[] customMergeAttrSplitted = customMergeAttr.split("\\s*,\\s*");
					int customMergeAttrSplittedLength = customMergeAttrSplitted.length;
					if (customMergeAttrSplittedLength >= 2 && customMergeAttrSplittedLength <= 4) {

						CustomAttribute customMergeAttribute = new CustomAttribute();

						customMergeAttribute.setNoOfParts(customMergeAttrSplittedLength);
						customMergeAttribute.setLocalName(customMergeAttrSplitted[0]);
						customMergeAttribute.setValue(customMergeAttrSplitted[1]);
						if (customMergeAttrSplittedLength >= 3) {
							customMergeAttribute.setNamespaceUri(customMergeAttrSplitted[2]);
						}
						if (customMergeAttrSplittedLength == 4) {
							customMergeAttribute.setPrefix(customMergeAttrSplitted[3]);
						}
						customMergeNodeAttrs.add(customMergeAttribute);
					}
				}
			}


			// If we should write custom attributes to the parent node, get them now
			List<CustomAttribute> customParentNodeAttrs = null;
			if (parentNodeAttr != null && !parentNodeAttr.isEmpty()) {
				customParentNodeAttrs = new ArrayList<CustomAttribute>();
				String[] parentNodeAttrSplitted = parentNodeAttr.split("\\s*;\\s*");
				for (String parentAttr : parentNodeAttrSplitted) {

					String[] customParentAttrSplitted = parentAttr.split("\\s*,\\s*");
					int customParentAttrSplittedLength = customParentAttrSplitted.length;

					if (customParentAttrSplittedLength >= 2 && customParentAttrSplittedLength <= 4) {
						CustomAttribute customParentAttribute = new CustomAttribute();
						customParentAttribute.setNoOfParts(customParentAttrSplittedLength);
						customParentAttribute.setLocalName(customParentAttrSplitted[0]);
						customParentAttribute.setValue(customParentAttrSplitted[1]);
						if (customParentAttrSplittedLength >= 3) {
							customParentAttribute.setNamespaceUri(customParentAttrSplitted[2]);
						}
						if (customParentAttrSplittedLength == 4) {
							customParentAttribute.setPrefix(customParentAttrSplitted[3]);
						}
						customParentNodeAttrs.add(customParentAttribute);
					}
				}
			}

			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
			xof.setProperty("javax.xml.stream.isRepairingNamespaces", true);
			FileWriter fw = new FileWriter(fDestinationFile.getAbsolutePath());
			XMLStreamWriter xsw = xof.createXMLStreamWriter(fw);
			XMLStreamReader xsr = null;
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			int counter = 0;

			// Write XML declaration
			xsw.writeStartDocument("UTF-8", "1.0");

			// Write overall enclosing XML element.
			xsw.writeStartElement(parentNode);

			// If custom attribute/s is/are given for the parent node in the config file, write it/them now
			// TODO: It's possible that a separation of writing the namespace and writing attributes is necessary!
			if (customParentNodeAttrs != null && customParentNodeAttrs.isEmpty()) {
				for (int i = 0; i < customParentNodeAttrs.size(); i++) {
					CustomAttribute customParentAttr = customParentNodeAttrs.get(i);
					String attrLocalName = customParentAttr.getLocalName();
					String attrValue = customParentAttr.getValue();
					int attrNoOfParts = customParentAttr.getNoOfParts();

					if (attrNoOfParts == 2) {
						xsw.writeAttribute(attrLocalName, attrValue);
					} else if (attrNoOfParts > 2) {
						String attrNamespaceUri = customParentAttr.getNamespaceUri();
						if (attrNoOfParts == 3) {
							xsw.writeAttribute(attrNamespaceUri, attrLocalName, attrValue);
						}
						if (attrNoOfParts == 4) {
							String attrPrefix = customParentAttr.getPrefix();
							xsw.writeAttribute(attrPrefix, attrNamespaceUri, attrLocalName, attrValue);
						}
					} 
				}
			}			

			// Iterate over all XML files
			for (File xmlFile : files) {

				// Set streams and readers
				fis = new FileInputStream(xmlFile);
				bis = new BufferedInputStream(fis);
				xsr = xif.createXMLStreamReader(bis);

				// Iterate over XML reader events
				while(xsr.hasNext()) {
					int e = xsr.next();

					switch (e) {
					case XMLStreamConstants.START_ELEMENT:

						// Get the name and namespaces of the current opening XML element
						String localName = xsr.getName().getLocalPart();
						String namespacePrefix = xsr.getName().getPrefix();
						String namespaceUri = xsr.getName().getNamespaceURI();

						// Check if the current opening XML element is the one that should be merged
						if (localName.equals(mergeNode)) {
							counter++;
						}

						if (counter >= mergeNodeLevel) {

							// Write the current opening XML element (with or without namespaces)
							if (!namespacePrefix.isEmpty() && !namespaceUri.isEmpty()) {
								xsw.writeStartElement(namespacePrefix, localName, namespaceUri);
							} else {
								xsw.writeStartElement(localName);
							}

							// Check if the current opening XML element has attributes
							int noAttr = xsr.getAttributeCount();
							if (noAttr > 0) {

								// If we encounter attributes, write them to the opening XML element
								for (int i = 0; i < noAttr; i++) {
									String attrLocalName = xsr.getAttributeName(i).getLocalPart();
									String attrNamespacePrefix = xsr.getAttributeName(i).getPrefix();
									String attrNamespaceUri = xsr.getAttributeName(i).getNamespaceURI();
									String attrValue = xsr.getAttributeValue(i);

									if (!attrNamespacePrefix.isEmpty() && !attrNamespaceUri.isEmpty()) {
										xsw.writeAttribute(attrNamespacePrefix, attrNamespaceUri, attrLocalName, attrValue);
									} else {
										xsw.writeAttribute(attrLocalName, attrValue);
									}
								}
							}

							if (localName.equals(mergeNode)) {
								// If custom attribute/s is/are given for the "node-to-merge" in the config file, write it/them now
								// TODO: It's possible that a separation of writing the namespace and writing attributes is necessary!
								if (customMergeNodeAttrs != null && !customMergeNodeAttrs.isEmpty()) {
									for (int i = 0; i < customMergeNodeAttrs.size(); i++) {
										CustomAttribute customAttr = customMergeNodeAttrs.get(i);
										String attrLocalName = customAttr.getLocalName();
										String attrValue = customAttr.getValue();
										int attrNoOfParts = customAttr.getNoOfParts();

										if (attrNoOfParts == 2) {
											xsw.writeAttribute(attrLocalName, attrValue);
										} else if (attrNoOfParts > 2) {
											String attrNamespaceUri = customAttr.getNamespaceUri();
											if (attrNoOfParts == 3) {
												xsw.writeAttribute(attrNamespaceUri, attrLocalName, attrValue);
											}
											if (attrNoOfParts == 4) {
												String attrPrefix = customAttr.getPrefix();
												xsw.writeAttribute(attrPrefix, attrNamespaceUri, attrLocalName, attrValue);
											}
										} 
									}
								}
							}

						}
						break;

					case XMLStreamConstants.CHARACTERS:
						if (counter >= mergeNodeLevel) {
							// Write the text content
							xsw.writeCharacters(xsr.getText());
						}
						break;

					case XMLStreamConstants.END_ELEMENT:
						String localNameEnd = xsr.getLocalName();

						if (counter >= mergeNodeLevel) {
							// Write the end element
							xsw.writeEndElement();
						}

						if (localNameEnd.equals(mergeNode)) {
							counter --;

							if (counter == 0) {
								// Output to file
								xsw.flush();
								fw.flush();
							} 
						}
						break;
					default:
						break;
					}
				}
			}

			xsw.writeEndElement();
			xsw.writeEndDocument();

			if (xsw != null) {
				xsw.close();
			}
			if (fw != null) {
				fw.close();
			}
			if (xsr != null) {
				xsr.close();
			}
			if (bis != null) {
				bis.close();
			}
			if (fis != null) {
				fis.close();
			}

			isMergingSuccessful = true;

		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException when processing file: " + fileName);
			e.printStackTrace();
		} catch (XMLStreamException e) {
			System.err.println("XMLStreamException when streaming file: " + fileName);
			e.printStackTrace();
		}

		return isMergingSuccessful;
	}


	private class CustomAttribute {
		private String prefix = null;
		private String namespaceUri = null;
		private String localName = null;
		private String value = null;
		private int noOfParts = 0;

		public CustomAttribute() {}

		public String getPrefix() {
			return prefix;
		}
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
		public String getNamespaceUri() {
			return namespaceUri;
		}
		public void setNamespaceUri(String namespaceUri) {
			this.namespaceUri = namespaceUri;
		}
		public String getLocalName() {
			return localName;
		}
		public void setLocalName(String localName) {
			this.localName = localName;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public int getNoOfParts() {
			return noOfParts;
		}
		public void setNoOfParts(int noOfParts) {
			this.noOfParts = noOfParts;
		}

		@Override
		public String toString() {
			return "CustomAttribute [prefix=" + prefix + ", namespaceUri=" + namespaceUri + ", localName=" + localName
					+ ", value=" + value + ", noOfParts=" + noOfParts + "]";
		}
	}


}

