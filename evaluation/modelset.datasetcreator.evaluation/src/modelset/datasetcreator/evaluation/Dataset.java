package modelset.datasetcreator.evaluation;

import java.util.ArrayList;
import java.util.List;

public class Dataset {

	private List<String> models;

	public Dataset(List<String> models) {
		this.models = new ArrayList<>(models);
	}

}
