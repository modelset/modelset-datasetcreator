package modelset.common.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class AnnotationsValidator {
	public static final AnnotationsValidator INSTANCE = new AnnotationsValidator();
	
	public Map<String, List<String>> toMap(String metadata) throws SyntaxError {
		// Naive approach
		
		Map<String, List<String>> result = new HashMap<>();
		String[] parts = metadata.split(",");
		for (String part : parts) {
			int idx = part.indexOf(':');
			if (idx == -1) {
				throw new SyntaxError("Expected format: 'keyword: value, keyword: value'");
			} 
			
			String keyword = part.substring(0, idx).trim().toLowerCase();
			String value = part.substring(idx + 1).trim().toLowerCase();
			
			List<String> l = result.computeIfAbsent(keyword, (k) -> new ArrayList<String>());
			l.add(value);
		}		
		
		return result;
	}
	
	@NonNull
	public ParsedMetadata toMetadata(@NonNull String metadata) throws SyntaxError {
		return new ParsedMetadata(toMap(metadata));
	}
	
	public static class ParsedMetadata {
		private final Map<String, List<String>> data;
		
		public ParsedMetadata(Map<String, List<String>> data) {
			this.data = data;
		}
		
		public Map<String, List<String>> getData() {
			return data;
		}
		
		public boolean hasValue(String key, String value) {
			List<? extends String> v = getValues(key);
			return v != null && v.contains(value);
		}
		
		public boolean hasKey(String key) {
			return data.containsKey(key);
		}
		
		@Nullable
		public List<? extends String> getValues(@NonNull String key) {
			List<String> values = data.get(key);
			return values;
		}
		
		public void remove(@NonNull String key, @NonNull String value) {
			List<String> values = data.get(key);
			values.remove(value);
		}
		
		public void remove(@NonNull String key) {
			data.remove(key);
		}
		
		public void set(@NonNull String key, @NonNull String value) {
			List<String> values = data.get(key);
			if (values == null) {
				values = new ArrayList<String>();
				data.put(key, values);
			}
			values.add(value);
		}

		@NonNull
		public String serialize() {
			List<String> keys = new ArrayList<>(data.keySet());
			Collections.sort(keys);
			
			StringBuffer result = new StringBuffer();
			String separator = "";
			for (String key : keys) {
				List<String> values = data.get(key);
				for (String value : values) {
					result.append(separator);
					result.append(key).append(": ").append(toValue(value));
					separator = ", ";
				}
				
			}
			return result.toString();
		}

		private String toValue(String value) {
			value = value.replaceAll("^\"+", "").replaceAll("\"+$", "");			
			if (value.contains(" ")) {
				return '"' + value + '"';
			}
			return value;
		}
	}
	
	public static class SyntaxError extends Exception {

		public SyntaxError(String message) {
			super(message);
		}
		
	}
	
}
