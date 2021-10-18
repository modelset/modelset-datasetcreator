package modelset.datasetcreator.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import lombok.NonNull;
import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.utils.ExtensionPointUtils;

public class ModelViewSupport {

	private @NonNull DatasetCreatorController controller;

	public ModelViewSupport(@NonNull DatasetCreatorController controller) {
		this.controller = controller;
	}

	@NonNull
	public Resource selectModel(@NonNull SwModel model, @NonNull ModelViewComposite modelView) {
		String repo = model.getRepo();
		String root = controller.getRootFolder(repo);
		if (root == null) {
			MessageDialog.openError(null, "Error", "Repository not configured yet: " + repo);
			return null;
		}
				
		ExtensionPointUtils.forceLoadExtensions();
		
		File f = Paths.get(root, model.getFile().getPath()).toFile();		
		Resource r = loadModel(f);

		controller.selectModel(model);		
		modelView.openModel(r, f, model);
		
		return r;
	}
	
	/**
	 * Loads an EMF model. First it tries to use regular resource factories. If it fails it uses a specific UML
	 * factory. This is to be able to load UML models stored with .xmi extension.
	 * 
	 * There might be more elegant solutions, but they imply: a) external configuration to day which files to load
	 * as UML files or b) inspecting the raw file to try to find an UML nsURI.
	 */
	public Resource loadModel(File f) {
		URI uri = URI.createFileURI(f.getAbsolutePath());
		try {
			ResourceSet rs = new ResourceSetImpl();
			Resource r = rs.getResource(uri, true);
			return r;
		} catch (Exception e) {
			UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
			Resource r = factory.createResource(uri);
			try {
				r.load(null);
			} catch (IOException e1) {
				throw new RuntimeException(e);
			}
			return r;
		}
	}

	public DatasetCreatorController getController() {
		return controller;
	}
}
