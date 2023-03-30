package cnr.ilc.tbx;
import org.w3c.dom.*;

public class Nodes {

	static public String getTextOfTag(Element root, String tagName) {
		if (root == null) return null;
		Element element = (Element) root.getElementsByTagNameNS("*", tagName).item(0);
		return element == null? null : element.getTextContent();
	}

	static public void removeNodesFromParsingTree(NodeList nodes) {
		for (int k = 0; k < nodes.getLength(); ++k)  {
			Node node = nodes.item(k);
			node.getParentNode().removeChild(node);
		}
	}

}
