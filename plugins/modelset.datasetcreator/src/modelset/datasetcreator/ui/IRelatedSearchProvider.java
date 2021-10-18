package modelset.datasetcreator.ui;

import org.eclipse.jdt.annotation.NonNull;

import modelset.datasetcreator.algorithms.AsyncSearcher.AsyncResult;

public interface IRelatedSearchProvider {

	@NonNull
	AsyncResult popRelated();

	int getRelatedSize();
	
}
