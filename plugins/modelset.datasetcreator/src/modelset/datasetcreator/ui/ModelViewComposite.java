package modelset.datasetcreator.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.extensions.IDatasetCreatorExtensionHandler;
import modelset.datasetcreator.visualizations.IVisualizer;

public class ModelViewComposite extends Composite {

	private TreeViewer modelTreeViewer;
	private Browser browser;
	
	private int currentVisualization = 0;
	private List<File> visualizations;
	private Link lnkURL;
	private Label lblVizSize;
	private Text txtTags;
	private Text txtFile;
	private IDatasetView datasetView;
	
	public ModelViewComposite(Composite parent, int style, IDatasetView datasetView) {
		super(parent, style);
		this.datasetView = datasetView;
				
		this.setLayout(new GridLayout(1, false));
		
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmTree = new TabItem(tabFolder, SWT.NONE);
		tbtmTree.setText("Tree");
		
		modelTreeViewer = new TreeViewer(tabFolder, SWT.BORDER);
		Tree tvModel = modelTreeViewer.getTree();
		tbtmTree.setControl(tvModel);
		
		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("Visualization");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmNewItem.setControl(composite);
		composite.setLayout(new GridLayout(4, false));
		
		Text txtPath = new Text(composite, SWT.NONE);
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPath.setText("<path>");
		
		lblVizSize = new Label(composite, SWT.NONE);
		lblVizSize.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVizSize.setText("#Viz");
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentVisualization--;
				if (currentVisualization >= 0) {
					browser.setUrl(visualizations.get(currentVisualization).getAbsolutePath());
					txtPath.setText(browser.getUrl());
				} else {
					currentVisualization++;
				}
			}
		});
		btnNewButton.setText("Prev");
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentVisualization++;
				if (currentVisualization < visualizations.size()) {
					browser.setUrl(visualizations.get(currentVisualization).getAbsolutePath());
					txtPath.setText(browser.getUrl());
				} else {
					currentVisualization--;
				}
			}
		});
		btnNewButton_1.setText("Next");
		
		browser = new Browser(composite, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 4, 1));
		
		Composite composite_2 = new Composite(this, SWT.NONE);
		composite_2.setLayout(new GridLayout(3, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("File:");
		new Label(composite_2, SWT.NONE);
		
		txtFile = new Text(composite_2, SWT.BORDER);
		txtFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblURL = new Label(composite_2, SWT.NONE);
		lblURL.setText("URL:");
		lblURL.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		new Label(composite_2, SWT.NONE);
		
		lnkURL = new Link(composite_2, SWT.NONE);
		lnkURL.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lnkURL.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String url = (String) lnkURL.getData();
				if (url != null && !url.isEmpty())
					Program.launch(url);
			}
		});
		
		Label lblNewLabel = new Label(composite_2, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Labels:");
		new Label(composite_2, SWT.NONE);
		
		txtTags = new Text(composite_2, SWT.BORDER);
		txtTags.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR || e.character == SWT.LF)
					datasetView.save(txtTags.getText());
				// CTRL + s
				if ((e.stateMask & SWT.CTRL) != 0 && (e.character == 19)) {
					datasetView.save(txtTags.getText());	
					datasetView.nextItem();
				}
			}
		});
		txtTags.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtTags.setBounds(0, 0, 75, 30);
	
		manualInit();
	}
	
	private void manualInit() {
		UIUtils.configureModelViewer(modelTreeViewer);	
	}

	public void openModel(Resource r, File f, SwModel model) {
		DatasetCreatorController controller = datasetView.getController();
		UIUtils.update(r, modelTreeViewer, null);
		txtFile.setText(f.getAbsolutePath());
		
		if (model.getMetadata() != null) {
			txtTags.setText(model.getMetadata());
		} else {
			txtTags.setText("");
		}
		
		String url = controller.getURL(model);
		if (url != null) {
			lnkURL.setData(url);
			lnkURL.setText("<a href=\"" + url + "\">" + url + "</a>");
		}		
	}
	
	// TODO: Think about a cleaner interface for this
	public TreeViewer getModelTreeViewer() {
		return modelTreeViewer;
	}
	
	public void updateVisualization(IDatasetCreatorExtensionHandler h) {
		// Loads visualizations
		this.currentVisualization = 0;
		this.visualizations = new ArrayList<File>();

		File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "ds_viz");
		if (!tmp.exists())
			tmp.mkdir();
		
		List<IVisualizer> visualizers = h.getVisualizer();
		for (IVisualizer visualizer : visualizers) {			
			// This is not nice, but if everything is ok the tree viewer has the current resource
			Resource r = (Resource) modelTreeViewer.getInput();		
			this.visualizations.addAll(visualizer.toImage(r, tmp));			
		}
		
		System.out.println(visualizations);
		
		lblVizSize.setText(String.valueOf(this.visualizations.size()));
		if (visualizations.size() > 0)
			browser.setUrl(visualizations.get(0).getAbsolutePath());
	}
}
