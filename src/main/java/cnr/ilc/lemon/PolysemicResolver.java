package cnr.ilc.lemon;

import java.util.ArrayList;
import java.util.Collection;

import cnr.ilc.lemon.resource.Sense;
import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.TermInterface;

public class PolysemicResolver {

	private TermInterface oneWordMultipleSensesToDifferentConcepts(Collection<TermInterface> input) {
		TermInterface ref = null;
		int n = 0;

		for(TermInterface word: input) {
			if (ref == null) {
				ref = word;
				ref.getSenses().clear();
			}
			String senseId = String.format("poly%s", ++n);
			SenseInterface sense = new Sense(ref, senseId, null);
			ref.getSenses().add(sense);
		}
		return ref;
	}

	public Collection<TermInterface> resolve(Collection<TermInterface> input) {
		if (input.size() < 2) return input;
		Collection<TermInterface> output = new ArrayList<>();
		output.add(oneWordMultipleSensesToDifferentConcepts(input));
		return output;
	}
}
