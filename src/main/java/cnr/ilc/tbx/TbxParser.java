package cnr.ilc.tbx;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.Metadata;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.Concept;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.Word;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class TbxParser implements ParserInterface, ResourceInterface {
	private Document document;
	private ConceptEntry conceptEntryParser = new ConceptEntry();
	private Map<String, String> lexicons = new HashMap<>();
	private Metadata metadata = new Metadata();
	private Collection<Concept> concepts = new ArrayList<>();
	private String creator;

    public TbxParser(InputStream inputStream, String creator) throws Exception {
		this.creator = creator;
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		metadata.putInMap("*", countingInputStream.getCount(), "fileSize");
		metadata.putInMap("*", "tbx", "fileType");
		metadata.putInMap("*", tbx.getAttribute("type"), "variant");
	}
	
	static private Document parseTbx(InputStream inputStream) throws RutException {
		InputSource inputSource = new InputSource(inputStream);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputSource);
			return document;
		}
		catch(Exception e) {
			throw new RutException(e);
		}
	}

	private void parseConcepts() {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			Concept concept = conceptEntryParser.parseConceptEntry(conceptEntry, creator);
			concepts.add(concept);
			lexicons.putAll(conceptEntryParser.getLexicons());
		}		
	}

	@Override
	public ResourceInterface parse() throws Exception {
		parseConcepts();
		metadata.putInMap("*", conceptEntryParser.numberOfTerms, "numberOfTerms");
		metadata.putInMap("*", concepts.size(), "numberOfConcepts");
		return this;
	}

	@Override
	public Map<String, String> getLexicons() {
		return lexicons;
	}

	@Override
	public Collection<Concept> getConcepts() {
		return concepts;
	}

	@Override
	public Collection<Word> getWords() {
		return null;
	}

}
