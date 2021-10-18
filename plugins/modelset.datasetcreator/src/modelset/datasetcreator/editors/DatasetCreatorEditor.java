package modelset.datasetcreator.editors;


import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.InvalidFormat;
import modelset.datasetcreator.ui.IStatusViewer;
import modelset.datasetcreator.ui.MainComposite;
import modelset.datasetcreator.ui.RelatedComposite;
import modelset.datasetcreator.ui.RepositoriesComposite;
import modelset.datasetcreator.ui.ReviewComposite;
import modelset.datasetcreator.ui.SearchComposite;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class DatasetCreatorEditor extends MultiPageEditorPart implements IResourceChangeListener{

	/** The text editor used in page 0. */
	private TextEditor editor;

	/** The font chosen in page 1. */
	private Font font;

	/** The text widget used in page 2. */
	private StyledText text;

	@NonNull
	private DatasetCreatorController controller;

	private ReviewComposite sessionComposite;

	private IProject project;
	
	/**
	 * Creates a multi-page editor example.
	 */
	public DatasetCreatorEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 * @return 
	 */
	MainComposite createPage0() {
		MainComposite composite = new MainComposite(getContainer(), SWT.NONE, controller, project, new EclipseStatusViewer(this));
		int index = addPage(composite);
		setPageText(index, "Browser");
		/*
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
		*/
		return composite;
	}
	
	private int pageIdxRelated = -1;
	
	RelatedComposite createPage_Related() {
		RelatedComposite composite = new RelatedComposite(getContainer(), controller, project, new EclipseStatusViewer(this));
		pageIdxRelated = addPage(composite);
		setPageText(pageIdxRelated, "Related");
		return composite;
	}
	
	void createPage_Search() {
		SearchComposite composite = new SearchComposite(getContainer(), SWT.NONE, controller, project, new EclipseStatusViewer(this));
		int index = addPage(composite);
		setPageText(index, "Search");
	}

	void createPage_Review() {
		sessionComposite = new ReviewComposite(getContainer(), SWT.NONE, controller);
		int index = addPage(sessionComposite);
		setPageText(index, "Review");
	}
	
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage_Repositories() {
		RepositoriesComposite composite = new RepositoriesComposite(getContainer(), SWT.NONE, controller);
		int index = addPage(composite);
		setPageText(index, "Repositories");
		controller.addChangeListener(composite);
	}
	
	/**
	 * Creates page 2 of the multi-page editor,
	 * which shows the sorted text.
	 */
	void createPage_Configuration() {
		editor = new TextEditor();
		try {
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			e.printStackTrace();
		}		
		
		/*
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);		
		text.setEditable(false);

		int index = addPage(composite);
		setPageText(index, "Preview");
		*/
	}
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		MainComposite main = createPage0();
		RelatedComposite related = createPage_Related();
		main.addModelLabellingListener(related);
		main.setRelatedSearchProvider(related);
		
		createPage_Search();
		createPage_Repositories();
		createPage_Review();
		createPage_Configuration();
		updateRepositories();
	}
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		IEditorPart ed = getEditor(5);
		updateRepositories();
		ed.doSave(monitor);
	}
	
	private void updateRepositories() {
		ITextEditor ed = (ITextEditor) getEditor(5);
		IDocument doc = ed.getDocumentProvider().getDocument(editor.getEditorInput());		
		try {
			IFileEditorInput p = (IFileEditorInput) ed.getEditorInput();
			IFolder f = (IFolder) p.getFile().getParent();
					
			controller.processRepositories(doc.get(), new File(f.getLocation().toOSString()));
		} catch (InvalidFormat e) {
			MessageDialog.openError(getSite().getShell(), "Error", "Invalid file format: " + e.getMessage());
		}
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
		
		IFileEditorInput fin = (IFileEditorInput) editorInput;
		File dbFile = new File(fin.getFile().getLocation().removeFileExtension().addFileExtension("db").toOSString());
		this.project = fin.getFile().getProject();		
		this.controller = new DatasetCreatorController(dbFile); 		
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(() -> {
				IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (int i = 0; i<pages.length; i++){
					if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
						IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
						pages[i].closeEditor(editorPart,true);
					}
				}
			});
		}
	}

	public class EclipseStatusViewer implements IStatusViewer {

		private DatasetCreatorEditor editor;

		public EclipseStatusViewer(DatasetCreatorEditor editor) {
			this.editor = editor;
		}
		
		@Override
		public void message(String txt) {
			IActionBars bar = editor.getEditorSite().getActionBars();
			bar.getStatusLineManager().setMessage(txt);
		}
		
		@Override
		public void relatedUpdated(int size) {
			if (size == 0)
				setPageText(pageIdxRelated, "Related");
			else
				setPageText(pageIdxRelated, "Related (" + size + ")");
		}
	}
}
