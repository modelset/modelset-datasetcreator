package modelset.datasetcreator.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.SwModel;

public class ScoreComparator extends ViewerComparator {

	@NonNull
	private final Map<SwModel, Double> models;
	private final Map<String, Double> modelsById;

	public ScoreComparator(Map<SwModel, Double> scores) {
		this.models = scores;
		this.modelsById = new HashMap<String, Double>();
		scores.forEach((k, v) -> {
			modelsById.put(k.getId(), v);
		});
	}
	
	public Set<String> notfound = new HashSet<>();
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		SwModel m1 = (SwModel) e1;
		SwModel m2 = (SwModel) e2;
		
		Double v1 = models.get(m1);
		Double v2 = models.get(m2);
		
		if (v1 == null && v2 == null) {
			return 0;
		}
		
		if (v1 == null) {
			notfound.add(m1.getId());
			System.out.println(notfound.size());
			System.out.println("Not found: " + m1.getId());
			System.out.println("   ById: " + modelsById.get(m1.getId()));
			return 1;
		}
		if (v2 == null) {
			notfound.add(m2.getId());
			System.out.println(notfound.size());
			System.out.println("Not found: " + m2.getId());
			return -1;
		}
		
		return -1 * Double.compare(v1, v2);		
	}
	
	public static class ScoreViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			SwModel model = (SwModel) element;			
			return ! model.isTagged();
		}
		
	}


	public static class NoFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return true;
		}

	}


}
