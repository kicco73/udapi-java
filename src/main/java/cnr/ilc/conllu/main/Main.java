/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

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

public class Main {
    String inCoNLL = "";
    String graphURL = null;
    String repository = "LexO";
    String language = "it";
    String creator = "bot";
    int chunkSize = 1250;
    String namespace = "http://txt2rdf/test#";
    String exportConll = null;

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    private void run(String[] args) throws Exception {
        int startIndex = 0; 
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-i":
                case "--input":
                    inCoNLL = args[startIndex];
                    break;
                case "-c":
                case "--creator":
                    creator = args[startIndex];     
                    break;   
                case "-o":
                case "--graphdb-url":
                    graphURL = args[startIndex];
                    break;
                case "-r":
                case "--repository":
                    repository = args[startIndex];
                    break;
                case "-s":
                case "--chunk-size":
                    chunkSize = Integer.parseInt(args[startIndex]);        
                    break;
                case "-l":
                case "--language":
                    language = args[startIndex];
                    break;
                case "-x":
                case "--export-conll":
                    exportConll = args[startIndex];
                    break;
                case "-n":
                case "--namespace":
                    namespace = args[startIndex];
                    break;
                default:
                    System.err.println(String.format("Unknown option: %s", args[startIndex-1]));
                    break;
            }
        }

        Document document = parseCoNLL(inCoNLL);
        Collection<Word> words = Compiler.compileLexicon(document, namespace);
        String statements = createSPARQL(words);

        if (exportConll != null) {
            writeConll(exportConll, document);
        }

        if (graphURL != null) {
            uploadStatements(graphURL, repository, statements);
        } else {
            System.out.println(statements);
        }
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

    private String createSPARQL(Collection<Word> words) throws Exception {
        SPARQLWriter sparql = new SPARQLWriter(namespace, language, creator);
        String lexiconFQN = sparql.createLexicon();
     
        int count = 0;
        for (Word word: words) {
            sparql.addWord(word, lexiconFQN);
            if (chunkSize > 0 && ++count % chunkSize == 0) 
                sparql.splitChunk("[data-chunk]");
        }

        return sparql.toString();
    }

    private static void uploadStatements(String graphURL, String repository, String statements) throws Exception {
        GraphDBClient client = new GraphDBClient(graphURL, repository);
        String[] chunks = statements.split("# \\[data-chunk\\]\n", 0);
        int n = 0;
        for (String chunk: chunks) {
            System.out.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
            client.post(chunk);
        }
        System.out.println();
    }
    
    private static void writeConll(String fileName, Document document) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }
}
