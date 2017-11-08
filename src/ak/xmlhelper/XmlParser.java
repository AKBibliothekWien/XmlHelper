package ak.xmlhelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathResult;

/**
 * This class provides methods to get information out of an XML document.
 * 
 * @author Michael Birkner
 */
public class XmlParser {

	XPath xPath = XPathFactory.newInstance().newXPath();

	
	/**
	 * Get the result of a XPath expression as List<String>
	 * @param document						the xml document that contains the element to parse
	 * @param xpath							the xpath which leads to the element in the XML document for which the value should be returned
	 * @param returnNull					a boolean indicating wether to return null or not if no match was found
	 * @return								a List<String> containing one or more results or null if no result is found.
	 * @throws XPathExpressionException
	 */
	public List<String> getXpathResult(Document document, String xpath, boolean returnNull) throws XPathExpressionException {

		List<String> xpathResults = new ArrayList<String>();
		//xPath.setNamespaceContext(new Namespaces(document));
		
		// Set namespaces to root element
		Namespaces namespaces = new Namespaces(document);
		Element rootElement = (Element)document.getFirstChild();
		for (Entry<String, String> namespace : namespaces.namesValues.entrySet()) {
			rootElement.setAttribute("xmlns:" + namespace.getKey(), namespace.getValue());
		}
		
		// Evaluate xPath with Xalan. This is because we can use "ANY_TYPE" as a return type and check later for the real return type.
		// This is necessary to for generic use of different xPath expressions, e. g. concat() returns String and not Element-Nodes.
		XPathEvaluatorImpl xPathEvaluatorImpl = new XPathEvaluatorImpl();
		
		XPathResult result = (XPathResult)xPathEvaluatorImpl.evaluate(xpath, document, xPathEvaluatorImpl.createNSResolver(document), org.w3c.dom.xpath.XPathResult.ANY_TYPE, null);
		short resultType = result.getResultType();
		
		if (resultType == org.w3c.dom.xpath.XPathResult.UNORDERED_NODE_ITERATOR_TYPE) {
			Node node = null;
			while ((node = result.iterateNext()) != null) {
				if (isTextNode(node)) {
					String textNodeValue = (node.getNodeValue() != null) ? node.getNodeValue().trim() : null;
					if (!returnNull && textNodeValue != null && !textNodeValue.trim().isEmpty()) {
						xpathResults.add(textNodeValue);
					} else if (returnNull) {
						xpathResults.add(null);
					}
				} else {
					NodeList nodeList = node.getChildNodes();
					if (nodeList.getLength() > 0) {
						for (int i = 0; i < nodeList.getLength(); i++) {
							String xpathResult = (nodeList.item(i).getTextContent() == null || nodeList.item(i).getTextContent().trim().isEmpty()) ? null : nodeList.item(i).getTextContent().trim();
							if (!returnNull && xpathResult != null && !xpathResult.trim().isEmpty()) {
								xpathResults.add(xpathResult);
							} else if (returnNull) {
								xpathResults.add(null);
							}
						}
					}
				}
			}
		} else if (resultType == org.w3c.dom.xpath.XPathResult.STRING_TYPE) {
			String resultStringValue = (result.getStringValue() != null) ? result.getStringValue().trim() : null;
			if (!returnNull && resultStringValue != null && !resultStringValue.trim().isEmpty()) {
				xpathResults.add(resultStringValue);
			} else if (returnNull) {
				xpathResults.add(null);
			}
		} else if (resultType == org.w3c.dom.xpath.XPathResult.NUMBER_TYPE) {
			xpathResults.add(String.valueOf(result.getNumberValue()));
		}

		if (xpathResults.isEmpty()) {
			xpathResults = null;
		}
		
		return xpathResults;
	}
	
	
	/**
	 * Get a W3C DOM Node by using xPath
	 * @param document		A W3C DOM document
	 * @param xPathString	The xPath to use
	 * @return				A W3C DOM Node
	 * @throws XPathExpressionException
	 */
	public Node getNodeByXpath(Document document, String xPathString) throws XPathExpressionException {		
		xPath.setNamespaceContext(new Namespaces(document));
		XPathExpression xPathExpression = xPath.compile(xPathString);
		Node result = (Node)xPathExpression.evaluate(document, XPathConstants.NODE);
		return result;
	}

	
	/**
	 * Check if the node is a Text node
	 * 
	 * @param 	node		Node object
	 * @return	boolean
	 */
	static boolean isTextNode(Node node) {
		boolean returnValue = false;
		if (node != null) {
			short nodeType = node.getNodeType();
			if (nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE) {
				returnValue = true;
			} else {
				returnValue = false;
			}
		}
		return returnValue;
	}

	
	/**
	 * Gets the text value (content) of one XML element. If xpath-expression finds more than one element, only the first text-value will be returned. Returns null if nothing was found.
	 * 
	 * @param document	the xml document that contains the element to parse
	 * @param xpath		the xpath which leads to the element in the XML document for which the text value should be returned
	 * @return			a String or null if nothing was found
	 * @throws XPathExpressionException
	 */
	public String getTextValue(Document document, String xpath) throws XPathExpressionException {
		String textValue = null;
		XPathExpression xPathExpression = xPath.compile(xpath+"/text()");
		NodeList nodeList = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);

		// Check if nodeList contains nodes to prevent NullPointerException for nodes with no text (for them, text() is not applicable):
		if (nodeList.getLength() > 0) {
			textValue = (nodeList.item(0).getNodeValue().trim().isEmpty()) ? null : nodeList.item(0).getNodeValue().trim();
		}

		return textValue;
	}

	
	/**
	 * Gets the text value (content) of one or more XML elements. If xpath-expression finds more than one element, all text-values will be returned in List<String>. Returns null if nothing was found.
	 * 
	 * @param document			the xml document that contains the elements to parse
	 * @param xpath				the xpath which leads to the elements in the XML document for which the text value should be returned
	 * @return List<String>		a List<String> or null if nothing was found
	 * @throws XPathExpressionException
	 */
	public List<String> getTextValues(Document document, String xpath) throws XPathExpressionException {
		List<String> textValues = new ArrayList<String>();
		String textValue = null;

		XPathExpression xPathExpression = xPath.compile(xpath+"/text()");
		NodeList nodeList = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);

		// Check if nodeList contains nodes to prevent NullPointerException for nodes with no text (for them, text() is not applicable):
		if (nodeList.getLength() > 0) {
			for(int i = 0; i < nodeList.getLength(); i++) {
				textValue = (nodeList.item(i).getNodeValue().trim().isEmpty()) ? null : nodeList.item(i).getNodeValue().trim();
				textValues.add(textValue);
			}
		} else {
			textValues = null;
		}

		return textValues;
	}


	/**
	 * Gets the value of an attribute of one XML element as a String. If more than one XML element is found with xpath expression, only the attribute value of the first one is returned.
	 *   
	 * @param document		the xml document that contains the element to parse
	 * @param xpath			the xpath which leads to the element in the XML document for which the attribute value should be returned
	 * @param attribute		the attribute for which the value should be returned
	 * @return String		a String conaining the attribute value or null if nothing was found
	 * @throws XPathExpressionException 
	 */
	public String getAttributeValue(Document document, String xpath, String attribute) throws XPathExpressionException {
		String attributeValue = null;
		XPathExpression xPathExpression = xPath.compile(xpath);
		NodeList nodeList = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);

		// Check if nodeList contains nodes to prevent NullPointerException. Instead of exception, just null should be returned.
		if (nodeList.getLength() > 0) {
			Element element = (Element)nodeList.item(0);
			// Check if attribute exists.
			boolean hasAttribute = element.hasAttribute(attribute);
			if (hasAttribute) {
				attributeValue = element.getAttribute(attribute);
			}
		}
		return attributeValue;
	}


	/**
	 * Gets the value of an attribute of one or more XML elements in a List<String>. If more than one element is found by xpath-expression, all values are returned.
	 * 
	 * @param document 			the xml document that contains the elements to parse
	 * @param xpath				the xpath which leads to the elements in the XML document for which the attribute values should be returned
	 * @param attribute			the attribute for which the values should be returned
	 * @return List<String>		a List<String> conaining the attribute value or null if nothing was found
	 * @throws XPathExpressionException 
	 */
	public List<String> getAttributeValues(Document document, String xpath, String attribute) throws XPathExpressionException {
		List<String> attributeValues = new ArrayList<String>();
		XPathExpression xPathExpression = xPath.compile(xpath);
		NodeList nodeList = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);

		// Check if nodeList contains nodes to prevent NullPointerException. Instead of exception, just null should be returned.
		if (nodeList.getLength() > 0) {
			// Iterate over node-list, get attribute-values and add them to a list:
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element)nodeList.item(i);
				String attributeValue = null;
				boolean hasAttribute = element.hasAttribute(attribute);
				// Check if attribute exists.
				if (hasAttribute) {
					attributeValue = element.getAttribute(attribute);
					attributeValues.add(attributeValue);
				}
			}
		} else {
			attributeValues = null;
		}

		return attributeValues;
	}

	
	/**
	 * Gets the number of nods of the given xPath expression. 
	 * 
	 * @param document						the xml document that nodes to count
	 * @param xpath							the xpath which leads to the elements in the XML document that should be counted
	 * @return int							an int that indicates how many nodes were counted
	 * @throws XPathExpressionException
	 */
	public int countNodes(Document document, String xpath) throws XPathExpressionException {
		int noOfNodes = 0;
		XPathExpression xPathExpression = xPath.compile(xpath);
		NodeList nodeList = (NodeList)xPathExpression.evaluate(document, XPathConstants.NODESET);
		noOfNodes = nodeList.getLength();
		return noOfNodes;
	}



	/**
	 * Namespace class
	 * @author Michael Birkner
	 *
	 */
	public class Namespaces implements NamespaceContext {
		private Map<String, String> valuesNames = new HashMap<String, String>();
		private Map<String, String> namesValues = new HashMap<String, String>();

		public Namespaces(Document doc) {
			checkNodeForNS(doc.getFirstChild());
		}

		private void checkNodeForNS(Node startNode) {
			NamedNodeMap attrs = startNode.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				storeNamespaces((Attr) attr);
			}

			NodeList childNodes = startNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					checkNodeForNS(childNode);
				}
			}
		}

		private void storeNamespaces(Attr attr) {
			if (attr.getNamespaceURI() != null && attr.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
				if (attr.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
					putNamespaces("NoNS", attr.getNodeValue());
				} else {
					putNamespaces(attr.getLocalName(), attr.getNodeValue());
				}
			}
		}

		private void putNamespaces(String nodeName, String nodeValue) {
			namesValues.put(nodeName, nodeValue);
			valuesNames.put(nodeValue, nodeName);
		}

		public String getNamespaceURI(String nsPrefix) {
			if (nsPrefix == null || nsPrefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
				return namesValues.get("NoNS");
			} else {
				return namesValues.get(nsPrefix);
			}
		}

		/**
		 * Methods not needed
		 */
		public String getPrefix(String namespaceURI) { return valuesNames.get(namespaceURI); }
		public Iterator<?> getPrefixes(String namespaceURI) { return null; }
	}
}