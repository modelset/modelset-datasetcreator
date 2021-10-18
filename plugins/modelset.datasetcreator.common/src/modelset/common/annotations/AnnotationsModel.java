package modelset.common.annotations;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.NonNull;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.SwModel;

public class AnnotationsModel {

	private Map<String, Annotation> annotationsByKey = new HashMap<>(); 
	
	@SuppressWarnings("serial")
	public static class Annotation extends HashMap<String, AnnotationValue> {

		public static final Annotation[] EMPTY = new Annotation[0];
		
		private @NonNull String annotationName;

		public Annotation(@NonNull String annotationName) {
			this.annotationName = annotationName;
		}
		
		public String getAnnotationName() {
			return annotationName;
		}
		
		public void addValue(@NonNull String value, @NonNull SwModel m) {
			SortedSet<SwModel> models = this.computeIfAbsent(value, v -> new AnnotationValue(this, v));
			models.add(m);
		}

		public int totalElements() {
			int total = 0;
			for (String k : keySet()) {
				total += this.get(k).size();
			}
			return total;
		}		
	}
	
	@SuppressWarnings("serial")
	public static class AnnotationValue extends TreeSet<SwModel> {
		private @NonNull String value;
		private Annotation annotation;

		public AnnotationValue(Annotation annotation, @NonNull String value) {
			super(SwModel.ByNameComparator);
			this.annotation = annotation;
			this.value = value;
		}
		
		@NonNull
		public String getValue() {
			return value;
		}
		
		@NonNull
		public Annotation getAnnotation() {
			return annotation;
		}
	}

	public AnnotationsModel(@NonNull Collection<? extends SwModel> models) {
		for (SwModel m : models) {
			String metadata = m.getMetadata();
			if (metadata != null) {
				try {
					Map<String, List<String>> map = AnnotationsValidator.INSTANCE.toMap(metadata);
					
					map.forEach((label, valueList) -> {
						Annotation values = annotationsByKey.computeIfAbsent(label, Annotation::new);
						for (String value : valueList) {
							values.addValue(value, m);
						}
					});				
				} catch (SyntaxError e) {
					e.printStackTrace();
					// Report as part of the model?
				}
				
			}
		}
	}

	public Collection<? extends Annotation> getAnnotations() {
		return annotationsByKey.values();
	}
	
}
