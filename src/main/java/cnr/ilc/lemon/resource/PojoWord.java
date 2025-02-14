package cnr.ilc.lemon.resource;

import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.rut.utils.Metadata;

public class PojoWord implements TermInterface {

	public int rowid = -1; //FIXME:

	final public Collection<SenseInterface> senses = new ArrayList<>();
	final private String lemma;
	final private String language;
	final private String FQName;
	final private String serialised;
	final private String conceptFQN;

	public PojoWord(String lemma, String language, String FQName, String serialised, String conceptFQN) {
		this.lemma = lemma;
		this.language = language;
		this.FQName = FQName;
		this.serialised = serialised;
		this.conceptFQN = conceptFQN;
	}

	@Override
	public ConceptInterface getConcept() {
		System.err.println("Unimplemented method 'getConcept'");
		return null;
	}

	@Override
	public String getLemma() {
		return lemma;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public String getPartOfSpeech() {
		throw new UnsupportedOperationException("Unimplemented method 'getPartOfSpeech'");
	}

	@Override
	public Metadata getMetadata() {
		throw new UnsupportedOperationException("Unimplemented method 'getMetadata'");
	}

	@Override
	public String getSerialised() {
		return serialised;
	}

	@Override
	public String getFQName() {
		return FQName;
	}

	@Override
	public Collection<SenseInterface> getSenses() {
		return senses;
	}

	@Override
	public String getConceptFQN() {
		return conceptFQN;
	}

}
