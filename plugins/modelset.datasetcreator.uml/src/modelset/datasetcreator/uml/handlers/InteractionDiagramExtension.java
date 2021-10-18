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
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.edit.providers.InteractionItemProvider;
import org.eclipse.uml2.uml.edit.providers.PackageItemProvider;

import lombok.NonNull;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.uml.visualization.InteractionDiagramDsVisualization;
import modelset.datasetcreator.visualizations.IVisualizer;

public class InteractionDiagramExtension implements IDatasetCreatorExtensionHandler {

	private Resource resource;

	@Override
	public void setResource(@NonNull Resource r) {
		this.resource = r;
	}
	
	@Override
	public String getId() {
		return "InteractionDiagram";
	}
	
	@Override
	public List<IVisualizer> getVisualizer() {
		return Collections.singletonList(new InteractionDiagramDsVisualization());
	}
	
	@Override
	public IOutline getOutline() {
		return new InteractionDiagramOutline(getInteractions(resource));
	}
	
	public static class InteractionDiagramOutline extends ArrayContentProvider implements IOutline, ILabelProvider {
		
		private List<Interaction> elements;

		public InteractionDiagramOutline(List<Interaction> interactions) {
			this.elements = interactions;
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
			Optional<ImageDescriptor> opt = ResourceLocator.imageDescriptorFromBundle(InteractionItemProvider.class, "icons/full/obj16/Interaction.gif");
			return opt.map(img -> img.createImage()).orElse(null);
		}

		@Override
		public String getText(Object element) {
			return ((Interaction) element).getName();
		}
		
	}
	
	private List<Interaction> getInteractions(@NonNull Resource r) {
		List<Interaction> result = new ArrayList<Interaction>();
		
		TreeIterator<EObject> it = resource.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof Interaction) {
				result.add((Interaction) obj);
			}
		}
		
		return result;
	}

}
