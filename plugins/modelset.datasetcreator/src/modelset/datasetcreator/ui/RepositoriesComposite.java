package modelset.datasetcreator.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import modelset.common.db.ClusterFile;
import modelset.common.db.Repository;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.ControllerChangeListener;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FillLayout;

public class RepositoriesComposite extends Composite implements ControllerChangeListener {

	private Table table;
	private DatasetCreatorController controller;
	private TableViewer tableViewer;
	private Table tblClusters;
	private TableViewer tvClusters;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RepositoriesComposite(Composite parent, int style, @NonNull DatasetCreatorController controller) {
		super(parent, style);
		this.controller = controller;
		setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite cmpRepositories = new Composite(sashForm, SWT.NONE);
		cmpRepositories.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(cmpRepositories, SWT.NONE);
		lblNewLabel.setText("Repositories");
		new Label(cmpRepositories, SWT.NONE);
		
		tableViewer = new TableViewer(cmpRepositories, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumn.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnFolder = tableViewerColumn_1.getColumn();
		tblclmnFolder.setWidth(100);
		tblclmnFolder.setText("Folder");
		
		Composite composite = new Composite(cmpRepositories, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.setLayoutData(new RowData(91, SWT.DEFAULT));
		btnNewButton.setText("New");
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.setLayoutData(new RowData(91, SWT.DEFAULT));
		btnNewButton_1.setText("Remove");
		
		Button btnImport = new Button(composite, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, (ctx) -> {
						controller.importRepositories();
					});
				} catch (InvocationTargetException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnImport.setLayoutData(new RowData(93, SWT.DEFAULT));
		btnImport.setText("Import");
		
		Composite cmpClusters = new Composite(sashForm, SWT.NONE);
		cmpClusters.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel_1 = new Label(cmpClusters, SWT.NONE);
		lblNewLabel_1.setText("Clusters");
		new Label(cmpClusters, SWT.NONE);
		
		tvClusters = new TableViewer(cmpClusters, SWT.BORDER | SWT.FULL_SELECTION);
		tblClusters = tvClusters.getTable();
		tblClusters.setHeaderVisible(true);
		tblClusters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tvClusters, SWT.NONE);
		TableColumn tableColumn = tableViewerColumn_2.getColumn();
		tableColumn.setWidth(100);
		tableColumn.setText("Name");
		
		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tvClusters, SWT.NONE);
		TableColumn tableColumn_1 = tableViewerColumn_3.getColumn();
		tableColumn_1.setWidth(100);
		tableColumn_1.setText("Folder");
		
		Composite composite_2 = new Composite(cmpClusters, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		composite_2.setLayout(new RowLayout(SWT.VERTICAL));
		
		Button button = new Button(composite_2, SWT.NONE);
		button.setLayoutData(new RowData(91, -1));
		button.setText("New");
		
		Button button_1 = new Button(composite_2, SWT.NONE);
		button_1.setLayoutData(new RowData(91, -1));
		button_1.setText("Remove");
		
		Button btnImportClusters = new Button(composite_2, SWT.NONE);
		btnImportClusters.setLayoutData(new RowData(93, -1));
		btnImportClusters.setText("Import");
		btnImportClusters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
				try {
					dialog.run(true, true, (ctx) -> {
						controller.importClusters();
					});
				} catch (InvocationTargetException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		sashForm.setWeights(new int[] {1, 1});

		manualInit();
	}

	private void manualInit() {
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new RepositoryLabelProvider());
		tableViewer.setInput(controller.getRepositories());

		tvClusters.setContentProvider(ArrayContentProvider.getInstance());
		tvClusters.setLabelProvider(new ClusterLabelProvider());
		tvClusters.setInput(controller.getClusters());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components		
	}

	private class RepositoryLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) { }

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			Repository rep = (Repository) element;
			switch (columnIndex) {
			case 0: return rep.getName();
			case 1: return rep.getFileSet();
			default:
				return "<unknown>";
			}
		}
		
	}

	private class ClusterLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) { }

		@Override
		public void dispose() {
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) { }

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			ClusterFile rep = (ClusterFile) element;
			switch (columnIndex) {
			case 0: return rep.getRepoName();
			case 1: return rep.getClustersFile();
			default:
				return "<unknown>";
			}
		}
		
	}
	@Override
	public void updateRepositories(List<? extends Repository> repositories, @NonNull List<? extends ClusterFile> clusters) {
		this.tableViewer.setInput(repositories);
		this.tableViewer.refresh();
		
		this.tvClusters.setInput(clusters);
		this.tvClusters.refresh();
	}

	@Override
	public void updateExternalMetadata() {
		// Do nothing
	}
	
}
