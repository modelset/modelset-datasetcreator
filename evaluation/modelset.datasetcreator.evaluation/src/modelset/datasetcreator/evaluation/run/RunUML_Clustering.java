package modelset.datasetcreator.evaluation.run;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import modelset.datasetcreator.controller.DatasetCreatorController.InvalidFormat;
import modelset.datasetcreator.evaluation.EvaluationMain;

public class RunUML_Clustering {

	@Test
	public void test() throws IOException, InvalidFormat {
		Map<String, String> env = System.getenv();
		String path = env.get("PATH_TO_DB");
		if (path == null) {
			// For example: "/home/username/projects/mdeml/dataset-experiments/datasets/dataset.genmymodel/data/genmymodel.db"
			throw new IllegalStateException("Please, set variable PATH_TO_DB in launch configuration");
		}
		
		String[] args = new String[] {
				path,
				// "UML_Random_KMeans_None"	
				//"UML_Random_HC_None"
				"UML_Random_DBScan_None"
		};
		EvaluationMain.main(args);
	}

}
