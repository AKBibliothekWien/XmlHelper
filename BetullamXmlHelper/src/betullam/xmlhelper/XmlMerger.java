package betullam.xmlhelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

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
import org.xml.sax.SAXException;

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
	 * Gets the node with the specified tag name. If there are multiple nodes with the same tag name,
	 * use nodeCount to specify which node to take (starting with 0). If only one node exists with the
	 * specified tag name, set nodeCount to 0.
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
	 * Checks a source directory for multiple XML files that should be merged togehter into one XML file. You may specify which nodes to copy from
	 * the old XML files to the new one that will be the result of the merging process. If there are multiple nested nodes with the same name, you
	 * can specify which one to take by the nodeCount parameter (if there is only one, use "0"). You also need to specify the name of the parent node,
	 * in which the copied nodes will be nested.
	 * 
	 * @param sourceDirectory	a String that specifies the full path to a directory with multiple XML files that should be merged.
	 * @param destinationFile	a String that specifies the full path and name of a XML file that is the result of the merging process (Warning: it cannot be in the same path as the source files!)
	 * @param parentNode		a String that specifies the parent node in which the merged nodes will be nested.
	 * @param nodeToMerge		a String that specifies the name of the nodes that should be copied from the old file to the new one.
	 * @param nodeCount			an Integer: Use 0 as default. If your source XML-files have nested nodes with the same name, use this to specify which node to take. 
	 */
	public void mergeElementNodes(String sourceDirectory, String destinationFile, String parentNode, String nodeToMerge, int nodeCount) {
		System.out.println("Started merging element nodes, please wait ...");
		File fSourceDirectory = new File(sourceDirectory);
		File fDestinationFile = new File(destinationFile);
		if (fSourceDirectory.getAbsolutePath().equals(fDestinationFile.getParent())) {
			System.out.println(Color.YELLOW + "WARNING: Stopped merging process.\nIt's not possible to save the destination file in the source directory. Please specify another path for your destination file!"  + Color.RESET);
			return;
		}
		
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(fDestinationFile.getAbsolutePath()));
			PrintWriter writer = new PrintWriter(out);
			
			writer.println("<" + parentNode + ">");
			for (File xmlFile : fSourceDirectory.listFiles()) {
				this.setDocument(xmlFile.getAbsolutePath());
				Node elementNode = this.getElementNode(nodeToMerge, nodeCount);
				writer.println(nodeToString(elementNode));
			}
			writer.println("</" + parentNode + ">");
			
			if (writer!=null) { writer.close(); }
			
			System.out.println("Merging done!");
		} catch (FileNotFoundException e) {
			System.out.print(Color.RED);
			e.printStackTrace();
			System.out.print(Color.RESET);
		}
		
		
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
			System.out.print(Color.RED);
			e.printStackTrace();
			System.out.print(Color.RESET);
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
			System.out.print(Color.RED);
			e.printStackTrace();
			System.out.print(Color.RESET);
		}
		
		return doc;
	}

}
