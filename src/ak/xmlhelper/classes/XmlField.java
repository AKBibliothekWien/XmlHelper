package ak.xmlhelper.classes;

public class XmlField {
	
	private String tagName;
	private String attrName;
	private String attrValue;

	public XmlField(String tagName, String attrName, String attrValue) {
		this.tagName = tagName;
		this.attrName = attrName;
		this.attrValue = attrValue;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	@Override
	public String toString() {
		return "XmlField [tagName=" + tagName + ", attrName=" + attrName + ", attrValue=" + attrValue + "]";
	}
	

}