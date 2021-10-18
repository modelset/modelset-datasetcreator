package modelset.datasetcreator.extensions;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;

import modelset.common.db.SwModel;

/**
 * Provides extensions for an specific type of dataset.
 */
public interface IDatasetTypeHandler {
	void setResource(@NonNull SwModel model, @NonNull Resource r);
	
	public boolean isApplicable();

	default void prepareUI(UpdatableUIElements ui) { }
}
