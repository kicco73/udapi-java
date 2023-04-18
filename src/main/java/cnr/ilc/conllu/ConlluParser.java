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
import cnr.ilc.rut.Concept;
import cnr.ilc.rut.ParserInterface;
import cnr.ilc.rut.ResourceInterface;
import cnr.ilc.rut.Word;

public class ConlluParser implements ParserInterface, ResourceInterface {
    private Document document;
    private String language;
    private String creator;
    private String namespace;
    private Collection<Word> words;

    public ConlluParser(InputStream inputStream, String creator, String language) throws Exception {
        this.creator = creator;
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
        String lexiconFQN = ":connll-u";
        words = ParserHelper.compileLexicon(document, namespace, language, lexiconFQN, creator);
        return this;
    }

    public void writeConll(String fileName) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }

    @Override
    public Map<String, Object> getMetadata() {
        return new HashMap<String, Object>();
   }

    @Override
    public Map<String, String> getLexicons() {
        Map<String, String> lexicons = new HashMap<>();
        lexicons.put(":connll-u", language);
        return lexicons;
    }

    @Override
    public Collection<Concept> getConcepts() {
        return null;
    }

    @Override
    public Collection<Word> getWords() {
        return words;
    }
}


