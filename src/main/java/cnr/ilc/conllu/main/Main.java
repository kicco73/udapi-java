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
import cnr.ilc.conllu.core.io.UdapiIOException;
import cnr.ilc.conllu.core.io.impl.CoNLLUReader;

public class Main {

    public static void main(String[] args) throws Exception {

        String inCoNLL = "";
        String graphURL = null;
        String repository = "LexO";
        String language = "it";
        int chunkSize = 5000;
        int startIndex = 0; 
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-i":
                    inCoNLL = args[startIndex];
                    break;
                case "-o":
                    graphURL = args[startIndex];
                    break;
                case "-r":
                    repository = args[startIndex];
                    break;
                case "-c":
                    chunkSize = Integer.parseInt(args[startIndex]);        
                    break;
                case "-l":
                    language = args[startIndex];
                default:
                    System.err.println(String.format("Unknown option: %s", args[startIndex-1]));
                    break;
            }
        }

        Document document = parseCoNLL(inCoNLL);
        Collection<Word> words = Compiler.compileLexicon(document);
        String statements = createSPARQL(words, chunkSize, language);

        if (graphURL != null) {
            GraphDBClient client = new GraphDBClient(graphURL, repository);
            String[] chunks = statements.split("# \\[data-chunk\\]\n", 0);
            int n = 0;
            for (String chunk: chunks) {
                System.out.print(String.format("\rPosting... %.0f%%", ++n * 100.0/chunks.length));
                client.post(chunk);
            }
            System.out.println();
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

    private static String createSPARQL(Collection<Word> words, int chunkSize, String language) throws Exception {
        SPARQLWriter sparql = new SPARQLWriter(language);
        String lexiconFQN = sparql.createLexicon();
     
        int count = 0;
        for (Word word: words) {
            sparql.addWord(word, lexiconFQN);
            if (chunkSize > 0 && ++count % chunkSize == 0) 
                sparql.splitChunk("[data-chunk]");
        }

        return sparql.toString();
    }
}
