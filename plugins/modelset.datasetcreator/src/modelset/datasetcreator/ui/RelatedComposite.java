package modelset.datasetcreator.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import modelset.common.db.SwModel;
import modelset.datasetcreator.algorithms.AsyncSearcher;
import modelset.datasetcreator.algorithms.AsyncSearcher.AsyncResult;
import modelset.datasetcreator.controller.DatasetCreatorController;

public class RelatedComposite extends Composite implements IModelLabellingListener, IRelatedSearchProvider {

	private @NonNull DatasetCreatorController controller;
	private @NonNull IProject project;

	@NonNull
	private AsyncSearcher asyncSearcher;

	private TabFolder tabFolder;
	private IStatusViewer statusViewer;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RelatedComposite(Composite parent, @NonNull DatasetCreatorController controller, IProject project, IStatusViewer statusViewer) {
		super(parent, SWT.NONE);
		this.controller = controller;
		this.project = project;
		this.statusViewer = statusViewer;
		
		this.asyncSearcher = new AsyncSearcher(new ModelViewSupport(controller), this::updateRelated);
		
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		tabFolder = new TabFolder(this, SWT.NONE);
		
	}

	private Set<SwModel> seenResults = new HashSet<>();
	
	public void updateRelated(@NonNull AsyncResult r) {	
		Display.getDefault().syncExec(() -> {
			Map<SwModel, Double> results = new HashMap<>();
			r.forEach((k, v) -> {
				if (! seenResults.contains(k)) {
					seenResults.add(k);
					results.put(k, v);
				}
			});					

			if (results.size() > 0) {
				TabItem item = new TabItem(tabFolder, SWT.NONE);
				AnnotationComposite cmp = new AnnotationComposite(tabFolder, controller, project, statusViewer);
	//			cmp.addListener(SWT.Show, new Listener() {				
	//				@Override
	//				public void handleEvent(Event event) {
	//					cmp.setModelSetTableViewerInput(r);
	//					cmp.removeListener(SWT.Show, this);
	//				}
	//			});
				
				tabFolder.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						cmp.setModelSetTableViewerInput(results);
						tabFolder.removeSelectionListener(this);
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) { 
						System.out.println(e);
					}
				});
				
				item.setControl(cmp);
				item.setData(r);
				
				String[] parts = r.getModel().getName().split("/");		
				item.setText(parts[parts.length - 1]);			
	
				statusViewer.relatedUpdated(this.asyncSearcher.size());
			}
		});
	}
	
	public static class AnnotationComposite extends MainComposite {

		private @NonNull AsyncResult result;

		public AnnotationComposite(Composite parent, @NonNull DatasetCreatorController controller,
				@NonNull IProject project, @NonNull IStatusViewer statusViewer) {
			super(parent, SWT.NONE, controller, project, statusViewer);
		}
		
		@Override
		protected void initModelSetTableViewer() {
			// Do not initialize yet, do it lazily
		}
		
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void labelled(@NonNull SwModel model) {
		for (TabItem item : tabFolder.getItems()) {
			AsyncResult r = (AsyncResult) item.getData();
			if (r.getModel() == model)
				return;
		}
	
		this.asyncSearcher.addStreak(model);
	}

	@Override
	public void next() {
		this.asyncSearcher.stop();
		for (TabItem tabItem : this.tabFolder.getItems()) {
			closeItem(tabItem);
		}
		seenResults.clear();
		statusViewer.relatedUpdated(0);
	}

	private void closeItem(TabItem tabItem) {
		tabItem.getControl().dispose();
		tabItem.dispose();
	}

	@Override
	public AsyncResult popRelated() {
		AsyncResult result = this.asyncSearcher.pop();
		for (TabItem tabItem : this.tabFolder.getItems()) {
			if (tabItem.getData() == result) {
				closeItem(tabItem);
			}
		}
		statusViewer.relatedUpdated(this.asyncSearcher.size());
		return result;
	}
	
	@Override
	public int getRelatedSize() {
		return this.asyncSearcher.size();
	}
	
//	
//	public void updateRelated(Map<SwModel, Double> scores) {
//		if (tableViewerRelatedSearches != null) {
//			Display.getDefault().syncExec(() -> {
//				// this.modelSetTableViewer.resetFilters();
//				ScoreComparator comparator = new ScoreComparator(scores);
//				this.tableViewerRelatedSearches.setInput(scores.keySet());
//				this.tableViewerRelatedSearches.setComparator(comparator);
//				this.tableViewerRelatedSearches.refresh();
//			});
//		}
//	}
//	

}
