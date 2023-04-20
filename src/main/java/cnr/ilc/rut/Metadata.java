package cnr.ilc.rut;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class Metadata {
	private Map<String, Object> metadata = new JSONObject();

	private static Map<String, Object> deepMerge(Map<String, Object> original, Map<String, Object> newMap) {
        for (String key : newMap.keySet()) {
            if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
                Map<String, Object> originalChild = (Map<String, Object>) original.get(key);
                Map<String, Object> newChild = (Map<String, Object>) newMap.get(key);
                original.put(key, deepMerge(originalChild, newChild));
            } else {
                original.put(key, newMap.get(key));
            }
        }
        return original;
    }

	public void merge(Map<String, Object> other) {
		metadata = deepMerge(metadata, other);
	}

	public void put(Object value, String ...path) {
		Map<String, Object> root = metadata;
		for (int index = 0; index < path.length-1; index++) {
			String leg = path[index];
			if (root.containsKey(leg)) {
				root = (Map<String, Object>) root.get(leg);
			} else {
				Map<String, Object> legEntry = new LinkedHashMap<>();
				root.put(leg, legEntry);
				root = legEntry;	
			}
		}
		root.put(path[path.length-1], value);
	}

	public void add(Object value, String ...path) {
		Collection<Object> collection = (Collection<Object>) get(path);
		if (collection == null) {
			collection = new JSONArray();
			put(collection, path);
		}
		collection.add(value);
	}

	public Object get(String ...path) {
		Map<String, Object> root = metadata;
		for (int index = 0; index < path.length-1; index++) {
			String leg = path[index];
			root = (Map<String, Object>) root.get(leg);
			if (root == null) return null;
		}
		return root.get(path[path.length-1]);
	}

	public Map<String, Object> get() {
		return metadata;
	}

	public String serialise() {
		return JSONObject.toJSONString(metadata);
	}
	
}
