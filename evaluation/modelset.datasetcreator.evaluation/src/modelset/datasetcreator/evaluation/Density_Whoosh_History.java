package modelset.datasetcreator.evaluation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.WhooshSearchService;
import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * 
 * * Random selection of the first model
 * * Selects the middle model in the current streak
 * 
 *
 */
public class Density_Whoosh_History extends LabellingAlgorithm {

	public Density_Whoosh_History(@NonNull DatasetCreatorController controller) {
		super(controller);
	}

	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> labelled) {
		return EvaluationUtils.pickByDensity(controller, available, labelled);
	}

	@Override
	protected ISearchService getSearchService() {
		return new WhooshSearchService("localhost", 5000, "ecore");
	}

	@Override
	protected List<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		List<? extends SwModel> secondary = streak.getSecondary();
		return Collections.unmodifiableList(secondary);
	}
	
}
