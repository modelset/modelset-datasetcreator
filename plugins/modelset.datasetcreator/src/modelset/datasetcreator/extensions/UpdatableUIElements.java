package modelset.datasetcreator.extensions;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import lombok.NonNull;
import modelset.datasetcreator.controller.DatasetCreatorController;

public class UpdatableUIElements {

	@NonNull
	private TreeViewer modelTreeViewer;
	@Nullable
	private TableViewer tvRelated;
	@NonNull 
	private DatasetCreatorController controller;

	public UpdatableUIElements(@NonNull DatasetCreatorController controller, @NonNull TreeViewer modelTreeViewer, @Nullable TableViewer tvRelated) {
		this.controller = controller;
		this.modelTreeViewer = modelTreeViewer;
		this.tvRelated = tvRelated;
	}

	@NonNull
	public TreeViewer getModelTreeViewer() {
		return modelTreeViewer;
	}
	
	@Nullable
	public TableViewer getTableViewerRelated() {
		return tvRelated;
	}

	@NonNull
	public DatasetCreatorController getController() {
		return controller;
	}
	
}
