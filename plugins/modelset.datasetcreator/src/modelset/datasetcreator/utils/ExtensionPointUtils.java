package modelset.datasetcreator.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IDatasetTypeHandler;
import modelset.datasetcreator.visualizations.IVisualizer;

public class ExtensionPointUtils {

	public static final String MODELTYPE_EXTENSION_POINT = "mdeml.datasetcreator.modeltype";
	public static final String DATASETTYPE_EXTENSION_POINT = "mdeml.datasetcreator.datasettype";

	public static List<IDatasetCreatorExtensionHandler> getHandlers(String extension, @NonNull Resource r) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = registry.getConfigurationElementsFor(MODELTYPE_EXTENSION_POINT);
		
		List<IDatasetCreatorExtensionHandler> result = new ArrayList<>();
		for (IConfigurationElement ce : extensions) {
			String declaredExtension = ce.getAttribute("extension");
			// This is a set of nsURI, with each uri separated by ,
			String uri = ce.getAttribute("nsURI");
			
			if (extension.toLowerCase().equals(declaredExtension) || "*".equals(declaredExtension)) {
				if (uri != null && ! hasURI(r, uri)) {
					continue;
				}
				
				try {
					IDatasetCreatorExtensionHandler h = (IDatasetCreatorExtensionHandler) ce.createExecutableExtension("handler");
					h.setResource(r);
					result.add(h);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;		
	}

	private static boolean hasURI(@NonNull Resource r, @NonNull String nsURIs) {
		Set<String> uris = new HashSet<>();
		Collections.addAll(uris, nsURIs.split(","));
		
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			EClass c = obj.eClass();
			EPackage pkg = c.getEPackage();
			if (pkg != null && uris.contains(pkg.getNsURI())) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * Allow extensions to do its own registration before using models affected by them.
	 */
	public static void forceLoadExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = registry.getConfigurationElementsFor(MODELTYPE_EXTENSION_POINT);
		for (IConfigurationElement ce : extensions) {
			IDatasetCreatorExtensionHandler h;
			try {
				h = (IDatasetCreatorExtensionHandler) ce.createExecutableExtension("handler");
				h.initialize();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<IDatasetTypeHandler> getDatasetExtensions(@NonNull String datasetType, @NonNull SwModel model, @NonNull Resource r) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = registry.getConfigurationElementsFor(DATASETTYPE_EXTENSION_POINT);
		
		List<IDatasetTypeHandler> result = new ArrayList<>();
		for (IConfigurationElement ce : extensions) {
			String declaredDatasetType = ce.getAttribute("name");			
			if (datasetType.toLowerCase().equals(declaredDatasetType)) {			
				try {
					IDatasetTypeHandler h = (IDatasetTypeHandler) ce.createExecutableExtension("handler");
					h.setResource(model, r);
					if (h.isApplicable())
						result.add(h);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;		
	}
	
}
