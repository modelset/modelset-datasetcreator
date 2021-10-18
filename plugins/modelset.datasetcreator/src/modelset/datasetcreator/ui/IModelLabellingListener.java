package modelset.datasetcreator.ui;

import org.eclipse.jdt.annotation.NonNull;

import modelset.common.db.SwModel;

public interface IModelLabellingListener {

	void labelled(@NonNull SwModel model);

	void next();

}
