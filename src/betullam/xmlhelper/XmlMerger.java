package betullam.xmlhelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlMerger {

	Document document;

	/**
	 * Gets the last node with the specified tag name.
	 * @param	tagName		a String with the tag name
	 * @return				a Node object or null
	 */
	public Node getLastElementNode(String tagName) {
		Node returnNode = null;
		NodeList recordNodes = document.getElementsByTagName(tagName);		
		returnNode = recordNodes.item(recordNodes.getLength()-1);
		return returnNode;
	}


	/**
	 * Gets the node with the specified tag name. If there are nested nodes with the same tag name,
	 * use nodeCount to specify which node to take (starting with 0). If only one node exists with the
	 * specified tag name, set nodeCount to 0. ATTENTION: This will only merge if the XML file contains
	 * only 1 element!
	 * @param tagName		a String with the tag name.
	 * @param nodeCount 	an Integer with the number of the node
	 * @return
	 */
	public Node getElementNode(String tagName, int nodeCount) {
		Node returnNode = null;
		NodeList recordNodes = document.getElementsByTagName(tagName);		
		returnNode = recordNodes.item(nodeCount);
		return returnNode;
	}


	/**
	 * IMPORTANT: Works only wiht XML-files having only one element!
	 * Checks a source directory for multiple XML files that should be merged togehter into one XML file. You may specify which node to copy from
	 * the old XML files to the new one that will be the result of the merging process. If there are multiple nested nodes with the same name, you
	 * may specify which one to take by the nodeCount parameter (if there is only one, use "0"). You also need to specify the name of the parent node,
	 * in which the copied nodes will be nested.
	 * 
	 * @param sourceDirectory	a String that specifies the full path to a directory with multiple XML files that should be merged.
	 * @param destinationFile	a String that specifies the full path and name of a XML file that is the result of the merging process (Warning: it cannot be in the same path as the source files!)
	 * @param parentNode		a String that specifies the parent node in which the merged nodes will be nested.
	 * @param nodeToMerge		a String that specifies the name of the nodes that should be copied from the old file to the new one.
	 * @param nodeCount			an Integer: Use 0 as default. If your source XML-files have nested nodes with the same name, use this to specify which node to take.
	 * @return 					true if the merge was successful, false otherwise
	 */
	public boolean mergeElementNodes(String sourceDirectory, String destinationFile, String parentNode, String nodeToMerge, int nodeCount) {
		boolean isMergingSuccessful = false;
		//System.out.println("Started merging element nodes, please wait ...");
		File fSourceDirectory = new File(sourceDirectory);
		File fDestinationFile = new File(destinationFile);
		if (fSourceDirectory.getAbsolutePath().equals(fDestinationFile.getParent())) {
			System.err.println("WARNING: Stopped merging process.\nIt's not possible to save the destination file in the source directory. Please specify another path for your destination file!");
			return isMergingSuccessful;
		}

		if (!fSourceDirectory.exists()) {
			System.err.println("WARNING: Stopped merging process.\nDirectory with multiple xml files does not exist!");
			return isMergingSuccessful;
		}

		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(fDestinationFile.getAbsolutePath()));
			PrintWriter writer = new PrintWriter(out);

			//int counter = 0;
			writer.println("<" + parentNode + ">");
			File[] files = fSourceDirectory.listFiles();
			Arrays.sort(files);
			for (File xmlFile : files) {

				this.setDocument(xmlFile.getAbsolutePath());
				Node elementNode = this.getElementNode(nodeToMerge, nodeCount);
				writer.println(nodeToString(elementNode));

				/*
				List<Node> elementNodeList = this.getElementNode(nodeToMerge, nodeCount);
				for (Node elementNode : elementNodeList) {
					counter = counter + 1;
					writer.println(nodeToString(elementNode));
				}
				 */
			}
			writer.println("</" + parentNode + ">");

			if (writer!=null) { writer.close(); }
			isMergingSuccessful = true;
			//System.out.println("Merging done!");
		} catch (FileNotFoundException e) {
			isMergingSuccessful = false;
			e.printStackTrace();
		}

		return isMergingSuccessful;

	}





	// TODO: Merge ALL nodes of an XML file (see example code below), not only the first one that is found.
	// But it could be a problem with nested nodes (nodeCount).
	// MERGES MULTIPLE NODES PER XML FILE - PROBLEM WITH nodeCount!
	public List<Node> getMultipleElementNode(String tagName) {
		List<Node> returnNodeList = new ArrayList<Node>();
		NodeList recordNodes = document.getElementsByTagName(tagName);

		int noOfElements = recordNodes.getLength();

		for (int i = 0; i < noOfElements; i++) {
			Node currentNode = recordNodes.item(i);
			returnNodeList.add(currentNode);

		}

		return returnNodeList;
	}

	/**
	 * IMPORTANT: Works with multiple nodes per XML file, but if there are nested nodes of the same name, you can not specify which to take!
	 * Checks a source directory for multiple XML files that should be merged togehter into one XML file.
	 * 
	 * @param sourceDirectory	a String that specifies the full path to a directory with multiple XML files that should be merged.
	 * @param destinationFile	a String that specifies the full path and name of a XML file that is the result of the merging process (Warning: it cannot be in the same path as the source files!)
	 * @param parentNode		a String that specifies the parent node in which the merged nodes will be nested.
	 * @param nodeToMerge		a String that specifies the name of the nodes that should be copied from the old file to the new one.
	 * @return 					true if the merge was successful, false otherwise
	 */
	public boolean mergeMultipleElementNodes(String sourceDirectory, String destinationFile, String parentNode, String nodeToMerge) {
		boolean isMergingSuccessful = false;
		//System.out.println("Started merging element nodes, please wait ...");
		File fSourceDirectory = new File(sourceDirectory);
		File fDestinationFile = new File(destinationFile);
		if (fSourceDirectory.getAbsolutePath().equals(fDestinationFile.getParent())) {
			System.err.println("WARNING: Stopped merging process.\nIt's not possible to save the destination file in the source directory. Please specify another path for your destination file!");
			return isMergingSuccessful;
		}

		if (!fSourceDirectory.exists()) {
			System.err.println("WARNING: Stopped merging process.\nDirectory with multiple xml files does not exist!");
			return isMergingSuccessful;
		}

		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(fDestinationFile.getAbsolutePath()));
			PrintWriter writer = new PrintWriter(out);

			//int counter = 0;
			writer.println("<" + parentNode + ">");
			File[] files = fSourceDirectory.listFiles();
			Arrays.sort(files);
			for (File xmlFile : files) {

				this.setDocument(xmlFile.getAbsolutePath());
				List<Node> elementNodeList = this.getMultipleElementNode(nodeToMerge);
				for (Node elementNode : elementNodeList) {
					writer.println(nodeToString(elementNode));
				}

			}
			writer.println("</" + parentNode + ">");

			if (writer!=null) { writer.close(); }
			isMergingSuccessful = true;
			//System.out.println("Merging done!");
		} catch (FileNotFoundException e) {
			isMergingSuccessful = false;
			e.printStackTrace();
		}

		return isMergingSuccessful;

	}


	// TODO: Merge elements using a SAX Parser
	public boolean mergeElements(String sourceDirectory, String destinationFile, String parentElement, String elementToMerge, int elementCount) {
		boolean isMergingSuccessful = false;

		File fSourceDirectory = new File(sourceDirectory);
		File fDestinationFile = new File(destinationFile);
		if (fSourceDirectory.getAbsolutePath().equals(fDestinationFile.getParent())) {
			System.err.println("WARNING: Stopped merging process.\nIt's not possible to save the destination file in the source directory. Please specify another path for your destination file!");
			return isMergingSuccessful;
		}

		if (!fSourceDirectory.exists()) {
			System.err.println("WARNING: Stopped merging process.\nDirectory with multiple xml files does not exist!");
			return isMergingSuccessful;
		}

		try {
			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			// Set ContentHandler:
			XmlContentHandler xmlContentHandler = new XmlContentHandler(elementToMerge);
			xmlReader.setContentHandler(xmlContentHandler);

			OutputStream out = new BufferedOutputStream(new FileOutputStream(fDestinationFile.getAbsolutePath()));
			PrintWriter writer = new PrintWriter(out);

			//int counter = 0;
			writer.println("<" + parentElement + ">");
			File[] files = fSourceDirectory.listFiles();
			Arrays.sort(files);
			
			for (File xmlFile : files) {
				// Specify XML-file to parse.
				FileReader reader = new FileReader(xmlFile);
				InputSource inputSource = new InputSource(reader);

				// Start parsing & indexing:
				xmlReader.parse(inputSource);
			}
			writer.println("</" + parentElement + ">");

			if (writer!=null) { writer.close(); }
			isMergingSuccessful = true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		return isMergingSuccessful;


	}

	public void setDocument(String xmlFile) {
		this.document = getXmlDocument(new File(xmlFile));
	}


	public Document getDocument() {
		return document;
	}


	private String nodeToString(Node node) {
		String xmlString = null;
		try {
			StringWriter stringWriter = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
			xmlString = stringWriter.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return xmlString;
	}



	private Document getXmlDocument(File xmlFile) {
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		return doc;
	}

	private class XmlContentHandler implements ContentHandler {

		String elementToMerge;
		String elementContent;
		int elementCounter = 0;
		
		private XmlContentHandler(String elementToMerge) {
			this.elementToMerge = elementToMerge;
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
				elementCounter = elementCounter + 1;
				System.out.println("Start: " + elementToMerge + " is on level " + elementCounter);
			}
		}

		/**
		 * Encounters end of element.<br><br>
		 * {@inheritDoc}
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String content = elementContent.toString();
			
			
			if(localName.equals(elementToMerge) ) {
				elementCounter = elementCounter - 1;
				System.out.println("End: " + elementToMerge + " is on level " + elementCounter);
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
		public void setDocumentLocator(Locator locator) {
		}

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
		}

	}

}
