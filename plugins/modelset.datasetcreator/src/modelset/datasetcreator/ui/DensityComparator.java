package modelset.datasetcreator.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.SwModel;

public class DensityComparator extends ViewerComparator {

	@NonNull
	private final Map<SwModel, Integer> models;
	@NonNull
	private final DensityViewerFilter filter;

	public DensityComparator(Map<SwModel, Integer> sortedDensities) {
		this.models = sortedDensities;
		this.filter = new DensityViewerFilter();
	}
	
	public Set<String> notfound = new HashSet<>();
	
	public int compare(Viewer viewer, Object e1, Object e2) {
		SwModel m1 = (SwModel) e1;
		SwModel m2 = (SwModel) e2;
		
		Integer idx1 = models.get(m1);
		Integer idx2 = models.get(m2);
		
		if (idx1 == null && idx2 == null) {
			return 0;
		}
		
		if (idx1 == null) {
			notfound.add(m1.getId());
			System.out.println(notfound.size());
			System.out.println("Not found: " + m1.getId());
			return 1;
		}
		if (idx2 == null) {
			notfound.add(m2.getId());
			System.out.println(notfound.size());
			System.out.println("Not found: " + m2.getId());
			return -1;
		}
		
		return Integer.compare(idx1, idx2);		
	}

	public ViewerFilter getFilter() {
		return filter;
	}
	
	private class DensityViewerFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			SwModel model = (SwModel) element;			
			return model.isTagged();
		}
		
	}
	
}
