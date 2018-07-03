package ak.xmlhelper;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ak.xmlhelper.classes.XmlField;

public class XmlCounter {

	int noOfElements = 0;


	/**
	 * Counts XML elements by tag name. It's also possibly to limit the counter to attribute names and values.
	 * @param xmlFile		String. The full path to XML file with the elements that should be counted.
	 * @param tagNameCount	String. Tag name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use "name".
	 * @param attrName		String. The name of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="..." /> tags, use "attr".
	 * @param attrValue		String. The value of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="value" /> tags, use "value".
	 * @param print			boolean. Specify if you want to print the count process.
	 * @return				int. The no. of elements found.
	 */
	public int count(String xmlFile, String tagNameCount, String attrName, String attrValue, boolean print) {
		try {
			// Specify XML-file to parse
			FileReader reader = new FileReader(xmlFile);
			InputSource inputSource = new InputSource(reader);

			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			CounterContentHandler cch = new CounterContentHandler(tagNameCount, attrName, attrValue, null, null, "countSimple", null, print);
			xmlReader.setContentHandler(cch);
			xmlReader.parse(inputSource);
			noOfElements = cch.getNoOfElements();
			reader.close();
		} catch (SAXException e) {
			System.err.println("SAXException while counting");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not found when trying to run XML counter: " + xmlFile);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException while counting");
			e.printStackTrace();
		}

		return noOfElements;
	}


	/**
	 * Count how many specified XML elements does exist within another specified XML element and outputs the value of the XML needed. 
	 * @param xmlFile				String. The full path to XML file with the elements that should be counted.
	 * @param tagNameCount			String. Tag name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use "name".
	 * @param attrNameCount			String. The name of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="..." /> tags, use "attr".
	 * @param attrValueCount		String. The value of the attribute in the XML tag you want to count or "null". E. g. if you want to count all <name attr="value" /> tags, use "value".
	 * @param tagNameCountWithin	String. Count the elements defined before only if they are within this element. E. g. if you want to count all "name" tags within "record" tags in a structure like <record>...<name>...</name><name>...</name>...</record>, use "record"
	 * @param xmlFieldsOut			ArrayList<XmlField>. A list of XmlField objects that define which element(s) should be printed besides the element count.
	 * @param outFile				String. A path to a file to which the results should be printed. E. g. /home/username/myfile.txt
	 * @return						int. The total number found in all elements. The number of XML elements found within the other XML element is printed to the console or to the output file.
	 */
	public int countWithin(String xmlFile, String tagNameCount, String attrNameCount, String attrValueCount, String tagNameCountWithin, ArrayList<XmlField> xmlFieldsOut, String outFile) {
		try {
			// Specify XML-file to parse
			FileReader reader = new FileReader(xmlFile);
			InputSource inputSource = new InputSource(reader);
			
			// Create SAX parser:
			XMLReader xmlReader = XMLReaderFactory.createXMLReader();
			CounterContentHandler cch = new CounterContentHandler(tagNameCount, attrNameCount, attrValueCount, tagNameCountWithin, xmlFieldsOut, "countWithin", outFile, false);
			xmlReader.setContentHandler(cch);
			xmlReader.parse(inputSource);
			noOfElements = cch.getNoOfElements();
			reader.close();
		} catch (SAXException e) {
			System.err.println("SAXException while counting");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not found when trying to run XML counter: " + xmlFile);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException while counting");
			e.printStackTrace();
		}

		return noOfElements;
	}


	private class CounterContentHandler implements ContentHandler {

		private String tagNameCount;
		private String tagNameCountWithin;
		private ArrayList<XmlField> xmlFieldsOut;
		private ArrayList<String> xmlFieldsOutContents = new ArrayList<String>();
		private String attrName;
		private String attrValue;
		private String countType;
		private String elementContent;
		boolean print = false;
		private int noOfElements = 0;

		boolean isInCountWithin = false;
		int counterWithin = 0;
		boolean isXmlFieldOut = false;
		String outFile;
		BufferedWriter bufferFileWriter;

		private CounterContentHandler(String tagNameCount, String attrName, String attrValue, String tagNameCountWithin, ArrayList<XmlField> xmlFieldsOut, String countType, String outFile, boolean print) {
			this.tagNameCount = tagNameCount;
			this.tagNameCountWithin = tagNameCountWithin;
			this.xmlFieldsOut = xmlFieldsOut;
			this.attrName = attrName;
			this.attrValue = attrValue;
			this.countType = countType;
			this.outFile = outFile;
			this.print = print;
			
			// Set the output file
			if (outFile != null) {
				FileWriter fileWriter;
				
				try {
					fileWriter = new FileWriter(outFile, true);
					this.bufferFileWriter = new BufferedWriter(fileWriter);
				} catch (IOException e) {
					System.err.println("IOException while counting");
					e.printStackTrace();
				}
			}
		}

		@Override
		public void setDocumentLocator(Locator locator) {}

		@Override
		public void startDocument() throws SAXException {}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			// Clear the element content variable (= text of XML element). If not, there will be problems with html-encoded characters (&lt;) at character()-method:
			elementContent = "";

			if (this.countType.equals("countSimple")) {
				if (localName.equals(this.tagNameCount)) { // Tag name exists
					if (this.attrName != null && !this.attrName.isEmpty()) {
						if (attrs.getValue(this.attrName) != null) { // Attribute name exists
							if (attrs.getValue(this.attrName).equals(this.attrValue)) { // Attribute value exists
								noOfElements = noOfElements + 1;
								print("No of <" + this.tagNameCount + " " + this.attrName + "=\"" + attrs.getValue(this.attrName) + "\" ... />: " + noOfElements + "               \r");
							} else if (this.attrValue == null){
								noOfElements = noOfElements + 1;
								print("No of <" + this.tagNameCount + " " + this.attrName + "=\"...\" ... />: " + noOfElements + "               \r");
							}
						}
					} else if ((this.attrName == null || this.attrName.isEmpty()) && (this.attrValue == null || this.attrValue.isEmpty())) {
						noOfElements = noOfElements + 1;
						print("No of <" + this.tagNameCount + " ... />: " + noOfElements + "               \r");
					}
				}
				
			} else if (this.countType.equals("countWithin")) {

				if (localName.equals(this.tagNameCountWithin)) {
					this.isInCountWithin = true;
				}

				if (this.isInCountWithin) {
					if (localName.equals(this.tagNameCount)) {
						if (this.attrName != null && !this.attrName.isEmpty()) {
							if (attrs.getValue(this.attrName) != null) { // Attribute name exists
								if (attrs.getValue(this.attrName).equals(this.attrValue)) { // Attribute value exists
									this.counterWithin = this.counterWithin + 1;
									this.noOfElements = this.noOfElements + 1;
								} else if (this.attrValue == null){
									this.counterWithin = this.counterWithin + 1;
									this.noOfElements = this.noOfElements + 1;
								}
							}
						} else if ((this.attrName == null || this.attrName.isEmpty()) && (this.attrValue == null || this.attrValue.isEmpty())) {
							this.counterWithin = this.counterWithin + 1;
							this.noOfElements = this.noOfElements + 1;
						}
					}
					
					for(XmlField xmlField : this.xmlFieldsOut) {
						if (localName.equals(xmlField.getTagName())) {
							String attrName = xmlField.getAttrName();
							if (attrName != null && attrs.getValue(attrName) != null) { // Attribute name exists
								String attrValue = xmlField.getAttrValue();
								if (attrs.getValue(attrName).equals(attrValue)) { // Attribute value exists
									this.isXmlFieldOut = true;
								} else if (attrValue == null) {
									this.isXmlFieldOut = true;
								}
							} else {
								this.isXmlFieldOut = true;
							}
						}
					}
				}
			}
		}

		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			
			if (this.countType.equals("countWithin")) {
				
				if (this.isInCountWithin) {
					if (this.isXmlFieldOut) {
						// Get the text content of the XML element
						String content = elementContent.toString();
						xmlFieldsOutContents.add("\"" + content + "\"");
						this.isXmlFieldOut = false;
					}
				}
				
				if (localName.equals(this.tagNameCountWithin)) {
					String outStringToPrint = xmlFieldsOutContents.toString().replace("[", "").replace("]", "");

					if (this.outFile != null) {
						try {
							this.bufferFileWriter.write(outStringToPrint + ", " + this.counterWithin);
							this.bufferFileWriter.newLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println(outStringToPrint + ", " + this.counterWithin);
					}
					
					this.isInCountWithin = false;
					this.counterWithin = 0;
					xmlFieldsOutContents = new ArrayList<String>();
				}
			}
			
		}
		

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (this.countType.equals("countWithin")) {
				elementContent += new String(ch, start, length);
			}
		}
		
		
		@Override
		public void endDocument() throws SAXException {
			try {
				if (this.bufferFileWriter != null) {
					this.bufferFileWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
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
