/**
 * This file is part of GoobiOaiHelper.
 * 
 * GoobiOaiHelper is free software: you can redistribute it and/or modify
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GoobiOaiHelper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GoobiOaiHelper.  If not, see <http://www.gnu.org/licenses/>.
 */

package betullam.xmlhelper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class provides methods to get information out of an XML document.
 * 
 * @author Michael Birkner
 */
public class XmlParser {

	XPath xPath = XPathFactory.newInstance().newXPath();


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

}
