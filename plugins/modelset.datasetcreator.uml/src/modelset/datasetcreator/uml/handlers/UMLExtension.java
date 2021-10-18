package modelset.datasetcreator.uml.handlers;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.edit.providers.StateMachineItemProvider;

import lombok.NonNull;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.extensions.UpdatableUIElements;
import modelset.datasetcreator.uml.visualization.ActivityDiagramDsVisualization;
import modelset.datasetcreator.uml.visualization.InteractionDiagramDsVisualization;
import modelset.datasetcreator.uml.visualization.StatemachineDsVisualization;
import modelset.datasetcreator.visualizations.IVisualizer;

public class UMLExtension implements IDatasetCreatorExtensionHandler {

	private Resource resource;

	@Override
	public String getId() {		
		return "UML";
	}

	@Override
	public void setResource(Resource r) {
		this.resource = r;		
	}

	@Override
	public List<IVisualizer> getVisualizer() {
		return Arrays.asList(
				new ActivityDiagramDsVisualization(),
				new StatemachineDsVisualization(),
				new InteractionDiagramDsVisualization()
		);
	}

	@Override
	public IOutline getOutline() {
		return new UMLOutline(getDiagrams(resource));
	}

	@Override
	public void prepareUI(UpdatableUIElements ui) {
		ui.getModelTreeViewer().expandToLevel(3);
	}
	
	private List<NamedElement> getDiagrams(@NonNull Resource r) {
		List<NamedElement> result = new ArrayList<NamedElement>();
		
		TreeIterator<EObject> it = resource.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof StateMachine) {
				result.add((StateMachine) obj);
			} else if (obj instanceof Interaction) {
				result.add((Interaction) obj);
			} else if (obj instanceof Activity) {
				result.add((Activity) obj);
			} else if (obj instanceof org.eclipse.uml2.uml.Package) {
				org.eclipse.uml2.uml.Package pkg = (org.eclipse.uml2.uml.Package) obj;
				if (pkg.getOwnedTypes().stream().anyMatch(t -> t instanceof Class)) {
					result.add(pkg);
				}
			}
		}
		
		return result;
	}

	public static class UMLOutline extends ArrayContentProvider implements IOutline, ILabelProvider {

		@NonNull
		private List<NamedElement> elements;

		public UMLOutline(@NonNull List<NamedElement> elements) {
			this.elements = elements;
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
		public void removeListener(ILabelProviderListener listener) {
			
		}

		@Override
		public Image getImage(Object element) {
			String image = null;
			if (element instanceof StateMachine) {
				image = "icons/full/obj16/StateMachine.gif";
			} else if (element instanceof Interaction) {
				image = "icons/full/obj16/Interaction.gif";
			} else if (element instanceof org.eclipse.uml2.uml.Package) {
				image = "icons/full/obj16/Package.gif";
			} else if (element instanceof Activity) {
				image = "icons/full/obj16/Activity.gif";
			}

			if (image == null)
				return null;
			
			Optional<ImageDescriptor> opt = ResourceLocator.imageDescriptorFromBundle(StateMachineItemProvider.class, image);
			return opt.map(img -> img.createImage()).orElse(null);
		}

		@Override
		public String getText(Object element) {
			NamedElement e = (NamedElement) element;			
			return e.getName();
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

	}
}
