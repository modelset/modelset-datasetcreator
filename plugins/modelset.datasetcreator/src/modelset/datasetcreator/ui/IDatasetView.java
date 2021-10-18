package modelset.datasetcreator.ui;

import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * Provides functionality to handle a dataset.
 */
public interface IDatasetView {

	public void save(String txtMetadata);

	public void nextItem();

	public DatasetCreatorController getController();
	
}
