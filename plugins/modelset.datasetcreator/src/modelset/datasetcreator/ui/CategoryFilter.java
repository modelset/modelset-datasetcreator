package modelset.datasetcreator.ui;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;

public class CategoryFilter extends ViewerFilter {
	
	private String categoryName;
	private @NonNull DatasetCreatorController controller;

	public CategoryFilter(String name, @NonNull DatasetCreatorController controller) {
		this.categoryName = name;
		this.controller = controller;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SwModel) {
			SwModel m = (SwModel) element;	
			Set<String> categories = controller.getCategories(m);
			return categories.contains(categoryName);
		}
		return false;
	}

}
