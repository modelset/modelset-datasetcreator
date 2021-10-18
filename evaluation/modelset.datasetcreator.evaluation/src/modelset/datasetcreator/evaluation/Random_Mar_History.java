package modelset.datasetcreator.evaluation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.MarSearchService;
import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * 
 * * Random selection of the first model
 * * Selects the middle model in the current streak
 * 
 *
 */
public class Random_Mar_History extends LabellingAlgorithm {

	public Random_Mar_History(@NonNull DatasetCreatorController controller) {
		super(controller);
	}

	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> labelled) {
		for (SwModel m : available) {
			if (! labelled.contains(m))
				return m;
		}
		return null;
	}

	@Override
	protected ISearchService getSearchService() {
		return new MarSearchService("localhost", 8080, "ecore");
	}

	@Override
	protected List<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		List<? extends SwModel> secondary = streak.getSecondary();
		return Collections.unmodifiableList(secondary);
	}
	
}
