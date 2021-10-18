package modelset.datasetcreator.extensions;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.TreeViewer;

import modelset.datasetcreator.visualizations.IVisualizer;

public interface IDatasetCreatorExtensionHandler {
	String getId();

	void setResource(@NonNull Resource r);
	
	@NonNull
	List<IVisualizer> getVisualizer();

	@NonNull
	IOutline getOutline();

	default void initialize() { }
	
	/**
	 * Customizes the visualization of the tree view. 
	 * TODO: In the future we might want to pass a datastructure with all the UI elements
	 * that can be customized.
	 */
	default void prepareUI(UpdatableUIElements ui) { }

}
