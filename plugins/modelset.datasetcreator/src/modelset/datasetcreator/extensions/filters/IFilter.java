package modelset.datasetcreator.extensions.filters;

import modelset.common.db.SwModel;

public interface IFilter {

	public boolean select(SwModel model);
	
	public boolean selectDensity(SwModel model);
	
	
}
