/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

import cnr.ilc.conllu.core.*;
import cnr.ilc.conllu.core.io.DocumentReader;
import cnr.ilc.conllu.core.io.DocumentWriter;
import cnr.ilc.conllu.core.io.UdapiIOException;
import cnr.ilc.conllu.core.io.impl.CoNLLUReader;
import cnr.ilc.conllu.core.io.impl.CoNLLUWriter;
import cnr.ilc.conllu.main.Compiler;
import cnr.ilc.conllu.main.SPARQLWriter;
import cnr.ilc.conllu.main.Word;

public class Connlu2Sparql {
    Document document;
    String language;
    String creator;
    int chunkSize;
    String namespace;
    private SPARQLWriter sparql;

    public Connlu2Sparql(String inCoNLL, SPARQLWriter sparql, int chunkSize) throws Exception {
        this.sparql = sparql;
        this.chunkSize = chunkSize;
        document = parseCoNLL(inCoNLL);
    }

    private static Document parseCoNLL(String inCoNLL) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(Paths.get(inCoNLL).toFile());
        } catch (FileNotFoundException e) {
            throw new UdapiIOException("Provided CoNLL file '" + inCoNLL + "' not found.");
        }

        DocumentReader coNLLUReader = new CoNLLUReader(fileReader);
        Document document = coNLLUReader.readDocument();
        return document;
    }

    public String createSPARQL() throws Exception {
        Collection<Word> words = Compiler.compileLexicon(document, namespace, language);
        String lexiconFQN = sparql.createLexicon(":connll-u", language);
     
        int count = 0;
        for (Word word: words) {
            sparql.addWord(word, lexiconFQN, "ontolex:Word");
            if (chunkSize > 0 && ++count % chunkSize == 0) 
                sparql.splitChunk("[data-chunk]");
        }

        return sparql.toString();
    }

    public void writeConll(String fileName) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }
}
