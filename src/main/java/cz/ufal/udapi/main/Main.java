package cz.ufal.udapi.main;

import cz.ufal.udapi.core.*;
import cz.ufal.udapi.core.io.DocumentReader;
import cz.ufal.udapi.core.io.DocumentWriter;
import cz.ufal.udapi.core.io.UdapiIOException;
import cz.ufal.udapi.core.io.impl.CoNLLUReader;
import cz.ufal.udapi.core.io.impl.CoNLLUWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

/**
 * The purpose of this class is to test correct behavior of Node operations
 * and to provide standard benchmark scenario.
 *
 * @author Enrico Carniani
 */
public class Main {

    public static void main(String[] args) throws Exception {

        String inCoNLL = "";
        String outCoNLL = "";
        int startIndex = 0;
        while (startIndex < args.length) {
            switch (args[startIndex++]) {
                case "-i":
                    inCoNLL = args[startIndex];
                    break;
                case "-o":
                    outCoNLL = args[startIndex];
                    break;                
            }
        }

        Document document = parse(inCoNLL);

        List<Sentence> sentences = document.getSentences();
        for (Sentence sentence: sentences) {

            System.out.println(sentence.getText());
            
            for (Token token: sentence.getTokens()) {
                Optional<MultiwordToken> mwt = token.getMwt();
                if (mwt.isPresent() && mwt.get().getTokens().get(0) == token) {
                    System.out.print("\t" + mwt.get().getForm() + ": multi-word " + token.getUpos());
                    for (Token subToken: mwt.get().getTokens()) {
                        System.out.print(" " + subToken.getForm());
                    }
                    System.out.println();
                }
                System.out.println("\t" + token.getForm() + ": " + token.getLemma() + " " + token.getUpos());
            }
        }

        if (outCoNLL.length() > 0)
            writeDoc(outCoNLL, document);
    }

    private static Document parse(String inCoNLL) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(Paths.get(inCoNLL).toFile());
        } catch (FileNotFoundException e) {
            throw new UdapiIOException("Provided CoNLL file '"+inCoNLL+"' not found.");
        }

        DocumentReader coNLLUReader = new CoNLLUReader(fileReader);
        Document document = coNLLUReader.readDocument();
        return document;
    }

    private static void writeDoc(String fileName, Document document) {
        DocumentWriter coNLLUWriter = new CoNLLUWriter();
        coNLLUWriter.writeDocument(document, Paths.get(fileName));
    }
}
