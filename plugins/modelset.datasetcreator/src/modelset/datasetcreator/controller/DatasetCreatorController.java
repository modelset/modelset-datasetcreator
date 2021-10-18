package modelset.datasetcreator.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import modelset.common.annotations.AnnotationsModel;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.Categories;
import modelset.common.db.ClusterFile;
import modelset.common.db.DatasetDb;
import modelset.common.db.Event;
import modelset.common.db.ExternalMetadata;
import modelset.common.db.IDatasetOrigin;
import modelset.common.db.IRepositoryProvider;
import modelset.common.db.Repository;
import modelset.common.db.SwModel;
import modelset.common.db.ExternalMetadata.IExternalMetadataProvider;
import modelset.common.db.ExternalMetadata.ModelData;
import modelset.common.export.DatasetExporter;
import modelset.common.export.DatasetStats;
import modelset.common.services.ISearchService;
import modelset.common.services.MarSearchService;
import modelset.common.services.WhooshSearchService;
import modelset.datasetcreator.extensions.filters.IFilter;
import modelset.datasetcreator.extensions.filters.NameFilter;
import modelset.search.lucene.LuceneSearchService;

public class DatasetCreatorController implements IRepositoryProvider, IExternalMetadataProvider {
	
	@NonNull 
	private final List<Repository> repositories = new ArrayList<>();
	@NonNull 
	private final List<ClusterFile> clusters = new ArrayList<>();
	@NonNull 
	private File dbFile;
	private DatasetDb db;
	@NonNull
	private final Set<ControllerChangeListener> listeners;
	
	@Nullable
	private SwModel currentModel;
	@NonNull
	private SearchEngineKind searchKind = SearchEngineKind.MAR;
	@NonNull
	private String searchType = "ecore";
	@NonNull
	private String searchURL = "localhost";
	private int searchPort = 8080;
	@Nullable
	private Map<String, String> densityFiles = new HashMap<String, String>();
	@NonNull
	private Map<String, Categories> categories = new HashMap<String, Categories>();
	@NonNull
	private Map<String, ExternalMetadata> externalMetadata = new HashMap<String, ExternalMetadata>();
	@Nullable
	private List<SwModel> densities = null;
	@Nullable
	private IDatasetOrigin origin;
	@NonNull
	private List<IFilter> filters = new ArrayList<>();

	private static enum SearchEngineKind {
		MAR,
		WHOOSH,
		LUCENE,		
	}
	
	public DatasetCreatorController(@NonNull File db) {
		this.db = new DatasetDb(db);
		this.listeners = new HashSet<ControllerChangeListener>();
	}
	
	public DatasetCreatorController(@NonNull File db, @NonNull File dsc) throws IOException, InvalidFormat {
		this(db);
		String dscContents = FileUtils.readFileToString(dsc, "UTF-8");
		processRepositories(dscContents, new File(dsc.getParent()));
	}
	
	public void importRepositories() {
		for(Repository r : repositories) {
			try {
				importFileSet(db, r.getName(), r.getFileSet());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void importFileSet(@NonNull DatasetDb db, @NonNull String repo, @NonNull String fileSet) throws IOException {
		 for (CSVRecord record : CSVFormat.DEFAULT.parse(new FileReader(fileSet))) {
			 String fileName = record.get(0);
			 db.insertModel(repo, fileName);
		     //for (String field : record) {
		         
		     //}
		     //System.out.println();
		 }
	}

	public void importClusters() {
		for (ClusterFile clusterFile : clusters) {
			try {
				importClusterFile(db, clusterFile.getRepoName(), clusterFile.getClustersFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	
	private void importClusterFile(@NonNull DatasetDb db, @NonNull String repoName, @NonNull String clustersFile) throws IOException {
		for (CSVRecord record : CSVFormat.DEFAULT.parse(new FileReader(clustersFile))) {
			 String fileName = record.get(0);
			 int cluster = Integer.parseInt(record.get(1));
			 db.insertCluster(repoName, fileName, cluster);
		 }		
	}
	
	public SwModel selectModel(SwModel model) {
		this.currentModel = model;
		return currentModel;
	}
		
	@NonNull
	public List<? extends Repository> getRepositories() {
		return repositories;
	}

	@NonNull
	public List<? extends ClusterFile> getClusters() {
		return clusters;
	}
	
	public List<SwModel> getModels() {
		return filter(db.getModels());
	}
	
	public Collection<? extends SwModel> getAllModels() {
		return db.getModels();
	}
	
	
	private List<SwModel> filter(@NonNull Collection<? extends SwModel> models) {
		if (filters.isEmpty())
			return new ArrayList<>(models);
		List<SwModel> results = new ArrayList<SwModel>();
		MODEL:
		for (SwModel swModel : models) {
			for (IFilter f : filters) {
				if (! f.select(swModel))
					continue MODEL;
			}
			results.add(swModel);
		}
		return results;
	}

	public void processRepositories(String str, File folder) throws InvalidFormat {
		boolean userMatched = false;
		String username = System.getProperty("user.name");
		BufferedReader reader = new BufferedReader(new StringReader(str));
		try {
			repositories.clear();
			clusters.clear();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				
				String[] parts = line.split(",");
				if (parts.length < 3) {
					throw new InvalidFormat("Format is profile,(repo|cluster)...");
				}
				
				String user = parts[0];
				if (!"username".equals(user) && !user.equals(username))
					continue;
				userMatched = true;
				
				String type = parts[1];
				if (type.equalsIgnoreCase("repo")) {
					if (parts.length != 5) {
						throw new InvalidFormat("Format is (repo|cluster)...");
					}
					String name = parts[2];
					String root = toPath(parts[3], folder);
					String fileSet = toPath(parts[4], folder);
					Repository repo = new Repository(name, root, fileSet, null);				
					repositories.add(repo);
				} else if (type.equalsIgnoreCase("txt")) {
					if (parts.length != 4) {
						throw new InvalidFormat("Format is profile, txt, reponame...");
					}
					String name = parts[2];
					String root = toPath(parts[3], folder);
					boolean found = false;
					for (Repository repository : repositories) {
						if (repository.getName().equals(name)) {
							repository.setTxtRoot(root);
							found = true;
							break;
						}
					}
					if (!found)
						throw new InvalidFormat("No repository: " + name);
				} else if (type.equalsIgnoreCase("clusters") || type.equalsIgnoreCase("cluster")) {
					if (parts.length != 4) {
						throw new InvalidFormat("Format is profile, (repo|cluster)...");
					}
					
					String name = parts[2];
					String clustersFile = toPath(parts[3], folder);
					clusters.add(new ClusterFile(name, clustersFile));
				} else if (type.equalsIgnoreCase("mar-url") || type.contentEquals("whoosh-url")) {
					if (parts.length != 5) {
						throw new InvalidFormat("Format is profile, mar-url|whoosh-url, model-type, url, port ");
					}

					this.searchKind = type.equalsIgnoreCase("mar-url") ? SearchEngineKind.MAR : SearchEngineKind.WHOOSH;
					this.searchType = parts[2];
					this.searchURL = parts[3];
					this.searchPort = Integer.parseInt(parts[4]);
				} else if (type.equalsIgnoreCase("lucene-index")) {
					this.searchKind = SearchEngineKind.LUCENE;
					this.searchType = parts[2];
					this.searchURL = toPath(parts[3], folder);
				} else if (type.equalsIgnoreCase("density")) {
					if (parts.length != 4) {
						throw new InvalidFormat("Format is profile, (repo|cluster), repoId, density_file ");
					}

					String repoid = parts[2];
					String densityFile = toPath(parts[3], folder);
					this.densityFiles.put(repoid, densityFile);
				} else if (type.equalsIgnoreCase("metadata")) {
					if (parts.length != 4) {
						throw new InvalidFormat("Format is profile, (repo|cluster), repoId, density_file ");
					}

					String repoid = parts[2];
					String metadataFile = toPath(parts[3], folder);
					this.externalMetadata.put(repoid, ExternalMetadata.loadFromFile(metadataFile, repoid));					
				} else if (type.equalsIgnoreCase("categories")) {
					String repoid = parts[2];
					String categories = toPath(parts[3], folder);
					this.categories.put(repoid, Categories.fromFile(categories, repoid));
				} else if (type.equalsIgnoreCase("origin")) {
					// String repoid = parts[2];
					String originType = parts[3];
					if (originType.equals("github")) {
						this.origin = new IDatasetOrigin.GithubDataset();
					} else if (originType.equals("genmymodel")){
						this.origin = new IDatasetOrigin.GenMyModelDataset();						
					}
				} else if (type.equalsIgnoreCase("filter")) {
					String repoid = parts[2];
					String name = parts[3];
					if (name.equals("name-filter")) {
						String filterList = toPath(parts[4], folder);
						List<String> files = IOUtils.readLines(new FileInputStream(filterList), "UTF-8");
						this.filters.add(new NameFilter(repoid, files));						
					}					
				} else {
					throw new InvalidFormat("Expected repo or clusters");
				}
			}
			
			if (! userMatched) {
				throw new InvalidFormat("No current user " + username + " matched");
			}
			
			this.listeners.forEach(l -> l.updateRepositories(repositories, clusters));
			if (! externalMetadata.isEmpty() || !filters.isEmpty()) {
				this.listeners.forEach(l -> l.updateExternalMetadata());
			}				
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String toPath(String path, File folder) {
		return path.replaceAll("\\$FOLDER", folder.getPath());
	}

	public void addChangeListener(@NonNull ControllerChangeListener listener) {
		this.listeners.add(listener);		
	}

	@SuppressWarnings("serial")
	public static class InvalidFormat extends Exception {

		public InvalidFormat(String string) {
			super(string);
		}
		
	}
	
	public static interface ControllerChangeListener {
		public void updateRepositories(List<? extends Repository> repositories, @NonNull List<? extends ClusterFile> clusters);
		public void updateExternalMetadata();
	}

	@Nullable
	public String getRootFolder(@NonNull String repo) {
		for (Repository repository : repositories) {
			if (repository.getName().equals(repo)) {
				return repository.getRoot();
			}
		}
		return null;
	}

	@Nullable
	public SwModel getCurrentModel() {
		return this.currentModel;
	}

	public void saveModel(SwModel model) {
		db.updateModel(model);
	}

	public DatasetDb getDb() {
		return db;
	}
	
	@Override
	public @Nullable Repository getRepository(@NonNull String repoId) {
		for (Repository repository : repositories) {
			if (repository.getName().equals(repoId)) {
				return repository;
			}
		}
		return null;
	}

	@Override
	public IDatasetOrigin getOrigin() {
		return this.origin;
	}
	
	@NonNull
	public Map<SwModel, Event> getEvents() {
		return db.getEvents();
		/*
		Map<SwModel, Event> events = null;
		for (Repository repository : repositories) {
			if (events == null) {
				events = db.getEvents(repository.getName());
			} else {
				events.putAll(db.getEvents(repository.getName()));
			}
		}
		return events;
		*/
	}

	
	public boolean hasDensityList() {
		return ! densityFiles.isEmpty();
	}
	
	@NonNull
	public List<? extends SwModel> getDensityList() throws IOException {
		if (densities == null) {
			densities = new ArrayList<SwModel>();
			for (Entry<String, String> entry : densityFiles.entrySet()) {
				String repoid = entry.getKey();
				String file = entry.getValue();
				List<String> lines = IOUtils.readLines(new FileInputStream(file), "UTF-8");
				for (String name : lines) {
					SwModel model = db.getModel(repoid, name);
					if (model != null) {
						densities.add(model);
					} else {
						System.out.println("Not found: " + name);
					}
				}
			}
		}
		return filter(densities);
	}

	@NonNull
	public ISearchService getSearchService() {
		switch(searchKind) {
		case MAR:
			return new MarSearchService(searchURL, searchPort, searchType);
		case WHOOSH:
			return new WhooshSearchService(searchURL, searchPort, searchType);
		case LUCENE:
			return new LuceneSearchService(searchURL);
		}
		throw new IllegalStateException();
	}

	public SwModel getModel(String name) {
		for (Repository repo : repositories) {
			SwModel m = db.getModel(repo.getName(), name);
			if (m != null)
				return m;
		}
		return null;
	}

	public void export() throws IOException, SyntaxError {
		DatasetExporter.INSTANCE.export(db, "/tmp");
		DatasetExporter.INSTANCE.export(this, db, new File("/tmp/repo.json"));
	}
	
	public void stats() throws IOException {
		DatasetStats.INSTANCE.stats(this, db, "/tmp");
	}


	public void registerEvent(String string) {
		db.registerEvent(string, null);
	}

	@NonNull
	public Set<String> getCategories(@NonNull SwModel m) {
		Set<String> result = new HashSet<String>();
		categories.forEach((repoId, c) -> {
			result.addAll(c.getCategories(m));
		});
		return result;
	}
	
	@Override
	@Nullable
	public ModelData getExternalMetadata(@NonNull SwModel m) {
		for (Entry<String, ExternalMetadata> meta : externalMetadata.entrySet()) {
			ModelData d = meta.getValue().getData(m.getId());
			if (d != null)
				return d;
		}
		return null;
	}

	@NonNull
	public AnnotationsModel getAnnotationModel() {
		return new AnnotationsModel(db.getModels());
	}
	
	@Override
	@Nullable
	public String getURL(@NonNull SwModel model) {
		if (origin == null)
			return null;
		
		return origin.getURL(model.getId());
	}
	
	@Override
	public @Nullable String getFullURL(@NonNull SwModel model) {
		if (origin == null)
			return null;

		return origin.getFullURL(model.getId());
	}
	
	@NonNull
	public List<? extends IFilter> getFilters() {
		return filters;
	}

}
