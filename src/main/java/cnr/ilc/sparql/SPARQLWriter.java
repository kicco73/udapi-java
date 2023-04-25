/**
 * @author Enrico Carniani
 */

package cnr.ilc.sparql;

public class SPARQLWriter {
	static final public String separator = "# data-chunk";
	final protected String creator;
	final private int chunkSize;
	final private StringBuffer buffer = new StringBuffer();
	private int charsWritten = 0;
	private boolean blockStarted = false;
	private String prefixes =
		"""		
		PREFIX : <%s>
		PREFIX conc: <%s>
		PREFIX term: <%s>
		PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		PREFIX dct: <http://purl.org/dc/terms/>
		PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#>
		PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#>
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		PREFIX lime: <http://www.w3.org/ns/lemon/lime#>
		PREFIX vs: <http://www.w3.org/2003/06/sw-vocab-status/ns#>
		PREFIX vartrans: <http://www.w3.org/ns/lemon/vartrans#>
		""";

	private void appendLine(String block) {
		boolean isEndOfChunk = charsWritten > chunkSize;

		if (isEndOfChunk) {
			buffer.append(String.format("}\n%s\n%s", separator, prefixes));
			charsWritten = 0;
			blockStarted = false;
		}
		if (charsWritten == 0 || isEndOfChunk) {
			buffer.append("INSERT DATA {\n");	
			blockStarted = true;
		}

		buffer.append(block);
		charsWritten += block.length();
	}

	// Hi-level interface

	public SPARQLWriter(String namespace, String creator, int chunkSize) {
		this.creator = creator;
		this.chunkSize = chunkSize;
		prefixes = String.format(prefixes, namespace, namespace, namespace);
		buffer.append(prefixes);
	}

	public void append(String block) {
		for (String line: block.split("\n")) {
			appendLine(line+"\n");
		}
	}

	public String getSparql() throws Exception {
		if (blockStarted) {
			buffer.append("}\n");
		}
		return buffer.toString();
	}
}
