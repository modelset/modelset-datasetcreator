package modelset.datasetcreator.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.SwModel;

public class NameFilter extends ViewerFilter {
	
	private String name;

	public NameFilter(String name) {
		this.name = name;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SwModel) {
			SwModel m = (SwModel) element;			
			if (m.getName().toLowerCase().contains(name.toLowerCase()) || m.getId().toLowerCase().contains(name.toLowerCase()))
				return true;
		}
		return false;
	}

}
