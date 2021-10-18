package modelset.datasetcreator.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.ClusterFile;
import modelset.common.db.Event;
import modelset.common.db.Repository;
import modelset.common.db.SwModel;
import modelset.common.db.ExternalMetadata.ModelData;
import modelset.common.services.ISearchService;
import modelset.common.services.ISearchService.Item;
import modelset.common.services.ISearchService.SearchResult;
import modelset.datasetcreator.algorithms.AsyncSearcher.AsyncResult;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.ControllerChangeListener;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.extensions.IDatasetTypeHandler;
import modelset.datasetcreator.extensions.IOutline;
import modelset.datasetcreator.extensions.UpdatableUIElements;
import modelset.datasetcreator.ui.ScoreComparator.ScoreViewerFilter;
import modelset.datasetcreator.utils.ExtensionPointUtils;

public class MainComposite extends Composite implements ControllerChangeListener, IDatasetView {
	private Text txtSearch;
	private Table table;
	@NonNull
	protected final DatasetCreatorController controller;
	private TableViewer modelSetTableViewer;
	
	private Table tabOutline;
	private TableViewer tableViewerOutline;
	private Combo cmbModelTypes;
	private TableViewer tvProjectRelated;
	
	private ModelViewComposite modelView;
	private ModelViewSupport modelViewSupport;
	private @NonNull IProject project;
	private @NonNull IStatusViewer statusViewer;
	
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MainComposite(Composite parent, int style, @NonNull DatasetCreatorController controller, @NonNull IProject project, @NonNull IStatusViewer statusViewer) {
		super(parent, style);
		this.controller = controller;
		this.project = project;
		this.controller.addChangeListener(this);
		this.modelViewSupport = new ModelViewSupport(controller);
		this.statusViewer = statusViewer;
		
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(this, SWT.NONE);
		
		Composite cmpLeft = new Composite(sashForm, SWT.NONE);
		cmpLeft.setLayout(new GridLayout(1, false));
		
		SashForm sashForm_3 = new SashForm(cmpLeft, SWT.VERTICAL);
		sashForm_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite cmpExplorer = new Composite(sashForm_3, SWT.NONE);
		cmpExplorer.setLayout(new GridLayout(4, false));
		
		Label lblSearch = new Label(cmpExplorer, SWT.NONE);
		lblSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblSearch.setText("Search");
		
		txtSearch = new Text(cmpExplorer, SWT.BORDER);
		/*
		txtSearch.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				applySearchFilter();
			}
		});
		*/
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF)
					applySearchFilter();
			}
		});
		
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnNewButton = new Button(cmpExplorer, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				swapRefinedSearch();
			}
		});
		btnNewButton.setImage(ResourceManager.getPluginImage("mdeml.datasetcreator", "icons/swap.png"));
		btnNewButton.setText("Related");
		
		Button btnNext = new Button(cmpExplorer, SWT.NONE);
		btnNext.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNext.setImage(ResourceManager.getPluginImage("mdeml.datasetcreator", "icons/next.png"));
		btnNext.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				nextByDensity();
			}
		});
		btnNext.setText("Next");
		
		currentSearchLnk = new Link(cmpExplorer, SWT.NONE);
		currentSearchLnk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openModel(currentSearch);
			}
		});
		currentSearchLnk.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		currentSearchLnk.setText("<a>No search</a>");
		
		Button button = new Button(cmpExplorer, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sortByHistory();
			}
		});
		button.setText("History");
		button.setImage(ResourceManager.getPluginImage("mdeml.datasetcreator", "icons/history.png"));
		
		modelSetTableViewer = new TableViewer(cmpExplorer, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table = modelSetTableViewer.getTable();
		table.setHeaderVisible(true);
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd_table.widthHint = 201;
		table.setLayoutData(gd_table);
		
		TableViewerColumn tableViewerColumn = new TableViewerColumn(modelSetTableViewer, SWT.NONE);
		TableColumn tblclmnName = tableViewerColumn.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");
		
//		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
//		TableColumn tblclmnCluster = tableViewerColumn_1.getColumn();
//		tblclmnCluster.setWidth(100);
//		tblclmnCluster.setText("Cluster");
		
		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(modelSetTableViewer, SWT.NONE);
		TableColumn tblclmnMetadata = tableViewerColumn_2.getColumn();
		tblclmnMetadata.setWidth(100);
		tblclmnMetadata.setText("Metadata");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(modelSetTableViewer, SWT.NONE);
		TableColumn tblclmnClusters = tableViewerColumn_1.getColumn();
		tblclmnClusters.setWidth(100);
		tblclmnClusters.setText("Clusters");
		
		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(modelSetTableViewer, SWT.NONE);
		TableColumn tblclmnFileName = tableViewerColumn_3.getColumn();
		tblclmnFileName.setWidth(100);
		tblclmnFileName.setText("File");
		
		initCmpRelated(sashForm_3);
		
		Composite cmdEditor = new Composite(sashForm, SWT.NONE);
		cmdEditor.setLayout(new GridLayout(1, false));
		
		SashForm sashForm_1 = new SashForm(cmdEditor, SWT.NONE);
		sashForm_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		modelView = new ModelViewComposite(sashForm_1, SWT.NONE, this);
		
		Composite composite = new Composite(sashForm_1, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		SashForm sashForm_2 = new SashForm(composite, SWT.VERTICAL);
		sashForm_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashForm_2.setSashWidth(4);
		
		Composite cmpOutline = new Composite(sashForm_2, SWT.NONE);
		cmpOutline.setLayout(new GridLayout(1, false));
		
		cmbModelTypes = new Combo(cmpOutline, SWT.NONE);
		cmbModelTypes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbModelTypes.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String item = cmbModelTypes.getItem(cmbModelTypes.getSelectionIndex());
				IDatasetCreatorExtensionHandler h = (IDatasetCreatorExtensionHandler) cmbModelTypes.getData(item);
				selectModelType(h);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
		});
		
		tableViewerOutline = new TableViewer(cmpOutline, SWT.BORDER | SWT.FULL_SELECTION);
		tabOutline = tableViewerOutline.getTable();
		tabOutline.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				selectFromOutline(tableViewerOutline);
			}
		});
		tabOutline.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite cmpOutlineExtension = new Composite(sashForm_2, SWT.NONE);
		cmpOutlineExtension.setLayout(new GridLayout(1, false));
		
		Combo combo = new Combo(cmpOutlineExtension, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tvProjectRelated = new TableViewer(cmpOutlineExtension, SWT.BORDER | SWT.FULL_SELECTION);
		Table table_1 = tvProjectRelated.getTable();
		tvProjectRelated.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					openModel((SwModel) ((IStructuredSelection) selection).getFirstElement());					
				}
			}
		});
		table_1.setHeaderVisible(true);
		table_1.setLinesVisible(true);
		table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableColumn tblclmnNewColumn = new TableColumn(table_1, SWT.NONE);
		tblclmnNewColumn.setWidth(100);
		tblclmnNewColumn.setText("Name");
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table_1, SWT.NONE);
		tblclmnNewColumn_1.setWidth(100);
		tblclmnNewColumn_1.setText("Information");
		sashForm_2.setWeights(new int[] {1, 1});
		sashForm_1.setWeights(new int[] {3, 2});
		sashForm.setWeights(new int[] {2, 3});

		manualInit();
	}

	protected void initCmpRelated(SashForm sashForm_3) {
		sashForm_3.setWeights(new int[] {2});
	}

	
	@NonNull
	private List<IModelLabellingListener> labellingListeners = new ArrayList<IModelLabellingListener>();
	
	public void addModelLabellingListener(@NonNull IModelLabellingListener listener) {
		labellingListeners.add(listener);
	}
	
	public void notifyLabelling(@NonNull SwModel model) {
		labellingListeners.forEach(l -> l.labelled(model));
	}
	
	public void notifyNextLabelling() {
		labellingListeners.forEach(l -> l.next());
	}
	
	@Nullable
	private IRelatedSearchProvider relatedSearchProvider;
	private SwModel currentSearch;
	private Link currentSearchLnk;

	public void setRelatedSearchProvider(IRelatedSearchProvider relatedSearchProvider) {
		this.relatedSearchProvider = relatedSearchProvider;
	}
	
	protected void swapRefinedSearch() {
		if (relatedSearchProvider != null) {
			AsyncResult result = relatedSearchProvider.popRelated();
			setCurrentSearch(result.getModel());
			setModelSetTableViewerInput(result);
		}
	}

	protected void setModelSetTableViewerInput(Map<SwModel, Double> scores) {
		this.modelSetTableViewer.resetFilters();
		ScoreComparator comparator = new ScoreComparator(scores);
		this.modelSetTableViewer.setInput(scores.keySet());
		this.modelSetTableViewer.setComparator(comparator);
		ScoreViewerFilter filter = new ScoreComparator.ScoreViewerFilter();			
		this.modelSetTableViewer.setFilters(filter);
	}
	
	protected void sortByHistory() {
		Map<SwModel, Event> events = controller.getEvents();

		this.modelSetTableViewer.resetFilters();
		EventComparator comparator = new EventComparator(events);
		this.modelSetTableViewer.setInput(events.keySet());
		this.modelSetTableViewer.setComparator(comparator);
		EventComparator.NoFilter filter = new EventComparator.NoFilter();			
		this.modelSetTableViewer.setFilters(filter);

	}
	
	protected void nextByDensity() {
		if (! controller.hasDensityList()) {
			MessageDialog.openError(null, "Error", "Add density file to configuration file");
			return;
		}
		
		try {
			List<? extends SwModel> sortedDensities = controller.getDensityList();
			SwModel selected = null;
			for (SwModel swModel : sortedDensities) {
				if (! swModel.isTagged()) {
					selected = swModel;
					break;
				}
			}
			
			if (selected == null) {
				MessageDialog.openInformation(null, "Info", "You are done! All labelled!");
				return;
			}

			setCurrentSearch(selected);
			filterBySearch(selected, new ScoreComparator.ScoreViewerFilter());		
			controller.registerEvent("click-next");

			// TODO: Notify through IModelLabellingListener interface
			notifyNextLabelling();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void setCurrentSearch(SwModel selected) {
		this.currentSearch = selected;
		String txt = getModelHumanName(selected) + " " + (selected.getMetadata() == null ? "" : selected.getMetadata());
		this.currentSearchLnk.setText("<a>" + txt.substring(0, Math.min(txt.length(), 100)) + "</a>");
	}

	protected void filterBySearch(SwModel selected, ViewerFilter filter) {
		ISearchService service = controller.getSearchService();
		File modelFile = selected.getFulllFile(controller);
		Resource r = modelViewSupport.loadModel(modelFile);
		SearchResult result = service.search(r, -1);
		Map<SwModel, Double> asMap = new HashMap<>();
		
		SwModel first = null;
		int idx = 0;
		System.out.println("Results: " + result.getItems().size());
		for (Item item : result.getItems()) {
			SwModel m = controller.getModel(item.getName());
			if (m == null) {
				System.out.println("Can't find " + item.getName());
			} else {
				asMap.put(m, item.getScore());
				if (first == null)
					first = m;
			}
			idx++;
			// Do all!
			//if (idx == 500)
			//	break;
		}
			
		// Just in case, make sure the obtained model is the first model
		if (first != selected) {
			System.out.println("Something weird happened. MAR didn't return the expected model as the first one: " + selected.getId());
			asMap.put(selected, Double.MAX_VALUE);
		}
		
		
		this.modelSetTableViewer.resetFilters();
		ScoreComparator comparator = new ScoreComparator(asMap);
		this.modelSetTableViewer.setInput(asMap.keySet());
		this.modelSetTableViewer.setComparator(comparator);
		this.modelSetTableViewer.setFilters(filter);
	}

	protected void applySearchFilter() {
		String text = txtSearch.getText();
        SearchFilter filter = SearchFilter.fromText(text, controller);
        filter.applyTo(modelSetTableViewer);
	}

	@Override
	public void save(String txtMetadata) {
		SwModel model = controller.getCurrentModel();
		if (model != null) {			
			try {
				AnnotationsValidator.INSTANCE.toMetadata(txtMetadata);
			} catch (SyntaxError e) {
				MessageDialog.openError(null, "Syntax error", e.getMessage());
			}
			
			model.setMetadata(txtMetadata);
			controller.saveModel(model);
			this.modelSetTableViewer.refresh(model);
			
			int count = 0;
			List<SwModel> models = controller.getModels();
			for (SwModel m : models) {
				if (m.isTagged()) {
					count++;
				}
			}
			
			String msg = String.format("Annotated %d models. %.2f", count, (count / (double) models.size()) * 100.0f) + "%";
			statusViewer.message(msg);
		}
		
		// We dont' find related for the first ones
		SwModel first = (SwModel) modelSetTableViewer.getElementAt(0);
		if (model.equals(first))
			return;
		
		notifyLabelling(model);		
	}
	
	public void nextItem() {
		//IStructuredSelection selection = (IStructuredSelection) this.modelSetTableViewer.getSelection();	
		//SwModel m = (SwModel) selection.getFirstElement();
		//this.modelSetTableViewer.setSelection(new StructuredSelection());
		
		Table tbl = modelSetTableViewer.getTable();
		int idx = tbl.getSelectionIndex();
		//TableItem[] sel = modelSetTableViewer.getTable().getSelection();
		if (idx >= 0 && idx < tbl.getItemCount()) {
			tbl.setSelection(idx + 1);
			IStructuredSelection selection = (IStructuredSelection) this.modelSetTableViewer.getSelection();	
			SwModel m = (SwModel) selection.getFirstElement();
			openModel(m);
		}		
	}

	protected void openModel(SwModel model) {
		Resource r = modelViewSupport.selectModel(model, modelView);
		if (r == null)
			return;
		
		//tableViewerOutline.setInput(null); // This raises an exception and has to moved to selectModelType
		
		applyExtensions(model, r);
	}

	private void applyExtensions(SwModel model, Resource r) {
		String extension = FilenameUtils.getExtension(model.getFile().getName());
		List<IDatasetCreatorExtensionHandler> handlers = ExtensionPointUtils.getHandlers(extension, r);
		cmbModelTypes.removeAll();
		
		for (IDatasetCreatorExtensionHandler h : handlers) {
			cmbModelTypes.add(h.getId());
			cmbModelTypes.setData(h.getId(), h);
		}
		
		if (handlers.size() > 0) {
			cmbModelTypes.select(0);
			handlers.get(0).prepareUI(new UpdatableUIElements(controller, modelView.getModelTreeViewer(), tvProjectRelated));
			selectModelType(handlers.get(0));
		}
		
		String datasetType = "github"; // TODO: as a parameter somehow
		List<IDatasetTypeHandler> datasetHandlers = ExtensionPointUtils.getDatasetExtensions(datasetType, model, r);
		if (datasetHandlers.size() > 0) {
			IDatasetTypeHandler handler = datasetHandlers.get(0);
			handler.prepareUI(new UpdatableUIElements(controller, modelView.getModelTreeViewer(), tvProjectRelated));
		}
	}

	protected void selectModelType(IDatasetCreatorExtensionHandler h) {
		IOutline outline = h.getOutline();
		tableViewerOutline.setContentProvider(outline.getContentProvider());
		tableViewerOutline.setInput(null);  // We reset the input, if not, setLabelprovider and setInput will raise an error
		tableViewerOutline.setLabelProvider(outline.getLabelProvider());
		tableViewerOutline.setInput(outline.getInput());

		modelView.updateVisualization(h);		
	}
	
	protected void selectFromOutline(@lombok.NonNull TableViewer tableViewer) {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		Object element = selection.getFirstElement();
		modelView.getModelTreeViewer().setSelection(new StructuredSelection(element), true);
	}

	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected void manualInit() {
		modelSetTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		modelSetTableViewer.setLabelProvider(new ModelLabelProvider());
		modelSetTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					openModel((SwModel) ((IStructuredSelection) selection).getFirstElement());					
				}
			}
		});
	
		initModelSetTableViewer();
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action("Annotate") {
			@Override
			public void run() {
				System.out.println("Copy file");
				IStructuredSelection selection = (IStructuredSelection) modelSetTableViewer.getSelection();
				EnterMetadataDialog d = new EnterMetadataDialog(getShell());
				if (d.open() == SWT.CANCEL)
					return;
				
				String data = d.getData();
				
				@SuppressWarnings("unchecked")
				Iterator<SwModel> it = selection.iterator();
				while (it.hasNext()) {
					SwModel model = it.next();
					model.setMetadata(data);
					controller.saveModel(model);
					modelSetTableViewer.refresh(model);
					// TODO: Refresh related
				}								
			}
		});		
		mgr.add(new Action("Restart labelling") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) modelSetTableViewer.getSelection();
				SwModel selected = (SwModel) selection.getFirstElement();
				setCurrentSearch(selected);
				filterBySearch(selected, new ScoreComparator.ScoreViewerFilter());		
				controller.registerEvent("restart-labelling");
			}
		});		
		mgr.add(new Action("Copy file") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) modelSetTableViewer.getSelection();
				selection.iterator();
				SwModel model = (SwModel) selection.getFirstElement();
				if (model != null) {
					File sourceFile = model.getFulllFile(controller);
					
					IFile target = project.getFile(sourceFile.getName());
					
					try {
						Files.copy(sourceFile.toPath(), new File(target.getLocation().toOSString()).toPath());
						target.refreshLocal(1, null);
					} catch (CoreException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});
		mgr.add(new Action("Export to CSV") {
			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) modelSetTableViewer.getSelection();
				Iterator<SwModel> it = selection.iterator();
				StringBuilder sb = new StringBuilder();
				while (it.hasNext()) {
					SwModel model = it.next();
					File sourceFile = model.getFile();
					sb.append(sourceFile.getPath() + ",\"" + model.getMetadata() + "\"" + "\n");
				}
								
				IFile target = project.getFile("list.csv");					
				try {
					if (!target.exists())
						target.create(new ByteArrayInputStream(sb.toString().getBytes()), true, null);
					else 
						target.setContents(new ByteArrayInputStream(sb.toString().getBytes()), IFile.FORCE, null);
					target.refreshLocal(1, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		modelSetTableViewer.getTable().setMenu(mgr.createContextMenu(modelSetTableViewer.getTable()));
		/*
		 * MenuManager mgr = new MenuManager();
		viewer.getTable().setMenu(mgr.createContextMenu(viewer.getTa ble()));
		 */
	}

	protected void initModelSetTableViewer() {
		modelSetTableViewer.setInput(controller.getModels());				
	}

	private class ModelLabelProvider implements ITableLabelProvider {

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
			SwModel m = (SwModel) element;
			switch (columnIndex) {
			case 0: 
				return getModelHumanName(m);
			case 1: return m.getMetadata();
			case 2: 
				return m.getClusters().stream().map(c -> c.getLabel()).collect(Collectors.joining(", "));
			case 3: return m.getFile().getAbsolutePath();
			default:
				return "<unknown>";
			}
		}
	}

	@Override
	public void updateRepositories(List<? extends Repository> repositories, @NonNull List<? extends ClusterFile> clusters) {
		// Do nothing
	}

	@Override
	public void updateExternalMetadata() {
		// This is to allow controller-defined filters to be applied.
		// TODO: We don't need to do this if we load the configuration file first!!
		this.modelSetTableViewer.setInput(controller.getModels());
		this.modelSetTableViewer.refresh(true);
	}

	@Override
	public DatasetCreatorController getController() {
		return controller;
	}
	
	private String getModelHumanName(SwModel m) {
		String name = m.getName();
		ModelData d = controller.getExternalMetadata(m);
		if (d != null) {
			return d.getName();
		}
		return name;
	}		
}
