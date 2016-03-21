package betullam.xmlhelper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlCounter {

	int noOfElements = 0;
	
	/**
	 * Counts XML elements by tag name. It's also possibly to limit the counter to attribute names and values.
	 * @param xmlFile		String. The full path to XML file with the elements that should be counted.
	 * @param tagName		String. Tag name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use "name".
	 * @param attrName		String. The name of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="..." /> tags, use "attr".
	 * @param attrValue		String. The value of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="value" /> tags, use "value".
	 * @param print			boolean. Specify if you want to print the count process.
	 * @return				int. The no. of elements found.
	 */
	public int count(String xmlFile, String tagName, String attrName, String attrValue, boolean print) {
		try {
			// Specify XML-file to parse
			FileReader reader = new FileReader(xmlFile);
			InputSource inputSource = new InputSource(reader);

			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			CounterContentHandler cch = new CounterContentHandler(tagName, attrName, attrValue, print);
			xmlReader.setContentHandler(cch);
			xmlReader.parse(inputSource);
			noOfElements = cch.getNoOfElements();
			reader.close();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return noOfElements;
	}



	private class CounterContentHandler implements ContentHandler {

		private String tagName;
		private String attrName;
		private String attrValue;
		boolean print = false;
		private int noOfElements = 0;

		private CounterContentHandler(String tagName, String attrName, String attrValue, boolean print) {
			this.tagName = tagName;
			this.attrName = attrName;
			this.attrValue = attrValue;
			this.print = print;
		}

		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void endDocument() throws SAXException {}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			if (localName.equals(this.tagName)) { // Tag name exists
				if (this.attrName != null && !this.attrName.isEmpty()) {
					if (attrs.getValue(this.attrName) != null) { // Attribute name exists
						if (attrs.getValue(this.attrName).equals(this.attrValue)) { // Attribute value exists
							noOfElements = noOfElements + 1;
							print("No of <" + this.tagName + " " + this.attrName + "=\"" + attrs.getValue(this.attrName) + "\" ... />: " + noOfElements + "               \r");
						} else if (this.attrValue == null){
							noOfElements = noOfElements + 1;
							print("No of <" + this.tagName + " " + this.attrName + "=\"...\" ... />: " + noOfElements + "               \r");
						}
					}
				} else if ((this.attrName == null || this.attrName.isEmpty()) && (this.attrValue == null || this.attrValue.isEmpty())) {
					noOfElements = noOfElements + 1;
					print("No of <" + this.tagName + " ... />: " + noOfElements + "               \r");
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {}

		@Override
		public void skippedEntity(String name) throws SAXException {}
		
		public int getNoOfElements() {
			return this.noOfElements;
		}
		
		private void print(String text) {
			if (this.print) {
				System.out.print(text);
			}
		}

	}
}


