package modelset.datasetcreator.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;

public class SearchComposite extends MainComposite {

	public SearchComposite(Composite parent, int style, @NonNull DatasetCreatorController controller, @NonNull IProject project, @NonNull IStatusViewer statusViewer) {
		super(parent, style, controller, project, statusViewer);
	}
	
	@Override
	protected void initCmpRelated(SashForm sashForm_3) {
		// We don't want this here
	}

	@Override
	protected void nextByDensity() {
		SwModel selected = controller.getCurrentModel();		
		filterBySearch(selected, new ScoreComparator.NoFilter());
	}
	
}
