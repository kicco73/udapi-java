package cnr.ilc.services;

import java.io.InputStream;

import cnr.ilc.conllu.ConlluParser;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.sparql.SPARQLAssembler;
import cnr.ilc.tbx.TbxParser;

public class OfflineCompiler {
	public boolean isTbx = false;
	public boolean isConnlu = false;
	public String language = "en";
	ParserInterface parser;

    private ParserInterface makeParser(InputStream inputStream, String creator) throws Exception {
        if (isTbx) {
            return new TbxParser(inputStream);
        } else if (isConnlu) {
            return new ConlluParser(inputStream, creator, language);
        }
        return null;
    }
	
	public String compile(InputStream inputStream, String namespace, String creator, int chunkSize, Filter filter) throws Exception {
		parser = makeParser(inputStream, creator);
		SPARQLAssembler assembler = new SPARQLAssembler(namespace, creator, chunkSize, filter);
		ResourceInterface resource = parser.parse();
		assembler.serialise(resource);
		return assembler.getSparql();
	}

	public void exportConll(String outfileName) {
		((ConlluParser)parser).writeConll(outfileName);
	}
}
