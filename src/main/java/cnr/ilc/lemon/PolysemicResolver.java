package cnr.ilc.lemon;

import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.Sense;
import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.WordInterface;

public class PolysemicResolver {

	private WordInterface oneWordMultipleSensesToDifferentConcepts(Collection<WordInterface> input) {
		WordInterface ref = null;
		int n = 0;

		for(WordInterface word: input) {
			if (ref == null)
				ref = new PojoWord(word.getLemma(), word.getLanguage(), word.getFQName(), word.getSerialised());
			String senseId = String.format("poly%s", ++n);
			SenseInterface sense = new Sense(ref, senseId, null);
			ref.getSenses().add(sense);
		}
		return ref;
	}

	public Collection<WordInterface> resolve(Collection<WordInterface> input) {
		Collection<WordInterface> output = new ArrayList<>();
		output.add(oneWordMultipleSensesToDifferentConcepts(input));
		return output;
	}
}
