package modelset.datasetcreator.evaluation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService.Item;
import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * 
 * * Random selection of the first model
 * * Use KMeans for similarity
 *
 */
public class Random_Clustering_None extends LabellingAlgorithm {

	private Map<SwModel, Integer> clusters = new HashMap<>();
	private Map<Integer, Set<SwModel>> models = new HashMap<>();
	
	public Random_Clustering_None(@NonNull DatasetCreatorController controller, String repo, File clustersFile) {
		super(controller);
		
		try {
			for (CSVRecord record : CSVFormat.DEFAULT.parse(new FileReader(clustersFile))) {
				 String fileName = record.get(0);
				 int cluster = Integer.parseInt(record.get(1));
				 
				 // fileName is in the format repo-ecore-all/data, otherwise we should use SwModel.modelId
				 fileName = fileName.replace("repo-ecore-all/", "");				 
				 SwModel m = controller.getModel(fileName);
				 if (m == null)
					 throw new IllegalStateException("Model: " + fileName + " not found");
				 clusters.put(m, cluster);
				 Set<SwModel> set = models.computeIfAbsent(cluster, (k) -> new HashSet<>());
				 set.add(m);
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}	
	}

	@Override
	protected List<Item> doSearch(SwModel first) {
		if (! clusters.containsKey(first))
			return Collections.emptyList();
		System.out.println("searching: " + first.getName());
		int cluster = clusters.get(first);
		Set<SwModel> modelsInCluster = models.get(cluster);
		List<Item> items = new ArrayList<Item>();
		modelsInCluster.forEach(m -> {
			items.add(new Item(getModelId(m), 1.0));			
		});
		
		return items;
	}
	
	@Override
	protected SwModel pick(List<SwModel> available, Set<SwModel> alreadyProcessed) {
		for (SwModel m : available) {
			if (! alreadyProcessed.contains(m))
				return m;
		}
		return null;
	}

	@Override
	protected Collection<SwModel> pickRelated(Streak streak, Set<SwModel> alreadyProcessed) {
		return null;
	}
	
}
