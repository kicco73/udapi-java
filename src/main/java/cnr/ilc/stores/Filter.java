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

	public Map<String, Collection<String>> get() {
		return map;
	}

}
