package modelset.common.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.AttributeOwner;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UseCase;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.DatasetDb;
import modelset.common.db.IDatasetOrigin;
import modelset.common.db.IRepositoryProvider;
import modelset.common.db.SwModel;

public class DatasetStats {

	public static final DatasetStats INSTANCE = new DatasetStats();
	
	public void stats(IRepositoryProvider repoProvider, DatasetDb db, String folder) throws IOException {

        BufferedWriter writerEcore = Files.newBufferedWriter(Paths.get(folder, "stats-ecore.csv"));
        CSVPrinter statsEcorePrinter = new CSVPrinter(writerEcore, CSVFormat.DEFAULT.withHeader("id", "category", "tags", "purpose", "notation", "tool", "numElements", "numPackages", "numClasses", "numEnums", "numDatatypes", "numAttributes", "numReferences"));
		
        BufferedWriter writerUml = Files.newBufferedWriter(Paths.get(folder, "stats-uml.csv"));
        CSVPrinter statsUmlPrinter = new CSVPrinter(writerUml, CSVFormat.DEFAULT.withHeader("modelId", "category", "tags", "mainDiagram", "numElements", "numStates", "numTransitions", "numInteractions", "numActivities", "numComponents", "numPackages", "numClasses", "numEnums", "numDatatypes", "numProperties", "numRelationships", "numOperations", "numGeneralizations", "numActors", "numUseCases", "numAssociations"));
		
        
		Collection<? extends SwModel> models = db.getModels();
		for (SwModel swModel : models) {
			if (! swModel.isTagged())
				continue;
			
			File file = swModel.getFulllFile(repoProvider);
			if(!file.exists())
				continue;
			Resource r = loadModel(file);
			// A bit of a hack...
			if (repoProvider.getOrigin() instanceof IDatasetOrigin.GithubDataset) {
				processEcore(statsEcorePrinter, swModel.getId(), swModel.getMetadata(), r);			
			} else {
				processUML(statsUmlPrinter, swModel.getId(), swModel.getMetadata(), r);		
			}
//			process(r);
		}
				
		statsEcorePrinter.close();
		statsUmlPrinter.close();
	}
	
	private void processEcore(CSVPrinter printer, String modelId, String metadata, Resource r) throws IOException {
		List<String> uris = new ArrayList<String>();
		
		// Info we should have
		String category = "";
		String tags = "";
		String purpose = "";
		String notation = "";
		String tool = "";
		
		try {
			if (metadata != null) {
				Map<String, List<String>> map = AnnotationsValidator.INSTANCE.toMap(metadata);
				
				if (map.containsKey("category")) {
					category = map.get("category").get(0);
				} else if (map.containsKey("domain")) {
					category = map.get("domain").get(0);
				}  
				
				if (map.containsKey("tags")) {
					tags = String.join(",", map.get("tags"));
				}
				
				if (map.containsKey("purpose")) {
					purpose = map.get("purpose").get(0);
				}
				
				if (map.containsKey("notation")) {
					notation = map.get("category").get(0);
				}
				
				if (map.containsKey("tool")) {
					tool = map.get("tool").get(0);
				}
			}
		} catch (SyntaxError e) {
			System.out.println("Error processing metadata at DatasetStats for model: " + modelId);
			e.printStackTrace();
		}
		
		// Numeric info	
		int numElements   = 0;
		int numPackages   = 0;
		int numClasses    = 0;
		int numEnums      = 0;
		int numDatatypes  = 0;
		int numAttributes = 0;
		int numReferences = 0;
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			numElements++;
			if (obj instanceof EPackage) {
				numPackages++;
				String nsURI = ((EPackage) obj).getNsURI();
				if (nsURI != null)
					uris.add(nsURI);
			} else if (obj instanceof EClass) {
				numClasses++;
			} else if (obj instanceof EAttribute) {
				numAttributes++;
			} else if (obj instanceof EReference) {
				numReferences++;
			} else if (obj instanceof EEnum) {
				numEnums++;
			} else if (obj instanceof EDataType) {
				numDatatypes++;
			}
		}
		
		// Print!
		printer.printRecord(modelId, category, tags, purpose, notation, tool, numElements, numPackages, numClasses, numEnums, numDatatypes, numAttributes, numReferences);
		
	}
	
	private void processUML(CSVPrinter printer, String modelId, String metadata, Resource r) throws IOException {// Info we should have
		String category = "";
		String tags = "";
		String mainDiagram = "";
		
		try {
			if (metadata != null) {
				Map<String, List<String>> map = AnnotationsValidator.INSTANCE.toMap(metadata);
				
				if (map.containsKey("category")) {
					category = map.get("category").get(0);
				} else if (map.containsKey("domain")) {
					category = map.get("domain").get(0);
				}  
				
				if (map.containsKey("tags")) {
					tags = String.join(",", map.get("tags"));
				}
				
				if (map.containsKey("type")) {
					mainDiagram = map.get("type").get(0);
				}
			}
		} catch (SyntaxError e) {
			System.out.println("Error processing metadata at DatasetStats for model: " + modelId);
			e.printStackTrace();
		}
		
		// Numeric info	
		int numElements   = 0;
		
		int numStates = 0;
		int numTransitions = 0;
		
		int numInteractions = 0;
		
		int numActivities = 0;
		
		int numComponents = 0;
		
		int numPackages   = 0;
		int numClasses    = 0;
		int numEnums      = 0;
		int numDatatypes  = 0;
		int numProperties = 0;
		int numRelationships = 0;
		int numOperations = 0;
		int numGeneralizations = 0;
		
		int numActors = 0;
		int numUseCases = 0;
		int numAssociations = 0;
				
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			String type = null;
			numElements++;
			if (obj instanceof State) {
				numStates++;
			} else if (obj instanceof Transition) {
				numTransitions++;
			} else if (obj instanceof Interaction) {
				numInteractions++;
			} else if (obj instanceof Activity) {
				numActivities++;
			} else if (obj instanceof Component) {
				numComponents++;
			} else if (obj instanceof org.eclipse.uml2.uml.Package) {
				numPackages++;
			} else if (obj instanceof org.eclipse.uml2.uml.Class) {
				numClasses++;
			} else if (obj instanceof Enumeration) {
				numEnums++;
			} else if (obj instanceof DataType) {
				numDatatypes++;
			} else if (obj instanceof org.eclipse.uml2.uml.Property) {
				numProperties++;
			} else if (obj instanceof org.eclipse.uml2.uml.Relationship) {
				numRelationships++;
			} else if (obj instanceof org.eclipse.uml2.uml.Operation) {
				numOperations++;
			} else if (obj instanceof org.eclipse.uml2.uml.Generalization) {
				numGeneralizations++;
			} else if (obj instanceof Actor) {
				numActors++;
			} else if (obj instanceof UseCase) {
				numUseCases++;
			} else if (obj instanceof Association) {
				numAssociations++;
			}
			
		}
		 
		// Print!
		printer.printRecord(modelId, category, tags, mainDiagram, numElements, numStates, numTransitions, numInteractions, numActivities, numComponents, numPackages, numClasses, numEnums, numDatatypes, numProperties, numRelationships, numOperations, numGeneralizations, numActors, numUseCases, numAssociations);
				
	}

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
	
}
