package modelset.datasetcreator.evaluation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.InvalidFormat;
import modelset.datasetcreator.evaluation.uml.UML_Density_Lucene_History;
import modelset.datasetcreator.evaluation.uml.UML_Density_Whoosh_History;
import modelset.datasetcreator.evaluation.uml.UML_Random_Lucene_History;

public class EvaluationMain {

	public static void main(String[] args) throws IOException, InvalidFormat {
		File db = new File(args[0]);
		String algorithm = args[1];
		
		File dsc = new File(FilenameUtils.removeExtension(db.getAbsolutePath()) + ".dsc");
		
		String dscContents = FileUtils.readFileToString(dsc, "UTF-8");
		
		DatasetCreatorController controller = new DatasetCreatorController(db);
		controller.processRepositories(dscContents, dsc.getParentFile());
		
		LabellingAlgorithm alg;
		
		if (algorithm.equals("Random_Mar_History")) {
			alg = new Random_Mar_History(controller);
		} else if (algorithm.equals("Density_Mar_History")) {
			alg = new Density_Mar_History(controller);

		} else if (algorithm.equals("Density_Woosh_History")) {
			alg = new Density_Whoosh_History(controller);

		} else if (algorithm.equals("Random_KMeans_None")) {
			alg = new Random_Clustering_None(controller, "repo-ecore-all", new File("../../../dataset-experiments/data/kmeans.csv"));
		} else if (algorithm.equals("Random_HC_None")) {
			alg = new Random_Clustering_None(controller, "repo-ecore-all", new File("../../../dataset-experiments/data/HC.csv"));
		} else if (algorithm.equals("Random_DBScan_None")) {
			alg = new Random_Clustering_None(controller, "repo-ecore-all", new File("../../../dataset-experiments/data/dbscanEcore.csv"));
		} else if (algorithm.equals("UML_Random_KMeans_None")) {
			alg = new Random_Clustering_None(controller, "repo-genmymodel-uml", new File("../../../dataset-experiments/data/kmeansUML.csv"));
		} else if (algorithm.equals("UML_Random_HC_None")) {
			alg = new Random_Clustering_None(controller, "repo-genmymodel-uml", new File("../../../dataset-experiments/data/HCUML.csv"));
		} else if (algorithm.equals("UML_Random_DBScan_None")) {
			alg = new Random_Clustering_None(controller, "repo-genmymodel-uml", new File("../../../dataset-experiments/data/dbscanUML.csv"));
		
			
		// UML	
		} else if (algorithm.equals("UML_Density_Woosh_History")) {
			alg = new UML_Density_Whoosh_History(controller);
		} else if (algorithm.equals("UML_Density_Lucene_History")) {
			// TODO: obtain the args[2]
			// Please, install this in the proper location, index and set this value accordingly
			String luceneIndex = "/home/username/usr/lucene-8.6.0/index";
			alg = new UML_Density_Lucene_History(controller, luceneIndex);
		} else if (algorithm.equals("UML_Random_Lucene_History")) {
			// TODO: obtain the args[2]
			// Please, install this in the proper location, index and set this value accordingly
			String luceneIndex = "/home/username/usr/lucene-8.6.0/index";
			alg = new UML_Random_Lucene_History(controller, luceneIndex);
		} else {
			throw new IllegalArgumentException(algorithm);
		}
		
		alg.evaluate();
	}
	
}
