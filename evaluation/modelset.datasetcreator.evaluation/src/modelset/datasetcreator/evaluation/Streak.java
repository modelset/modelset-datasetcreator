package modelset.datasetcreator.evaluation;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import modelset.common.db.SwModel;

public class Streak {

	private SwModel first;
	private List<SwModel> followingModels = new ArrayList<>();
	private List<Integer> windowSize = new ArrayList<>();
	private String domain;
	
	public Streak(@NonNull SwModel model, String domain) {
		this.first = model;
		this.domain = domain;
	}
	
	public SwModel getFirst() {
		return first;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public void addModel(SwModel model, int windowSize) {
		this.followingModels.add(model);
		this.windowSize.add(windowSize);
	}
	
	public int getSize() {
		return followingModels.size() + 1;
	}

	public List<? extends SwModel> getSecondary() {
		return followingModels;
	}
}
