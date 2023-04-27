package cnr.ilc.stores.filterstore;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Filter {
	private Map<String, Collection<String>> map = new HashMap<>();
	private boolean noConcepts = false;
	private boolean noSenses = false;
	private boolean noPolysemicGroups = false;
	private boolean synonyms = false;
	private Collection<String> excludeIds = new HashSet<>();

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
		setLanguages(other.getLanguages());
		setDates(other.getDates());
		setSubjectFields(other.getSubjectFields());

		excludeIds = cloneCollection(other.excludeIds);
		noConcepts = other.noConcepts;
		noSenses = other.noSenses;
		noPolysemicGroups = other.noPolysemicGroups;
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

	public boolean isNoSenses() {
		return noSenses;
	}

	public void setNoSenses(boolean noSenses) {
		this.noSenses = noSenses;
	}

	public boolean isNoPolysemicGroups() {
		return noPolysemicGroups;
	}

	public void setNoPolysemicGroups(boolean noPolysemicGroups) {
		this.noPolysemicGroups = noPolysemicGroups;
	}

	public Collection<String> getExcludeIds() {
		return excludeIds;
	}

	public void setExcludeIds(Collection<String> ids) {
		excludeIds.clear();
		excludeIds.addAll(ids);
	}
	
	public boolean isSynonyms() {
		return synonyms;
	}

	public void setSynonyms(boolean synonyms) {
		this.synonyms = synonyms;
	}

    private String whereValueInList(String entityName, String columnName, Collection<String> values) {
        String where = "";
        String op = "and";
        if (values == null) return where;

        if (values.contains(null)) {
            where += String.format(" %s %s.%s IS null", op, entityName, columnName);
            values.remove(null);
            op = "or";
        }

        if (values.size() > 0) {
            String listString = values.stream().collect(Collectors.joining("', '"));
            where += String.format(" %s %s.%s in ('%s')", op, entityName, columnName, listString);  
        }
        return where;
    }

    public String buildWhere(String entityName) {
        String where = "true";
        for (Entry<String, Collection<String>> clause: getMap().entrySet()) {
            where += whereValueInList(entityName, clause.getKey(), clause.getValue());          
        }
        
        String excludeClause = whereValueInList(entityName, "rowid", getExcludeIds());
        if (excludeClause.length() > 0) {
            where += String.format(" and not (true %s)", excludeClause);  
		}

		if (isNoPolysemicGroups()) {
			where += " and polysemicGroup is null";
		}
		return where;
    }
}
