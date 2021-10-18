package modelset.datasetcreator.ecore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;

import modelset.common.db.SwModel;
import modelset.datasetcreator.extensions.IDatasetTypeHandler;
import modelset.datasetcreator.extensions.UpdatableUIElements;

public class GithubDatasetType implements IDatasetTypeHandler {

	private Resource resource;
	private SwModel model;

	public GithubDatasetType() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setResource(SwModel model, Resource r) {
		this.resource = r;
		this.model = model;
	}

	@Override
	public boolean isApplicable() {
		return resource.getURI().toString().endsWith(".ecore");
	}
	
	@Override
	public void prepareUI(UpdatableUIElements ui) {
		TableViewer tvRelated = ui.getTableViewerRelated();
		
		String projectRelative = SwModel.modelId(model.getRepo(), model.getFile().toPath().subpath(0, 3).toString());		
				
		List<SwModel> toShow = new ArrayList<SwModel>();
		List<SwModel> models = ui.getController().getModels();
		for (SwModel swModel : models) {
			if (swModel.getId().startsWith(projectRelative)) {
				toShow.add(swModel);
			}
		}
	
		tvRelated.setContentProvider(TableViewerOutline.INSTANCE);
		tvRelated.setLabelProvider(TableViewerOutline.INSTANCE);
		tvRelated.setInput(toShow);
	}

	public static class TableViewerOutline implements IStructuredContentProvider, ITableLabelProvider {

		public static final TableViewerOutline INSTANCE = new TableViewerOutline();
		
		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			SwModel m = (SwModel) element;
			if (columnIndex == 0) {
				return m.getName();
			} else if (columnIndex == 1) {
				return m.getMetadata();
			}
			return null;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray(SwModel.EMPTY);
		}

		@Override
		public void dispose() {
			
		}		
	}
	
}
