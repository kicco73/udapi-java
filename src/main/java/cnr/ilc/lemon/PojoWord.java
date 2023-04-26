package cnr.ilc.lemon;

import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Metadata;

public class PojoWord implements WordInterface {

	public int rowid = -1; //FIXME:

	final public Collection<SenseInterface> senses = new ArrayList<>();
	final private String lemma;
	final private String language;
	final private String FQName;
	final private String serialised;

	public PojoWord(String lemma, String language, String FQName, String serialised) {
		this.lemma = lemma;
		this.language = language;
		this.FQName = FQName;
		this.serialised = serialised;
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
	public String getCreator() {
		System.err.println("Unimplemented method 'getCreator'");
		return "Zio Pino";
	}

	@Override
	public Collection<SenseInterface> getSenses() {
		return senses;
	}

}
