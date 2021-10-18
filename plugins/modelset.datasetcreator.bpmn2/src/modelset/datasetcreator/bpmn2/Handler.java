package modelset.datasetcreator.bpmn2;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.models.bpmn.BPMNLoader;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.visualizations.IVisualizer;

public class Handler implements IDatasetCreatorExtensionHandler {

	public Handler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId() {
		return "BPMN2";
	}
	
	@Override
	public void initialize() {
		BPMNLoader.initBPMN();
	}
	
	@Override
	public void setResource(Resource r) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IVisualizer> getVisualizer() {
		return Collections.emptyList();
	}

	@Override
	public IOutline getOutline() {
		// TODO Auto-generated method stub
		return null;
	}

}
