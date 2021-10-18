package modelset.common.db;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.ParsedMetadata;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;

public class SwModel {
	private final String id;
	private String repo;
	private String cluster;
	private String metadata;
	private File file;

	private List<Cluster> clusters = new ArrayList<>();
	
	public List<Cluster> getClusters() {
		return clusters;
	}
	
	public SwModel(String id) {
		this.id = id;
	}

	public String getName() {
		return file.getName();
	}	
	
	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getId() {
		return id;
	}

	public void setClusters(List<Cluster> clusters) {
		this.clusters = clusters;
	}

	@NonNull
	public File getFulllFile(@NonNull IRepositoryProvider repos) {
		Repository repo = repos.getRepository(this.repo);
		return Paths.get(repo.getRoot(), file.getPath()).toFile();
	}

	public void addCluster(@NonNull Cluster cluster) {
		clusters.add(cluster);
	}

	public boolean isTagged() {
		return metadata != null && !metadata.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((repo == null) ? 0 : repo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SwModel other = (SwModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (repo == null) {
			if (other.repo != null)
				return false;
		} else if (!repo.equals(other.repo))
			return false;
		return true;
	}		
	
	public static String modelId(String repo, String fileName) {
		return repo + "/" + fileName;
	}

	public static final ByNameComparator ByNameComparator = new ByNameComparator();
	public static final SwModel[] EMPTY = new SwModel[0];
	
	public static class ByNameComparator implements Comparator<SwModel> {

		@Override
		public int compare(SwModel o1, SwModel o2) {
			return o1.getId().compareTo(o2.getId());
			// return o1.getName().compareTo(o2.getName());
		}
		
	}

	public ParsedMetadata toMetadata() throws SyntaxError {
		if (metadata == null || metadata.trim().isEmpty()) 
			return new ParsedMetadata(new HashMap<String, List<String>>());
		return AnnotationsValidator.INSTANCE.toMetadata(metadata);
	}
	
}
