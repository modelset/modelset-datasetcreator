package modelset.datasetcreator.algorithms;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;

import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.ISearchService.Item;
import modelset.common.services.ISearchService.SearchResult;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.ui.ModelViewSupport;

public class AsyncMergingSearcher {

	private HistoryQueue queue;
	private DatasetCreatorController controller;
	private ExecutorService workStealingPool;
	private ModelViewSupport support;
	private Consumer<Map<SwModel, Double>> updater;
	
	
	public AsyncMergingSearcher(ModelViewSupport support, Consumer<Map<SwModel, Double>> updater) {
		this.controller = support.getController();
		this.support = support;
		this.workStealingPool = Executors.newWorkStealingPool(2);	
		this.updater = updater;
	}
	
	public void addStreak(SwModel... models) {
		//queue.addModels(models);
		for (SwModel swModel : models) {
			workStealingPool.submit(new Job(swModel));			
		}
	}
	
	private Map<SwModel, Double> scores = new HashMap<SwModel, Double>();
	
	public class Job implements Runnable {

		private SwModel swModel;

		public Job(SwModel swModel) {
			this.swModel = swModel;
		}

		@Override
		public void run() {
			ISearchService service = controller.getSearchService();
			File modelFile = swModel.getFulllFile(controller);
			Resource r = support.loadModel(modelFile);
			
			int max = 50;
			SearchResult result = service.search(r, max);
			Map<SwModel, Double> resultScores = new HashMap<SwModel, Double>();
				
			double selfScore = Double.MIN_VALUE;
			
			int i = 0;
			for (Item item : result.getSortedItems()) {
				SwModel m = controller.getModel(item.getName());
				if (m.equals(swModel)) {
					selfScore = item.getScore();
				}

				if (m.isTagged())
					continue;
			
				// This is just to make sure that at least we pick a value
				if (selfScore == Double.MIN_VALUE) {
					selfScore = item.getScore();
				}
								
				resultScores.put(m, item.getScore());
				
				// I don't really trust implementations of service.search(r, max) 
				if (++i > 50)
					break;
				
			}
						
			synchronized (scores) {
				final double selfScore_ = selfScore;
				resultScores.forEach((m, score) -> {
					score = score / selfScore_;
					double d = scores.getOrDefault(m, Double.MIN_VALUE);
					
					if (score > d) 
						scores.put(m, score);
				});
			}
			
			updater.accept(new HashMap<>(scores));
		}
		
	}
	
	public void stop() {
		workStealingPool.shutdownNow();
		workStealingPool = Executors.newWorkStealingPool(2);	
		scores = new HashMap<SwModel, Double>();
	}
}
