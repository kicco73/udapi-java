package cnr.ilc.rut;

import java.util.ArrayList;
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
			Object originalChild = original.get(key);
			Object newChild = newMap.get(key);
            if (newChild instanceof Map && originalChild instanceof Map) {
                original.put(key, deepMerge((Map<String, Object>) originalChild, (Map<String, Object>) newChild));
			} else if (newChild instanceof Collection && originalChild instanceof Collection) {
				Collection<Object> mergedCollection = (Collection<Object>)originalChild;
				mergedCollection.addAll((Collection<Object>) newChild);
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
