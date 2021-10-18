package modelset.common.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a service to search models in a repository 
 *
 */
public interface ISearchService {

	@Nullable
	default public SearchResult search(@NonNull Resource r) {
		return search(r, 100);
	}

	@Nullable
	SearchResult search(@NonNull Resource r, int max);
	
	public static class SearchResult {

		private List<Item> items = new ArrayList<>();
		
		public void add(@NonNull String name, double score) {
			items.add(new Item(name, score));
		}
		
		@NonNull
		public List<? extends Item> getItems() {
			return items;
		}
		
		@NonNull
		public List<Item> getSortedItems() {
			List<Item> list = new ArrayList<>(items);
			Collections.sort(list);
			return list;
		}
	}
	
	public static class Item implements Comparable<Item> {

		public static final Object[] EMPTY_ARRAY = new Object[0];
		
		@NonNull
		private String name;
		private double score;

		public Item(String name, double score) {
			this.name = name;
			this.score = score;
		}
		
		public String getName() {
			return name;
		}
		
		public double getScore() {
			return score;
		}

		@Override
		public int compareTo(Item o) {
			return -1 * Double.compare(score, o.score);
		}		
	}
}
