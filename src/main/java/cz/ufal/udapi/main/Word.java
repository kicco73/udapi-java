/**
 * @author Enrico Carniani
 */

package cz.ufal.udapi.main;

import java.util.HashSet;
import java.util.Set;

public class Word {
	final String FQName;
	final String canonicalForm;
	final String partOfSpeech;
	final Set<String> otherForms;

	public Word(String lemma, String partOfSpeech) {
		
		String FQName = lemma.replaceAll("[\\.']", "-");

		if (lemma != FQName) {
			System.err.println(String.format("Warning: found lemma %s, using FQName %s", lemma, FQName));
		}

		this.FQName = String.format(":le_%s", FQName);
		this.canonicalForm = lemma;
		this.partOfSpeech = partOfSpeech;
		this.otherForms = new HashSet<>();
	}
}
