package modelset.datasetcreator.algorithms;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.ISearchService.Item;
import modelset.common.services.ISearchService.SearchResult;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.ui.ModelViewSupport;

public class AsyncSearcher {

	private DatasetCreatorController controller;
	private ExecutorService workStealingPool;
	private ModelViewSupport support;
	private Consumer<AsyncResult> updater;
	private Deque<AsyncResult> results = new ArrayDeque<>();
	
	public AsyncSearcher(ModelViewSupport support, Consumer<AsyncResult> updater) {
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
	
	@SuppressWarnings("serial")
	public static class AsyncResult extends HashMap<SwModel, Double> {
		private SwModel model;

		public AsyncResult(SwModel model) {
			this.model = model;
		}
		
		public SwModel getModel() {
			return model;
		}
	}
		
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
			
			final int MAX = 100;
			SearchResult result = service.search(r, MAX * 2);
			AsyncResult resultScores = new AsyncResult(swModel);
				
			int i = 0;
			for (Item item : result.getSortedItems()) {
				SwModel m = controller.getModel(item.getName());

				if (m.isTagged())
					continue;
											
				resultScores.put(m, item.getScore());
				
				// I don't really trust implementations of service.search(r, max) 
				if (++i > MAX)
					break;
				
			}
			
			synchronized (results) {
				results.add(resultScores);
			}
			updater.accept(resultScores);
		}
		
	}
	
	@Nullable
	public AsyncResult pop() {
		synchronized (results) {
			if (results.isEmpty())
				return null;
			return results.pop();
		}
	}
	
	public void stop() {
		workStealingPool.shutdownNow();
		workStealingPool = Executors.newWorkStealingPool(2);	
		synchronized (results) {
			results.clear();
		}
	}

	public int size() {
		synchronized (results) {
			return results.size();
		}
	}
}
