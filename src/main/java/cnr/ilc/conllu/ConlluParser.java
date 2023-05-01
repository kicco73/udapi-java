/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;

import cnr.ilc.conllu.core.*;
import cnr.ilc.conllu.core.io.DocumentReader;
import cnr.ilc.conllu.core.io.DocumentWriter;
import cnr.ilc.conllu.core.io.impl.CoNLLUReader;
import cnr.ilc.conllu.core.io.impl.CoNLLUWriter;
import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.Global;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.ResourceInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.ParserInterface;

public class ConlluParser implements ParserInterface, ResourceInterface {
    private Document document;
    private String language;
    private String namespace;
    private Collection<TermInterface> terms;
    Global global = new Global();

    public ConlluParser(InputStream inputStream, String language) throws Exception {
        this.language = language;
        document = parseCoNLL(inputStream);
    }

    private static Document parseCoNLL(InputStream inputStream) {
        InputStreamReader inputStreamReader;
        inputStreamReader = new InputStreamReader(inputStream);
        DocumentReader coNLLUReader = new CoNLLUReader(inputStreamReader);
        Document document = coNLLUReader.readDocument();
        return document;
    }

    @Override
    public ResourceInterface parse() throws Exception {
        String lexiconFQN = global.triples.addLexicon(language);
        global.metadata.putInMap("*", "conllu", "fileType");
        terms = ParserHelper.compileLexicon(document, namespace, language, lexiconFQN);
        return this;
    }

    public void writeConll(String fileName) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }

    @Override
    public Collection<String> getLanguages() {
        return Arrays.asList(new String[]{language});
    }

    @Override
    public Collection<GlobalInterface> getGlobals() {
        Collection<GlobalInterface> globals = new ArrayList<>();
        globals.add(global);
        return globals;
    }

    @Override
    public Collection<ConceptInterface> getConcepts() {
        return new ArrayList<>();
    }

    @Override
    public Collection<TermInterface> getTerms() {
        return terms;
    }
}


