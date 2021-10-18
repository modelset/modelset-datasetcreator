package modelset.datasetcreator.uml.visualization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;

import modelset.datasetcreator.visualizations.IVisualizer;
import modelset.datasetcreator.visualizer.uml.ActivityDiagramVisualizer;

public class ActivityDiagramDsVisualization implements IVisualizer {

	@Override
	public List<File> toImage(Resource resource, File folder) {
		List<File> results = new ArrayList<>();
		TreeIterator<EObject> it = resource.getAllContents();
		int i = 0;
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof Activity) {
				try {
					i++;
					String png = "sm" + "_" + i + ".png";
					File file = Paths.get(folder.getAbsolutePath(), png).toFile();
					ActivityDiagramVisualizer viz = new ActivityDiagramVisualizer();
					viz.visualize((Activity) obj, file);
					results.add(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return results;
	}

}
