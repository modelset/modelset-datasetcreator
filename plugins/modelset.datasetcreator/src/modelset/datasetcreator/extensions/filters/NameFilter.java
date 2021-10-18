package modelset.datasetcreator.extensions.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import modelset.common.db.SwModel;

public class NameFilter implements IFilter {

	private Set<String> valids;

	public NameFilter(String repoId, Collection<? extends String> models) {
		this.valids = new HashSet<>(models.size());
		for (String modelName : models) {
			valids.add(SwModel.modelId(repoId, modelName));
		}
	}

	@Override
	public boolean select(SwModel model) {
		return valids.contains(model.getId());
	}

	@Override
	public boolean selectDensity(SwModel model) {
		return valids.contains(model.getId());
	}
}
