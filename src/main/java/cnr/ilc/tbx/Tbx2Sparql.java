package cnr.ilc.tbx;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import cnr.ilc.rut.CountingInputStream;
import cnr.ilc.rut.RutException;
import cnr.ilc.rut.SPARQLWriter;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Tbx2Sparql {
	public final String tbxType;
	public final long fileSize;

	private Document document;
	private SPARQLWriter sparql;
	private ConceptEntry conceptEntryParser;
	protected Set<String> concepts = new HashSet<>();
	
    public Tbx2Sparql(String fileName, SPARQLWriter sparql) throws Exception {
		this.sparql = sparql;
		conceptEntryParser = new ConceptEntry(sparql);

		InputStream inputStream = fileName != null? new FileInputStream(fileName) : System.in;
		CountingInputStream countingInputStream = new CountingInputStream(inputStream);

		document = parseTbx(countingInputStream);
		document.getDocumentElement().normalize();
		Element tbx = (Element) document.getElementsByTagName("tbx").item(0);

		fileSize = countingInputStream.getCount();
		tbxType = tbx.getAttribute("type");
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

	public String createSPARQL() throws Exception {
		NodeList conceptEntries = document.getElementsByTagName("conceptEntry");
		
		for (int i = 0; i < conceptEntries.getLength(); ++i)  {
			Element conceptEntry = (Element) conceptEntries.item(i);
			String conceptId = conceptEntryParser.parseConceptEntry(conceptEntry);
			concepts.add(conceptId);
		}

		return sparql.toString();
	}

	public int getNumberOfConcepts() {
		return concepts.size();
	}

	public int getNumberOfLanguages() {
		return conceptEntryParser.getNumberOfLanguages();
	}

	public int getNumberOfTerms() {
		return conceptEntryParser.numberOfTerms;
	}
}
