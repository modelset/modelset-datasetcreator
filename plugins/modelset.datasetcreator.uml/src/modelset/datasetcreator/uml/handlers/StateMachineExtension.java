package modelset.datasetcreator.uml.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.edit.providers.StateMachineItemProvider;

import lombok.NonNull;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.uml.visualization.StatemachineDsVisualization;
import modelset.datasetcreator.visualizations.IVisualizer;

public class StateMachineExtension implements IDatasetCreatorExtensionHandler {

	private Resource resource;
	
	@Override
	public void setResource(@NonNull Resource r) {
		this.resource = r;
	}
	
	@Override
	public String getId() {
		return "StateMachine";
	}
	
	@Override
	public List<IVisualizer> getVisualizer() {
		return Collections.singletonList(new StatemachineDsVisualization());
	}
	
	@Override
	public IOutline getOutline() {
		return new StateMachineOutline(getStateMachines(resource));
	}
	
	public static class StateMachineOutline extends ArrayContentProvider implements IOutline, ILabelProvider {
		
		private List<StateMachine> elements;

		public StateMachineOutline(List<StateMachine> stateMachines) {
			this.elements = stateMachines;
		}

		@Override
		public Object getInput() {
			return elements;
		}
		
		@Override
		public IContentProvider getContentProvider() {			
			return this;
		}
		
		@Override
		public ILabelProvider getLabelProvider() {
			return this;
		}

		@Override
		public void addListener(ILabelProviderListener listener) { }

		@Override
		public void dispose() { }

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) { }

		@Override
		public Image getImage(Object element) {
			Optional<ImageDescriptor> opt = ResourceLocator.imageDescriptorFromBundle(StateMachineItemProvider.class, "icons/full/obj16/StateMachine.gif");
			return opt.map(img -> img.createImage()).orElse(null);
		}

		@Override
		public String getText(Object element) {
			return ((StateMachine) element).getName();
		}
		
	}
	
	private List<StateMachine> getStateMachines(@NonNull Resource r) {
		List<StateMachine> result = new ArrayList<StateMachine>();
		
		TreeIterator<EObject> it = resource.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof StateMachine) {
				result.add((StateMachine) obj);
			}
		}
		
		return result;
	}

}
