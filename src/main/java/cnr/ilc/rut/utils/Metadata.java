package cnr.ilc.rut.utils;

import java.util.Collection;
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

	public Metadata() {}

	public void merge(String language, Map<String, Object> other) {
		Map<String, Object> root = getMap(language);
		if(root == null) {
			root = new JSONObject();
			metadata.put(language, root);
		}
		deepMerge(root, other);
	}

	private void put_(Object value, String ...path) {
		Map<String, Object> root = metadata;
		for (int index = 0; index < path.length-1; index++) {
			String leg = path[index];
			if (root.containsKey(leg)) {
				root = (Map<String, Object>) root.get(leg);
			} else {
				Map<String, Object> legEntry = new JSONObject();
				root.put(leg, legEntry);
				root = legEntry;	
			}
		}
		root.put(path[path.length-1], value);
	}


	public void putInMap(String language, Object value, String ...path) {
		String[] newArray = new String[path.length + 1];
		newArray[0] = language;
 		System.arraycopy(path, 0, newArray, 1, path.length);
		put_(value, newArray);
	}


	public void addToList(String language, Object value, String ...path) {
		Collection<Object> collection = getCollection(language, path);
		if (collection == null) {
			collection = new JSONArray();
			putInMap(language, collection, path);
		}
		collection.add(value);
	}

	private Object get_(String ...path) {
		Map<String, Object> root = metadata;
		for (int index = 0; index < path.length-1; index++) {
			String leg = path[index];
			root = (Map<String, Object>) root.get(leg);
			if (root == null) return null;
		}
		return root.get(path[path.length-1]);
	}

	public Object getObject(String language, String ...path) {
		String[] newArray = new String[path.length + 1];
		newArray[0] = language;
 		System.arraycopy(path, 0, newArray, 1, path.length);
		return get_(newArray);
	}

	public Map<String,Object> getMap(String language, String ...path) {
		return (Map<String,Object>) getObject(language, path);
	}

	public Collection<Object> getCollection(String language, String ...path) {
		return (Collection<Object>) getObject(language, path);
	}

	public String toJson(String language, String...path) {
		JSONObject data = (JSONObject) getMap(language, path);
		return JSONObject.toJSONString(data);
	}
	
}
