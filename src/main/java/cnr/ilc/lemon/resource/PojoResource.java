package cnr.ilc.lemon.resource;

import java.util.Collection;

public class PojoResource implements ResourceInterface {
	final private Collection<String> languages;
	final private Collection<GlobalInterface> globals;
	final private Collection<ConceptInterface> concepts;
	final private Collection<WordInterface> words;

	public PojoResource(Collection<String> languages, Collection<GlobalInterface> globals, Collection<ConceptInterface> concepts, Collection<WordInterface> words) {
		this.languages = languages;
		this.globals = globals;
		this.concepts = concepts;
		this.words = words;
	}

	@Override
	public Collection<String> getLanguages() {
		return languages;
	}

	@Override
	public Collection<GlobalInterface> getGlobals() {
		return globals;
	}

	@Override
	public Collection<ConceptInterface> getConcepts() {
		return concepts;
	}

	@Override
	public Collection<WordInterface> getWords() {
		return words;
	}
	
}
