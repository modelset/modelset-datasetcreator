package modelset.datasetcreator.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import lombok.NonNull;
import modelset.common.annotations.AnnotationsModel;
import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsModel.AnnotationValue;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.SwModel;

public class AnnotationFilter extends ViewerFilter {
	
	private @NonNull String key;
	private @NonNull String value;

	public AnnotationFilter(@NonNull String key, @NonNull String name) {
		this.key = key;
		this.value = name.toLowerCase();
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SwModel) {
			SwModel m = (SwModel) element;		
			if (m.getMetadata() == null || m.getMetadata().isEmpty())
				return false;
			
			Map<String, List<String>> map;
			try {
				map = AnnotationsValidator.INSTANCE.toMap(m.getMetadata());
				List<String> domain = map.get(key);
				if (domain == null)
					return false;
				for (String d : domain) {
					System.out.println(d);
					if (d.toLowerCase().contains(value))
						return true;
				}
			} catch (SyntaxError e) {
				return false;
			}
		} else if (element instanceof AnnotationsModel.Annotation) {
			AnnotationsModel.Annotation ann = (AnnotationsModel.Annotation) element;
			return key.equals(ann.getAnnotationName()) && ann.containsKey(value);
		} else if (element instanceof AnnotationValue) {
			AnnotationValue v = (AnnotationValue) element;
			return key.equals(v.getAnnotation().getAnnotationName()) && v.getValue().equals(value);
		}
		
		return false;
	}

}
