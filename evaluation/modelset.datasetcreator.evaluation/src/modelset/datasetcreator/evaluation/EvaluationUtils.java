package modelset.datasetcreator.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;

public class EvaluationUtils {

	public static SwModel pickByDensity(DatasetCreatorController controller, List<SwModel> available, Set<SwModel> alreadyProcessed) {
		try {
			List<? extends SwModel> sortedDensities = controller.getDensityList();
			SwModel selected = null;
			for (SwModel swModel : sortedDensities) {
				if (available.contains(swModel) && !alreadyProcessed.contains(swModel)) {
					selected = swModel;
					break;
				}
			}
			
			// In case there are more to process not available in density list
			for (SwModel m : available) {
				if (! alreadyProcessed.contains(m))
					return m;
			}
			
			return selected;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
