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
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.edit.providers.ActivityItemProvider;

import lombok.NonNull;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.uml.visualization.ActivityDiagramDsVisualization;
import modelset.datasetcreator.visualizations.IVisualizer;

public class ActivityDiagramExtension implements IDatasetCreatorExtensionHandler {

	private Resource resource;

	@Override
	public void setResource(@NonNull Resource r) {
		this.resource = r;
	}
	
	@Override
	public String getId() {
		return "ActivityDiagram";
	}
	
	@Override
	public List<IVisualizer> getVisualizer() {
		return Collections.singletonList(new ActivityDiagramDsVisualization());
	}
	
	@Override
	public IOutline getOutline() {
		return new ActivityDiagramOutline(getActivities(resource));
	}
	
	public static class ActivityDiagramOutline extends ArrayContentProvider implements IOutline, ILabelProvider {
		
		private List<Activity> elements;

		public ActivityDiagramOutline(List<Activity> activities) {
			this.elements = activities;
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
			Optional<ImageDescriptor> opt = ResourceLocator.imageDescriptorFromBundle(ActivityItemProvider.class, "icons/full/obj16/Activity.gif");
			return opt.map(img -> img.createImage()).orElse(null);
		}

		@Override
		public String getText(Object element) {
			return ((Activity) element).getName();
		}
		
	}
	
	private List<Activity> getActivities(@NonNull Resource r) {
		List<Activity> result = new ArrayList<Activity>();
		
		TreeIterator<EObject> it = resource.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof Activity) {
				result.add((Activity) obj);
			}
		}
		
		return result;
	}

}
