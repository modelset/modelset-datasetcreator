package modelset.datasetcreator.ecore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.provider.EPackageItemProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.extensions.UpdatableUIElements;
import modelset.datasetcreator.visualizations.IVisualizer;

public class EcoreHandler implements IDatasetCreatorExtensionHandler {

	private Resource resource;

	@Override
	public void setResource(Resource r) {
		this.resource = r;
	}

	@Override
	public String getId() {
		return "Ecore";
	}

	@Override
	public List<IVisualizer> getVisualizer() {
		return Collections.emptyList();
	}

	@Override
	public IOutline getOutline() {
		return new EcoreOutline(getPackages(resource));
	}

	@Override
	public void prepareUI(UpdatableUIElements ui) {
		ui.getModelTreeViewer().expandToLevel(2);
	}
	
	public static class EcoreOutline extends ArrayContentProvider implements IOutline, ILabelProvider {

		private List<EPackage> packages;

		public EcoreOutline(List<EPackage> packages) {
			this.packages = packages;
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
			Optional<ImageDescriptor> opt = ResourceLocator.imageDescriptorFromBundle(EPackageItemProvider.class, "icons/full/obj16/EPackage.gif");
			return opt.map(img -> img.createImage()).orElse(null);
		}

		@Override
		public String getText(Object element) {
			return ((EPackage) element).getName();
		}

		@Override
		public Object getInput() {
			return packages;
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

	private List<EPackage >getPackages(Resource r) {
		List<EPackage> packages = new ArrayList<>();
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof EPackage) {
				packages.add((EPackage) obj);
			}
		}			
		return packages;
	}

}
