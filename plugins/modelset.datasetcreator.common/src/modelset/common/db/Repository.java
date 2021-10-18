package modelset.common.db;

public class Repository {
	private String name;
	private String root;
	private String fileSet;
	
	private String txtRoot;
	
	public Repository(String name, String root, String fileSet, String txtRoot) {
		this.name = name;
		this.root = root;
		this.fileSet = fileSet;
		this.txtRoot = txtRoot;
	}

	public String getName() {
		return name;
	}
	
	public String getRoot() {
		return root;
	}
	
	public String getTxtRoot() {
		return txtRoot;
	}
	
	public void setTxtRoot(String txtRoot) {
		this.txtRoot = txtRoot;
	}
	
	public String getFileSet() {
		return fileSet;
	}	
	
}
