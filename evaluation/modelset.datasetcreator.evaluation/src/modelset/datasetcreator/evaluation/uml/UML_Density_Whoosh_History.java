package modelset.datasetcreator.evaluation.uml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.WhooshSearchService;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.evaluation.EvaluationUtils;
import modelset.datasetcreator.evaluation.LabellingAlgorithm;
import modelset.datasetcreator.evaluation.Streak;
import modelset.search.lucene.LuceneSearchService;

/**
 * 
 * * Random selection of the first model
 * * Selects the middle model in the current streak
 *
 */
public class UML_Density_Whoosh_History extends LabellingAlgorithm {

	public UML_Density_Whoosh_History(@NonNull DatasetCreatorController controller) {
		super(controller);
	}

	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> labelled) {
		return EvaluationUtils.pickByDensity(controller, available, labelled);
	}

	@Override
	protected ISearchService getSearchService() {
		return new WhooshSearchService("localhost", 5000, "uml");
	}

	@Override
	protected List<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		List<? extends SwModel> secondary = streak.getSecondary();
		return Collections.unmodifiableList(secondary);
	}
	
}
