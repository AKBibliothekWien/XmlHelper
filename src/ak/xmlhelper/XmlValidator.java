package ak.xmlhelper;

import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlValidator {

	
	public boolean validateXML(String xmlFile)  {
		boolean returnValue = false;
		
		try {
			// Set up SAX-Parser and input source:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			FileReader reader = new FileReader(xmlFile);
			InputSource inputSource = new InputSource(reader);

			// Set ContentHandler:
			ValidationContentHandler validationContentHandler = new ValidationContentHandler();
			xmlReader.setContentHandler(validationContentHandler);

			// Start parsing:
			xmlReader.parse(inputSource);

			// End parsing:
			returnValue = true;
		} catch (SAXException e) {
			e.printStackTrace();
			returnValue = false;
			return returnValue;
		} catch (IOException e) {
			e.printStackTrace();
			returnValue = false;
			return returnValue;
		} catch (Exception e) {
			e.printStackTrace();
			returnValue = false;
			return returnValue;
		}
		return returnValue;
	}

	

	// Inner class for ContentHandler of XML-Parser:
	private class ValidationContentHandler implements ContentHandler {

		//private String nodeContent;
		private boolean isSYS;
		private int elementCounter = 0;


		@Override
		public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException {
			isSYS = false;
			if(localName.equals("controlfield")) {
				String tag = attribs.getValue("tag");
				isSYS = (tag.equals("SYS")) ? true : false;
			}
		}


		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {		
			if (isSYS == true) {
				elementCounter = elementCounter + 1;
			}
		}


		@Override public void characters(char[] ch, int start, int length) throws SAXException {}
		@Override public void startDocument() throws SAXException {}
		@Override public void endDocument() throws SAXException {}
		@Override public void setDocumentLocator(Locator locator) {}
		@Override public void startPrefixMapping(String prefix, String uri) throws SAXException {}
		@Override public void endPrefixMapping(String prefix) throws SAXException {}
		@Override public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
		@Override public void processingInstruction(String target, String data) throws SAXException {}
		@Override public void skippedEntity(String name) throws SAXException {}
	}

}