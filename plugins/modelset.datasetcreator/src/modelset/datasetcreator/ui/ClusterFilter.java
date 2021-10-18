package modelset.datasetcreator.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.Cluster;
import modelset.common.db.SwModel;

public class ClusterFilter extends ViewerFilter {

	private Set<Integer> clusters = new HashSet<>();
	
	public ClusterFilter(Integer... ids) {
		Collections.addAll(clusters, ids);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SwModel) {
			SwModel m = (SwModel) element;
			for(Cluster c : m.getClusters()) {
				if (clusters.contains(c.getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
