/**
 * @author Enrico Carniani
 */

package cnr.ilc.conllu.main;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cnr.ilc.conllu.core.*;

public class Compiler {

    private static void compileFeatures(String featuresString, Form form) {
        
        final Map<String, String> mapping = Stream.of(new String[][] {
            {"1", "lexinfo:firstPerson"},
            {"2", "lexinfo:secondPerson"},
            {"3", "lexinfo:thirdPerson"},
            {"Definite", "lexinfo:definite"},
            {"Degree", "lexinfo:degree"},
            {"Fem", "lexinfo:feminine"},
            {"Gender", "lexinfo:gender"},
            {"Ger", "lexinfo:gerund"},
            {"Imp", "lexinfo:imperative"},
            {"Ind", "lexinfo:indicative"},
            {"Inf", "lexinfo:infinite"},
            {"Masc", "lexinfo:masculine"},
            {"Mood", "lexinfo:mood"},
            {"Neg",	"lexinfo:negative"},
            {"Number", "lexinfo:number"},
            {"Past", "lexinfo:past"},
            {"Person", "lexinfo:person"},
            {"Plur", "lexinfo:plural"},
            {"Sing", "lexinfo:singular"},
            {"Tense", "lexinfo:tense"},
            {"VerbForm", "lexinfo:verbForm"},
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
        
        final Pattern featuresPattern = Pattern.compile("(?<key>[^|=]+)=(?<value>[^|]+)");
        Matcher features = featuresPattern.matcher(featuresString);
        while (features.find()) {
            String key = mapping.getOrDefault(features.group("key"), "<http://nomapping>");
            String value = mapping.getOrDefault(features.group("value"), "<http://nomapping>");

            if (mapping.get(features.group("key")) == null) {
                System.err.println(String.format("Feature key %s not defined, using null", features.group("key")));
            }
            if (mapping.get(features.group("value")) == null) {
                System.err.println(String.format("Feature value %s not defined, using null", features.group("value")));
            }

            form.features.put(key, value);
        }    
    }

    static Collection<Word> compileLexicon(Document document) {

        final Map<String, String> parts = Stream.of(new String[][] {
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
                    
                String lemma = token.getLemma().toLowerCase();
                String writtenRep = token.getForm().toLowerCase();
                String partOfSpeech = parts.get(token.getUpos());
                String features = token.getFeats();
                Word word = lemmas.get(lemma);

                // la chiave di lemmas deve essere: lemma+upos e non lemma

                if (word == null) {
                    word = new Word(lemma, partOfSpeech);
                    lemmas.put(lemma, word);

                    if (lemma.equals(writtenRep))
                        compileFeatures(features, word.canonicalForm);

                } else if (!word.hasForm(writtenRep)) {
                    Form form = new Form(writtenRep);
                    word.addOtherForm(form);
                    compileFeatures(features, form);
                } else if (word.canonicalForm.features.isEmpty()) {
                    compileFeatures(features, word.canonicalForm);
                }     
            }
        }
        return lemmas.values();
    }
}
