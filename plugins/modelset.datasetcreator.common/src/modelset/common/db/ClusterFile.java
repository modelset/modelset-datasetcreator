package modelset.common.db;

public class ClusterFile {
	private String repoName;
	private String clustersFile;
	
	public ClusterFile(String repoName, String clustersFile) {
		this.repoName = repoName;
		this.clustersFile = clustersFile;
	}
	
	public String getClustersFile() {
		return clustersFile;
	}
	
	public String getRepoName() {
		return repoName;
	}
}

