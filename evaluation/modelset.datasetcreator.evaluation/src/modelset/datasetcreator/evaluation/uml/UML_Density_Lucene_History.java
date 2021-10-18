package modelset.datasetcreator.evaluation.uml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.resource.Resource;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.WhooshSearchService;
import modelset.common.services.ISearchService.Item;
import modelset.common.services.ISearchService.SearchResult;
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
public class UML_Density_Lucene_History extends LabellingAlgorithm {

	private String luceneIndexFolder;

	public UML_Density_Lucene_History(@NonNull DatasetCreatorController controller, String luceneIndexFolder) {
		super(controller);
		this.luceneIndexFolder = luceneIndexFolder;
	}

	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> labelled) {
		return EvaluationUtils.pickByDensity(controller, available, labelled);
	}

	@Override
	protected ISearchService getSearchService() {
		return new LuceneSearchService(luceneIndexFolder);
	}
	
	@Override
	protected List<Item> doSearch(SwModel first) {
		String name = first.getFile().getName();
		
		
		int i = 0;
		List<Item> list = super.doSearch(first);
		for (Item item : list) {
			if (item.getName().contains(name)) {
				return list;
			}
			i++;
			if (i > 50)
				break;
		}
	
		ISearchService service = new WhooshSearchService("localhost", 5000, "uml");
		Resource r = loadModel(first.getFulllFile(controller));				
		SearchResult result = service.search(r, 500);
		// e.g., because of a timeout
		if (result == null) {
			return Collections.emptyList();
		}
		List<Item> items = result.getSortedItems();
		return items;

	}

	@Override
	protected List<SwModel> pickRelated(Streak streak, Set<SwModel> labelled) {
		List<? extends SwModel> secondary = streak.getSecondary();
		return Collections.unmodifiableList(secondary);
	}
	
}
