package modelset.datasetcreator.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.jdt.annotation.NonNull;

import modelset.common.db.SwModel;

public class HistoryQueue {

	private Object lock = new Object();
	
	@NonNull
	private Set<SwModel> history = new HashSet<>(); // ConcurrentHashMap.newKeySet();
	@NonNull
	private Deque<SwModel> toCheck = new ConcurrentLinkedDeque<>();
	
	public void addModels(@NonNull SwModel... models) {
		addModels(Arrays.asList(models));
	}
	
	public void addModels(@NonNull List<? extends SwModel> models) {
		for (SwModel m : models) {
			synchronized (lock) {				
				if (! history.contains(m)) {
					history.add(m);
					toCheck.add(m);
				}
			}
		}
	}

	@NonNull
	public Collection<? extends SwModel> getPending() {
		return toCheck;
	}

	@NonNull
	public SwModel pop() {
		return toCheck.pop();
	}

	public boolean isEmpty() {
		return toCheck.isEmpty();
	}
}
