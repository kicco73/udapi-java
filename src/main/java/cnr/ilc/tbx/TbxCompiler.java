package cnr.ilc.tbx;
import org.json.simple.JSONArray;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.BaseCompiler;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.SPARQLWriter;

import javax.xml.parsers.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class TbxCompiler extends BaseCompiler {
	private Document document;
	private ConceptEntry conceptEntryParser;
	private Set<String> concepts = new HashSet<>();
	
    public TbxCompiler(InputStream inputStream, SPARQLWriter sparql) throws Exception {
		super(inputStream, sparql);
		conceptEntryParser = new ConceptEntry(sparql);

		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		metadata.put("fileSize", countingInputStream.getCount());
		metadata.put("fileType", "tbx");
		metadata.put("variant", tbx.getAttribute("type"));
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
	public String toSPARQL() throws Exception {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			String conceptId = conceptEntryParser.parseConceptEntry(conceptEntry);
			concepts.add(conceptId);
		}

		JSONArray array = new JSONArray();
		Set<String> languages = conceptEntryParser.getLanguages();
		array.addAll(languages);
		metadata.put("languages", array);
		metadata.put("numberOfConcepts", concepts.size());
		metadata.put("numberOfTerms", conceptEntryParser.numberOfTerms);

		return sparql.toString();
	}
}
