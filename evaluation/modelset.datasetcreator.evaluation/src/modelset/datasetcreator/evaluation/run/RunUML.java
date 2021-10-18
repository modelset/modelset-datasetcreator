package modelset.datasetcreator.evaluation.run;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import modelset.datasetcreator.controller.DatasetCreatorController.InvalidFormat;
import modelset.datasetcreator.evaluation.EvaluationMain;

public class RunUML {

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
				// "UML_Density_Woosh_History"	
				"UML_Density_Lucene_History"	
				//"UML_Random_Lucene_History"	
		};
		EvaluationMain.main(args);
	}

}
