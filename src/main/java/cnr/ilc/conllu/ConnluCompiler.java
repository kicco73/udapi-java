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
import cnr.ilc.rut.BaseCompiler;
import cnr.ilc.rut.SPARQLWriter;
import cnr.ilc.rut.Word;

public class ConnluCompiler extends BaseCompiler {
    Document document;
    String language;
    String creator;
    String namespace;

    public ConnluCompiler(InputStream inputStream, SPARQLWriter sparql, String language) throws Exception {
        super(inputStream, sparql);
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
    public String toSPARQL() throws Exception {
        Collection<Word> words = CompilerHelper.compileLexicon(document, namespace, language);
        String lexiconFQN = sparql.createLexicon(":connll-u", language);
     
        for (Word word: words) {
            sparql.addWord(word, lexiconFQN, "ontolex:Word");
        }

        return sparql.toString();
    }

    public void writeConll(String fileName) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }
}
