package cnr.ilc.stores.filterstore;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Filter {
	private Map<String, Collection<String>> map = new HashMap<>();
	private boolean noConcepts = false;

	private Collection<String> cloneCollection(Collection<String> original) {
		Collection<String> clone = new HashSet<>();
		for (String item: original) {
			clone.add(item);
		}
		return clone;
	}

	public Filter() {
		setLanguages(null);
		setDates(null);
		setSubjectFields(null);
	}
	
	public Filter(Filter other) {
		noConcepts = other.noConcepts;
		setLanguages(other.getLanguages());
		setDates(other.getDates());
		setSubjectFields(other.getSubjectFields());
	}

	public void setLanguages(Collection<String> languages) {
		if (languages == null) map.put("language", new HashSet<>());
		else map.put("language", cloneCollection(languages));
	}

	public Collection<String> getLanguages() {
		return map.get("language");
	}

	public void setDates(Collection<String> dates) {
		if (dates == null) map.put("date", new HashSet<>());
		else map.put("date", cloneCollection(dates));
	}


	public Collection<String> getDates() {
		return map.get("date");
	}

	public void setSubjectFields(Collection<String> subjectFields) {
		if (subjectFields == null) map.put("subjectField", new HashSet<>());
		else map.put("subjectField", cloneCollection(subjectFields));
	}

	public Collection<String> getSubjectFields() {
		return map.get("subjectField");
	}

	public boolean isNoConcepts() {
		return noConcepts;
	}

	public void setNoConcepts(boolean noConcepts) {
		this.noConcepts = noConcepts;
	}

	public Map<String, Collection<String>> getMap() {
		return map;
	}
}
