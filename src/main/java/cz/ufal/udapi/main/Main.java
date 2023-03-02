/**
 * @author Enrico Carniani
 */

package cz.ufal.udapi.main;

import cz.ufal.udapi.core.*;
import cz.ufal.udapi.core.io.DocumentReader;
import cz.ufal.udapi.core.io.UdapiIOException;
import cz.ufal.udapi.core.io.impl.CoNLLUReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws Exception {

        String inCoNLL = "";
        String graphURL = null;
        String repository = "LexO";
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
                default:
                    break;
            }
        }

        Document document = parseCoNLL(inCoNLL);
        Collection<Word> words = compileLexicon(document);
        String statements = createSPARQL(words, chunkSize);

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

    private static Collection<Word> compileLexicon(Document document) {

        Map<String, String> parts = Stream.of(new String[][] {
                { "ADV", "lexinfo:adverb" },
                { "VERB", "lexinfo:verb" },
                { "ADJ", "lexinfo:adjective" },
                { "NOUN", "lexinfo:noun" },
                { "PROPN", "lexinfo:properNoun" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

        Map<String, Word> lemmas = new HashMap<>();        
        List<Sentence> sentences = document.getSentences();

        for (Sentence sentence : sentences) {

            for (Token token : sentence.getTokens()) {
                Optional<MultiwordToken> mwt = token.getMwt();
                if (mwt.isPresent() && mwt.get().getTokens().get(0) == token)
                    continue;

                if (!parts.containsKey(token.getUpos())) 
                    continue;
                    
                String lemma = token.getLemma();
                String partOfSpeech = parts.get(token.getUpos());
                Word word;

                if (!lemmas.containsKey(lemma)) {
                    word = new Word(lemma, partOfSpeech);
                    lemmas.put(lemma, word);
                } else {
                    word = lemmas.get(lemma);
                    String form = token.getForm();
                    if (!lemma.equals(form)) {
                        word.otherForms.add(form);
                    }                
                }
            }
        }
        return lemmas.values();
    }

    private static String createSPARQL(Collection<Word> words, int chunkSize) throws Exception {
        SPARQLWriter sparql = new SPARQLWriter();
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
