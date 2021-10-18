package modelset.datasetcreator.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import lombok.NonNull;
import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.SwModel;
import modelset.common.services.ISearchService;
import modelset.common.services.ISearchService.Item;
import modelset.common.services.ISearchService.SearchResult;
import modelset.datasetcreator.controller.DatasetCreatorController;

/**
 * <pre>
 *   loop "while there are models not labelled":
 *      m <- pick model from not labelled (subclass defines it)
 *      loop:
 *         models <- search(m)
 *         <user tries to label> => count how many are labelled in a streak
 *         m <- pick model from the information in models
 *         if m == null
 *           break
 * </pre>
 * 
 */
public abstract class LabellingAlgorithm {

	protected final @NonNull DatasetCreatorController controller;

	public LabellingAlgorithm(@NonNull DatasetCreatorController controller) {
		this.controller = controller;
	}

	protected String getModelId(SwModel m) {
		return m.getId().replace(m.getRepo() + "/", "");
	}

	public void evaluate() {
		List<SwModel> relevantModels = getRelevantModels();
		System.out.println("Total relevant models: " + relevantModels.size());
		
		Set<SwModel> labelled = new HashSet<>();
		
		// This could be more structured
		Map<SwModel, String> domains = new HashMap<SwModel, String>();
		for (SwModel m : relevantModels) {
			String domain = getDomain(m);
			if (domain == null)
				continue;
			domains.put(m, domain);
		}
		
		long initTime = System.currentTimeMillis();
		
		EvaluationResult results = new EvaluationResult();
		
		Streak streak = null;
		int i = 0;
		while (true) {
			SwModel selected = pick(relevantModels, labelled);
			// If no model is available, we are done
			if (selected == null)
				break; 

			String domain = domains.get(selected);
			if (streak != null && streak.getDomain().equals(domain)) {
				// This means that "Next" has continued the previous streak, so we reuse it
				streak.addModel(selected, 1);
			} else {
				streak = results.addStreak(selected, domain);
			}

			Deque<SwModel> notVisited = new ArrayDeque<>();
			notVisited.add(selected);
			
			int pass = 0;
			while (! notVisited.isEmpty()) {
				SwModel first = notVisited.pop();
				
				i++;
				pass++;
				System.out.println("Processing " + i + ": " + first.getId());
					
				domain = domains.get(first);
				labelled.add(first);
				
				List<Item> items = doSearch(first);				
				
				// TODO: Check how many times we annotate something that was annotated in a previous streak
				final int WindowSize = 3;
				int currentWindow = 0;

				int annotated = 0;
				for (Item item : items) {
					SwModel m = controller.getModel(item.getName());
					if (m == null)
						throw new IllegalStateException();
					// We have virtually labelled this model. The user would not see it.
					if (labelled.contains(m))
						continue;
			
					String searchDomain = domains.get(m);
					// If the model has not been annotated yet we consider that it is "hole"
					// We can lift this restriction and just ignore the model for the count
					if (searchDomain != null && searchDomain.equals(domain)) {
						streak.addModel(m, currentWindow);
						annotated++;
						currentWindow = 0;
						// Simulate the labelling of the model
						labelled.add(m);
					} else {
						currentWindow++;
					}
					
					if (currentWindow > WindowSize) {
						break;
					}
				}

				System.out.println("Pass " + pass + " got " + items.size() + ", annotated " + annotated + ". Total labelled: " + labelled.size());
				
				debugCheckpoint(initTime, results, i);

				// If we fail to annotate here, we abort the current streak
				// TODO: Does it make sense to continue with this model? Maybe since it is probably similar in the continuum of domains...
				// TODO: Maybe this is a property of each concrete algorithm
				//if (annotated == 0)
				//	break;
				// This is to allow history

				// Try to pick a good model to contribute to the current streak
				Collection<SwModel> related = pickRelated(streak, labelled);
				if (related != null) {
					for (SwModel swModel : related) {
						if (labelled.contains(swModel))
							continue;
						notVisited.add(swModel);
					}
				}
			}				
			
		}

		System.out.println("Finished");
		debugCheckpoint(initTime, results, i);
		results.saveTo("/tmp");

	}

	protected List<Item> doSearch(SwModel first) {
		ISearchService service = getSearchService();
		Resource r = loadModel(first.getFulllFile(controller));				
		SearchResult result = service.search(r, 500);
		// e.g., because of a timeout
		if (result == null) {
			return Collections.emptyList();
		}
		List<Item> items = result.getSortedItems();
		return items;
	}

	protected ISearchService getSearchService() {
		return controller.getSearchService();
	}

	protected abstract Collection<SwModel> pickRelated(Streak streak, Set<SwModel> labelled);

	private void debugCheckpoint(long initTime, EvaluationResult results, int i) {
		double elapsed = getElapsedMinutes(initTime);
		
		System.out.println(String.format("Elapsed: %.2f", elapsed));
		System.out.println(results.toString());
		
		if (i % 10 == 0) {
			System.out.println("Saving...");
			results.saveTo("/tmp");
		}
	}
	
	protected List<SwModel> getRelevantModels() {
		Collection<? extends SwModel> models = controller.getAllModels();
		System.out.println("Total models: " + models.size());
		//List<SwModel> tagged = models.stream().filter(SwModel::isTagged).collect(Collectors.toList());
		List<SwModel> tagged = models.stream().filter(m -> getDomain(m) != null).collect(Collectors.toList());
		System.out.println("Tagged: " + models.size());
		Collections.shuffle(tagged);
		return tagged;
	}
	
	// TODO: Maybe pass the model type??
	protected Resource loadModel(File f) {
		URI uri = URI.createFileURI(f.getAbsolutePath());
		try {
			ResourceSet rs = new ResourceSetImpl();
			Resource r = rs.getResource(uri, true);
			return r;
		} catch (Exception e) {
			UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
			Resource r = factory.createResource(uri);
			try {
				r.load(null);
			} catch (IOException e1) {
				throw new RuntimeException(e);
			}
			return r;
		}
	}
	
	protected String getDomain(SwModel m) {
		if (! m.isTagged())
			return null;
		try {
			Map<String, List<String>> d = AnnotationsValidator.INSTANCE.toMap(m.getMetadata());
			if (d.containsKey("category"))
				return d.get("category").get(0);
				
			if (d.containsKey("domain"))
				return d.get("domain").get(0);
			
			return null;
		} catch (SyntaxError e) {
			return null;
		}		
	}

	protected double getElapsedMinutes(long initTime) {
		long finalTime = System.currentTimeMillis();
		return ((finalTime - initTime) / (1_000 * 60.0));		
	}

	@NonNull
	protected abstract SwModel pick(List<SwModel> available, Set<SwModel> alreadyProcessed);

}
