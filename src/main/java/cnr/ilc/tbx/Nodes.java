package cnr.ilc.tbx;
import org.w3c.dom.*;

public class Nodes {

	static public String getTextOfTag(Element root, String tagName) {
		if (root == null) return null;

		String text = "";
		NodeList nodeList = root.getElementsByTagNameNS("*", tagName);
		for (int k = 0; k < nodeList.getLength(); ++k) {
			Element element = (Element) nodeList.item(k);
			text += (text.length() > 0? "\n" : "") + element.getTextContent();		
		}
		return text.length() == 0? null : text;
	}

	static public String getTextOfTagWithAttribute(Element root, String tagName, String attributeName, String attributeValue) {
		NodeList elements = root.getElementsByTagNameNS("*", tagName);
		for (int k = 0; k < elements.getLength(); ++k) {
			Element element = (Element) elements.item(k);
			String value = element.getAttribute(attributeName);
			if (value.equals(attributeValue)) return element.getTextContent();
		}
		return null;
	}

	static public String getTextOfTagOrAlternateTagWithAttribute(Element root, String tagName, String alternateTagName, String attributeName) {
		String text = getTextOfTag(root, tagName);
		if (text == null) {
			text = getTextOfTagWithAttribute(root, alternateTagName, attributeName, tagName);
		}
		return text;
	}

	static public void removeNodesFromParsingTree(NodeList nodes) {
		for (int k = nodes.getLength(); k > 0; --k)  {
			Node node = nodes.item(k-1);
			node.getParentNode().removeChild(node);
		}
	}

}
