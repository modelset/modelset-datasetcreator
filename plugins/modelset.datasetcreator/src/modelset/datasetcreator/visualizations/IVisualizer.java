package modelset.datasetcreator.visualizations;

import java.io.File;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;

public interface IVisualizer {

	List<File> toImage(@NonNull Resource resource, @NonNull File file);

}
