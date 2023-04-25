package cnr.ilc.stores;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Filter {
	private Map<String, Collection<String>> map = new HashMap<>();

	public Filter() {
		setLanguages(new HashSet<>());
		setDates(new HashSet<>());
	}

	public Filter(Filter other) {
		map = new HashMap<>(other.map);
	}

	public void setLanguages(Collection<String> languages) {
		if (languages == null) map.put("language", new HashSet<>());
		else map.put("language", new HashSet<>(languages));
	}

	public Collection<String> getLanguages() {
		return map.get("language");
	}

	public void setDates(Collection<String> dates) {
		if (dates == null) map.put("date", new HashSet<>());
		else map.put("date", new HashSet<>(dates));
	}


	public Collection<String> getDates() {
		return map.get("date");
	}

	public void setSubjectFields(Collection<String> subjectFields) {
		if (subjectFields == null) map.put("subjectField", new HashSet<>());
		else map.put("subjectField", new HashSet<>(subjectFields));
	}


	public Collection<String> getSubjectFields() {
		return map.get("subjectField");
	}

	public Map<String, Collection<String>> get() {
		return map;
	}

}
