package modelset.common.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.ParsedMetadata;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;

public class DatasetDb {

	@NonNull
	private Connection connection;

	@NonNull
	private Map<String, SwModel> modelsCache = null; 
	
	public DatasetDb(File file) {					
		String url = "jdbc:sqlite:" + file.getAbsolutePath();
		 
        try {
        	Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
            	if (! file.exists()) {
	                DatabaseMetaData meta = conn.getMetaData();
	                System.out.println("The driver name is " + meta.getDriverName());
	                System.out.println("A new database has been created.");
            	}
            	
                String models = "CREATE TABLE IF NOT EXISTS models (\n"
                        + "    id varchar(255) PRIMARY KEY,\n"
                        + "    repo varchar(255) NOT NULL,\n"  // FK (repositories)
                        + "    filename text NOT NULL\n"
                        + ");";

                String metadata  = "CREATE TABLE IF NOT EXISTS metadata (\n"
                        + "    id varchar(255) PRIMARY KEY,\n"
                        + "    metadata text NOT NULL\n"  // FK (models)
                        + "    json text NOT NULL\n"
                        + ");";

                String repositories = "CREATE TABLE IF NOT EXISTS repositories (\n"
                        + "    id varchar(255) PRIMARY KEY,\n"
                        + "    folder varchar(255) NOT NULL\n"
                        + ");";

                String clusters = "CREATE TABLE IF NOT EXISTS clusters (\n"
                        + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + "    id_in_csv INTEGER,\n"
                        + "    repo_name varchar(255) NOT NULL,\n"
                        + "    human_name varchar(255)\n"
                        + ");";

                String clusters_models= "CREATE TABLE IF NOT EXISTS clusters_models (\n"
                        + "    cluster_id varchar(255) NOT NULL,\n"
                        + "    model_name varchar(255) NOT NULL,"
                        + "    PRIMARY KEY (cluster_id, model_name)\n"
                        + ");";

                String events = "CREATE TABLE IF NOT EXISTS events (\n"
                        + "    start_time INTEGER NOT NULL,\n"
                        + "    type VARCHAR(255) NOT NULL,\n"
                        + "    model_id VARCHAR(255),\n"
                        + "    PRIMARY KEY (start_time)\n"
                        + ");";

                Statement stmt = conn.createStatement();
                stmt.execute(models);
                
                stmt = conn.createStatement();
                stmt.execute(metadata);

                stmt = conn.createStatement();
                stmt.execute(repositories);

                stmt = conn.createStatement();
                stmt.execute(clusters);

                stmt = conn.createStatement();
                stmt.execute(clusters_models);

                stmt = conn.createStatement();
                stmt.execute(events);
            }
            this.connection = conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
	}	
	
	public Collection<? extends SwModel> getModels() {
		if (modelsCache != null) {
			return modelsCache.values();
		}
		modelsCache = new HashMap<String, SwModel>();
		
		try {			
			
			String s = "SELECT models.id as mid, repo, filename, metadata, "
					+ "C.id as cluster_id, C.human_name "
					+ "FROM models "
					+ "LEFT JOIN metadata ON models.id = metadata.id "
					+ "LEFT JOIN clusters_models AS CM ON models.id = CM.model_name "
					+ "LEFT JOIN clusters AS C ON C.id = CM.cluster_id and CM.model_name = models.id";
			
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT models.id as mid, repo, filename, metadata, "
					+ "C.id as cluster_id, C.human_name "
					+ "FROM models "
					+ "LEFT JOIN metadata ON models.id = metadata.id "
					+ "LEFT JOIN clusters_models AS CM ON models.id = CM.model_name "
					+ "LEFT JOIN clusters AS C ON C.id = CM.cluster_id and CM.model_name = models.id");
			preparedStatement.execute();
			ResultSet rs = preparedStatement.getResultSet();
			Map<String, SwModel> processed = new HashMap<>();
			List<SwModel> models = new ArrayList<SwModel>();
			while (rs.next()) {
				String id = rs.getString(1);
				SwModel model = processed.get(id);
				if (model == null) {
					model = new SwModel(id);
					String repo = rs.getString(2);
					String filename = rs.getString(3);
					String metadata = rs.getString(4);
					model.setRepo(repo);
					model.setFile(new File(filename));
					model.setMetadata(metadata);
					models.add(model);
					
					processed.put(id, model);
				}
				
				Object clusterId = rs.getObject(5);
				String clusterName = rs.getString(6);
				
				if (clusterId != null)
					model.addCluster(new Cluster((int) clusterId, clusterName));
			}
			
			this.modelsCache = processed;
			return models;
		} catch (SQLException e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}				
	}

	@NonNull
	public Map<SwModel, Event> getEvents() {
		try {						
			String stmt = "SELECT max(start_time), type, model_id FROM events WHERE model_id IS NOT NULL GROUP BY model_id ";		
			PreparedStatement preparedStatement = connection.prepareStatement(stmt);
			preparedStatement.execute();
			ResultSet rs = preparedStatement.getResultSet();
			Map<SwModel, Event> events = new HashMap<>();
			while (rs.next()) {
				long time = rs.getLong(1);
				String model_id = rs.getString(3);
				SwModel model = getModel(model_id);
				events.put(model, new Event(time));
			}
			return events;
		} catch (SQLException e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}				
	}

	public List<SwModel> getModels_Slow() {
		try {			
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT models.id, repo, filename, metadata FROM models LEFT JOIN metadata ON models.id = metadata.id");
			preparedStatement.execute();
			ResultSet rs = preparedStatement.getResultSet();
			List<SwModel> models = new ArrayList<SwModel>();
			while (rs.next()) {
				String id = rs.getString(1);
				String repo = rs.getString(2);
				String filename = rs.getString(3);
				String metadata = rs.getString(4);
				SwModel model = new SwModel(id);
				model.setRepo(repo);
				model.setFile(new File(filename));
				model.setMetadata(metadata);
				models.add(model);
				
				addClustersToModel(id, model);
			}
			return models;
		} catch (SQLException e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}				
	}

	public SwModel getModel(String repoId, String id) {
		String modelId = SwModel.modelId(repoId, id);
		return getModel(modelId);
	}
	
	// The id must be constructed <repo-id>/<id>
	public SwModel getModel(String modelId) {
		if (modelsCache != null) {
			SwModel model = modelsCache.get(modelId);
			if (model != null)
				return model;
		}
		
		try {			
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, repo, filename FROM models WHERE id = ?");
			preparedStatement.setString(1, modelId);
			//preparedStatement.setString(1, "repo-genmymodel-uml/data/" + id.split("\\\\")[1]);
			preparedStatement.execute();
			ResultSet rs = preparedStatement.getResultSet();
			while (rs.next()) {
				String dbid = rs.getString(1);
				String repo = rs.getString(2);
				String filename = rs.getString(3);
				SwModel model = new SwModel(dbid);
				model.setRepo(repo);
				model.setFile(new File(filename));
				
				addClustersToModel(dbid, model);
				
				return model;
			}
		} catch (SQLException e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}	
		return null;
	}

	private void addClustersToModel(String dbid, SwModel model) throws SQLException {
		PreparedStatement clusterStatement = connection.prepareStatement("SELECT DISTINCT C.id, C.human_name FROM clusters C , clusters_models CM WHERE C.id = CM.cluster_id AND model_name = ?");
		clusterStatement.setString(1, dbid);
		clusterStatement.execute();
		ResultSet rs2 = clusterStatement.getResultSet();
		while (rs2.next()) {
			int clusterId = rs2.getInt(1);
			String clusterName = rs2.getString(2);
			model.addCluster(new Cluster(clusterId, clusterName));
		}
	}

	
	public void updateRepositories(@NonNull List<Repository> repositories) {
		// Possibly nothing to in practice...
	}
	
	public void insertModel(@NonNull String repo, String fileName) {
		this.modelsCache = null;
		
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO models VALUES (?, ?, ?)");;
			preparedStatement.setString(1, SwModel.modelId(repo, fileName));
			preparedStatement.setString(2, repo);
			preparedStatement.setString(3, fileName);
			preparedStatement.execute();
		} catch (SQLException e) {
			if (e.getErrorCode() == 19) {
				// Attempt to re-insert an element, no problem
				return;
			}
			e.printStackTrace();
			throw new RuntimeException(e);
		}
       
	}

	public void updateModel(@NonNull SwModel model) {		
		updateModel(model, true);
	}
	
	public void updateModel(@NonNull SwModel model, boolean recordTrace) {
		String metadata = model.getMetadata();
		String json;
		try {
			Map<String, List<String>> map = AnnotationsValidator.INSTANCE.toMap(metadata);
			ObjectMapper mapper = new ObjectMapper();
			json = mapper.writeValueAsString(map);
		} catch (SyntaxError | JsonProcessingException e2) {
			throw new IllegalArgumentException();
		}
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO metadata VALUES (?, ?, ?)");
			preparedStatement.setString(1, model.getId());
			preparedStatement.setString(2, metadata);
			preparedStatement.setString(3, json);
			preparedStatement.execute();
			System.out.println("Inserted!");
			
			if (recordTrace)
				registerEvent("metadata-update", model.getId());
			
			if (modelsCache != null) {
				this.modelsCache.put(model.getId(), model);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == 19) {
				// Attempt to re-insert an element, no problem
				try {
					PreparedStatement preparedStatement = connection.prepareStatement("UPDATE metadata SET metadata = ?, json = ? WHERE id = ?");
					preparedStatement.setString(1, metadata);
					preparedStatement.setString(2, json);
					preparedStatement.setString(3, model.getId());
					preparedStatement.execute();
					System.out.println("Updated!");
					return;
				} catch (SQLException e1) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}			
			e.printStackTrace();
			throw new RuntimeException(e);		
		}
	}

	public void insertCluster(@NonNull String repoName, String modelName, int cluster) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM clusters WHERE id_in_csv = ? and repo_name = ?");
			preparedStatement.setInt(1, cluster);
			preparedStatement.setString(2, repoName);
			preparedStatement.execute();
			
			ResultSet rs = preparedStatement.getResultSet();
			if (rs.next() == false) {
				// Insert cluster
				PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO clusters (id_in_csv, repo_name) VALUES (?, ?)");;
				insertStatement.setInt(1, cluster);
				insertStatement.setString(2, repoName);
				insertStatement.execute();
									
				// Do it again
				insertCluster(repoName, modelName, cluster);
				return;
			} else {
				int id = rs.getInt(1);
				PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO clusters_models (cluster_id, model_name) VALUES (?, ?)");;
				insertStatement.setInt(1, id);
				insertStatement.setString(2, SwModel.modelId(repoName, modelName));
				insertStatement.execute();
				// TODO: Check that the modelName actually exists...
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void registerEvent(String type, String modelId) {
		try {
			long startMillis = System.currentTimeMillis();
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO events(start_time, type, model_id) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, startMillis);
			preparedStatement.setString(2, type);			
			preparedStatement.setString(3, modelId);
			preparedStatement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
}
