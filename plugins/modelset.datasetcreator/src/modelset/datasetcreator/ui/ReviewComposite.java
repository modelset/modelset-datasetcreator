package modelset.datasetcreator.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import modelset.common.annotations.AnnotationsModel;
import modelset.common.annotations.AnnotationsModel.Annotation;
import modelset.common.annotations.AnnotationsModel.AnnotationValue;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.ClusterFile;
import modelset.common.db.Repository;
import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.ControllerChangeListener;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.UpdatableUIElements;
import modelset.datasetcreator.utils.ExtensionPointUtils;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;

public class ReviewComposite extends Composite implements ControllerChangeListener, IDatasetView {
	private DatasetCreatorController controller;
	private Timer timer;
	private TreeViewer tvAnnotations;
	private Text txtFilter;
	private ModelViewSupport modelViewSupport;
	private ModelViewComposite modelView;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ReviewComposite(Composite parent, int style, @NonNull DatasetCreatorController controller) {
		super(parent, style);
		this.controller = controller;
		this.modelViewSupport = new ModelViewSupport(controller);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm_1 = new SashForm(this, SWT.NONE);
		
		Composite cmpAnnotations = new Composite(sashForm_1, SWT.NONE);
		cmpAnnotations.setLayout(new GridLayout(3, false));
		
		Label lblFilter = new Label(cmpAnnotations, SWT.NONE);
		lblFilter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilter.setText("Filter:");
		
		txtFilter = new Text(cmpAnnotations, SWT.BORDER);
		txtFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF)
					doSearch(txtFilter.getText());
			}
		});
		txtFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(cmpAnnotations, SWT.NONE);
		
		tvAnnotations = new TreeViewer(cmpAnnotations, SWT.BORDER);
		Tree tree = tvAnnotations.getTree();
		tvAnnotations.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					openModel((SwModel) ((IStructuredSelection) selection).getFirstElement());					
				}
			}
		});
		
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(tvAnnotations, SWT.NONE);
		TreeColumn trclmnAnnotation = treeViewerColumn.getColumn();
		trclmnAnnotation.setWidth(100);
		trclmnAnnotation.setText("Annotation");
		
		TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(tvAnnotations, SWT.NONE);
		TreeColumn trclmnNewColumn_1 = treeViewerColumn_1.getColumn();
		trclmnNewColumn_1.setWidth(20);
		trclmnNewColumn_1.setText("Information");
		
				Composite composite_2 = new Composite(cmpAnnotations, SWT.NONE);
				composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
				composite_2.setLayout(new RowLayout(SWT.VERTICAL));
				
				Button btnRefresh = new Button(composite_2, SWT.NONE);
				btnRefresh.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						computeDomains();
					}
				});
				btnRefresh.setLayoutData(new RowData(96, SWT.DEFAULT));
				btnRefresh.setText("Refresh");
						
						Button btnExport = new Button(composite_2, SWT.NONE);
						btnExport.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								runExport();
							}
						});
						btnExport.setLayoutData(new RowData(95, SWT.DEFAULT));
						btnExport.setText("Export");
						
						Button btnStats = new Button(composite_2, SWT.NONE);
						btnStats.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								runStats();
							}
						});
						btnStats.setLayoutData(new RowData(95, SWT.DEFAULT));
						btnStats.setText("Stats");
		
		modelView = new ModelViewComposite(sashForm_1, SWT.NONE, this);
		sashForm_1.setWeights(new int[] {1, 1});
		
		manualInit();
	}
	

	protected void openModel(SwModel model) {
		Resource r = modelViewSupport.selectModel(model, modelView);
		String extension = FilenameUtils.getExtension(model.getFile().getName());
		List<IDatasetCreatorExtensionHandler> handlers = ExtensionPointUtils.getHandlers(extension, r);
		
		if (handlers.size() > 0) {
			handlers.get(0).prepareUI(new UpdatableUIElements(controller, modelView.getModelTreeViewer(), null));
			modelView.updateVisualization(handlers.get(0));	
		}
	}


	protected void doSearch(String text) {
        SearchFilter filter = SearchFilter.fromText(text, controller);
        filter.applyTo(tvAnnotations);		
	}


	private void manualInit() {
		tvAnnotations.setContentProvider(new AnnotationsContentProvider());
		tvAnnotations.setLabelProvider(new AnnotationsLabelProvider());
		tvAnnotations.setComparator(new AnnotationsComparator());
	}

	protected void computeDomains() {
		AnnotationsModel model = controller.getAnnotationModel();
		tvAnnotations.setInput(model);
		tvAnnotations.refresh(true);
	}
		

	private void runExport() {
		try {
			controller.export();
		} catch (IOException | SyntaxError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.openError(null, "Error", e.getMessage());
		}		
	}
	
	private void runStats() {
		try {
			controller.stats();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageDialog.openError(null, "Error", e.getMessage());
		}		
	}


	@Override
	public void dispose() {
		this.timer.cancel();
		super.dispose();
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void updateRepositories(List<? extends Repository> repositories,
			@NonNull List<? extends ClusterFile> clusters) {
		// Do nothing. Possibly delete.

	}
	@Override
	public void updateExternalMetadata() {
		// Do nothing
	}
	
	public static class AnnotationsContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof AnnotationsModel) {
				AnnotationsModel model = (AnnotationsModel) inputElement;
				Collection<? extends Annotation> annotations = model.getAnnotations();
				return annotations.toArray(Annotation.EMPTY);
			}
			return null;
		}

		@Override
		public Object[] getChildren(Object element) {		
			if (element instanceof Annotation) {
				Annotation v = (Annotation) element;
				// Use the value as a string 
				return v.values().toArray(new Object[0]);
			} else if (element instanceof AnnotationValue) {
				return ((AnnotationValue) element).toArray(SwModel.EMPTY);
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return (element instanceof AnnotationsModel) || (element instanceof Annotation) || (element instanceof AnnotationValue);
		}
		
	}
	
	public static class AnnotationsLabelProvider implements ITableLabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose() {
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
			switch (columnIndex) {
			case 0:
				if (element instanceof AnnotationsModel.Annotation) {
					return ((AnnotationsModel.Annotation) element).getAnnotationName();
				} else if (element instanceof AnnotationValue) {
					return ((AnnotationValue) element).getValue();
				} else if (element instanceof SwModel) {
					return ((SwModel) element).getName();
				}
				break;
			case 1:
				if (element instanceof AnnotationsModel.Annotation) {
					return ((AnnotationsModel.Annotation) element).totalElements() + "|" + ((AnnotationsModel.Annotation) element).size();				
				} else if (element instanceof AnnotationValue) {
					return "" + ((AnnotationValue) element).size();
				} else if (element instanceof SwModel) {
					return ((SwModel) element).getMetadata();
				}		
				break;
			default:
				break;
			}
			return null;
		}

	}
	
	public static class AnnotationsComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof AnnotationsModel.Annotation) {
				AnnotationsModel.Annotation ann1 = (AnnotationsModel.Annotation) e1;
				AnnotationsModel.Annotation ann2 = (AnnotationsModel.Annotation) e2;
				return ann1.getAnnotationName().compareTo(ann2.getAnnotationName());
			} else if (e1 instanceof AnnotationValue) {
				AnnotationsModel.AnnotationValue ann1 = (AnnotationsModel.AnnotationValue) e1;
				AnnotationsModel.AnnotationValue ann2 = (AnnotationsModel.AnnotationValue) e2;
				return ann1.getValue().compareTo(ann2.getValue());
			} else if (e1 instanceof SwModel) {
				SwModel m1 = (SwModel) e1;
				SwModel m2 = (SwModel) e2;
				return m1.getName().compareTo(m2.getName());
			}
			return 0;
		}
	}

	/** IDatasetView **/
	
	@Override
	public void save(String txtMetadata) {
		SwModel model = controller.getCurrentModel();
		if (model != null) {			
			model.setMetadata(txtMetadata);
			controller.saveModel(model);
			this.tvAnnotations.refresh(model);			
		}		
	}


	@Override
	public void nextItem() {
		// Ignore because this part is not as interactive as the main browser
	}


	@Override
	public DatasetCreatorController getController() {
		return controller;
	}
}
