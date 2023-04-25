package cnr.ilc.tbx;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.Logger;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class TbxParser implements ParserInterface, ResourceInterface {
	private Document document;
	private ConceptEntry conceptEntryParser = new ConceptEntry();
	private Map<String, Object> summary = new LinkedHashMap<>();
	private Map<String, String> lexicons = new HashMap<>();
	private Collection<Concept> concepts = new ArrayList<>();
	private String creator;

    public TbxParser(InputStream inputStream, String creator) throws Exception {
		this.creator = creator;
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		summary.put("fileSize", countingInputStream.getCount());
		summary.put("fileType", "tbx");
		summary.put("variant", tbx.getAttribute("type"));
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

	@Override
	public ResourceInterface parse() throws Exception {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");

		int prevPercentage = 0;
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			int newPrecentage = 100 * (i+1) / conceptEntries.getLength();
			if (newPrecentage > prevPercentage) {
				Logger.progress(newPrecentage, "Progress");
				prevPercentage = newPrecentage;
			}

			Element conceptEntry = (Element) conceptEntries.item(i);
			Concept concept = conceptEntryParser.parseConceptEntry(conceptEntry, creator);
			concepts.add(concept);
			lexicons.putAll(conceptEntryParser.getLexicons());
		}

		Logger.progress(100, "Done");
		return this;
	}

	@Override
	public Map<String, Object> getSummary() {
		return summary;
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
