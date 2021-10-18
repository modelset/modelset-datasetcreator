package modelset.datasetcreator.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService.Item;
import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * 
 * * Random selection of the first model
 * * No selection of models based on the current streak
 *
 */
public class RandomAll_Algorithm extends LabellingAlgorithm {

	public RandomAll_Algorithm(@NonNull DatasetCreatorController controller) {
		super(controller);
	}

	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> alreadyProcessed) {
		Collections.shuffle(available);
		for (SwModel m : available) {
			if (! alreadyProcessed.contains(m))
				return m;
		}
		return null;
	}

	@Override
	protected Collection<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		return null;
	}

	@Override
	protected List<Item> doSearch(SwModel first) {
		List<SwModel> allModels = controller.getModels();
		Collections.shuffle(allModels);
		List<Item> results = new ArrayList<>();
		
		final int max = 100;		
		for(int i = 0; i < max; i++) {
			SwModel m = allModels.get(i);			
			results.add(new Item(m.getId(), max - i));
		}
		
		return results;
	}
	
}
