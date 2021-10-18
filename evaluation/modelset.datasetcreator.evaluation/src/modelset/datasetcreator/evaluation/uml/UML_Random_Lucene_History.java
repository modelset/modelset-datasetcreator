package modelset.datasetcreator.evaluation.uml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.evaluation.LabellingAlgorithm;
import modelset.datasetcreator.evaluation.Streak;
import modelset.search.lucene.LuceneSearchService;

/**
 * 
 * * Random selection of the first model
 * * Selects the middle model in the current streak
 *
 */
public class UML_Random_Lucene_History extends LabellingAlgorithm {

	private String luceneIndex;

	public UML_Random_Lucene_History(@NonNull DatasetCreatorController controller, String luceneIndex) {
		super(controller);
		this.luceneIndex = luceneIndex;
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
		return new LuceneSearchService(luceneIndex);
	}

	@Override
	protected List<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		List<? extends SwModel> secondary = streak.getSecondary();
		return Collections.unmodifiableList(secondary);
	}
	
}
