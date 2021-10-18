package modelset.datasetcreator.extensions;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;

public interface IOutline {
	
	public Object getInput();
	
	public IContentProvider getContentProvider();

	public ILabelProvider getLabelProvider();
	
}
