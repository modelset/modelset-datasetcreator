package modelset.common.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.DatasetDb;
import modelset.common.db.SwModel;
import modelset.common.db.ExternalMetadata.IExternalMetadataProvider;
import modelset.common.db.ExternalMetadata.ModelData;

public class DatasetExporter {

	public static final DatasetExporter INSTANCE = new DatasetExporter();
	
	public void export(@NonNull DatasetDb db, String folder) throws IOException {
		File tagsFile = Paths.get(folder, "tags.txt").toFile();
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(folder, "domains.csv"));
        BufferedWriter allWriter = Files.newBufferedWriter(Paths.get(folder, "all.csv"));        
        CSVPrinter domainPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("id", "domain", "tags", "language", "purpose"));
        CSVPrinter allPrinter = new CSVPrinter(allWriter, CSVFormat.DEFAULT.withHeader("id", "metadata"));
		
		
		Collection<? extends SwModel> models = db.getModels();
		for (SwModel swModel : models) {
			String metadata = swModel.getMetadata();
			try {
				if (! swModel.isTagged())
					continue;

				allPrinter.printRecord(swModel.getId(), metadata);
				
				Map<String, List<String>> map = AnnotationsValidator.INSTANCE.toMap(metadata);
				
				
				List<String> domain = map.get("domain");
				if (domain == null) {
					domain = map.get("category");
				}
				List<String> subdomain = map.get("tags");
				
				List<String> purpose = map.get("purpose");
				List<String> language = map.get("language");
				
				if (domain != null) {
					String d = domain.get(0);
					if (d != null) {
						if (d.startsWith("dummy")) {
							d = "dummy";
						} else if (d.startsWith("unknown")) {
							d = "unknown";
						}
					}
					
					String p = "unknown";
					if (purpose != null)
						p = purpose.get(0);
					
					String sub = "";
					if (subdomain != null)
						sub = String.join("|", subdomain);
					
					String lang = "english";
					if (language != null) {
						lang = language.get(0);
					}
					
					// Check if there is more
					domainPrinter.printRecord(swModel.getId(), d, sub, lang, p);
				}
			} catch (SyntaxError e) {
				System.out.println("Error processing: " + swModel.getId());
				e.printStackTrace();
			}
		}
		
		domainPrinter.close();
		allPrinter.close();
		
		// Save unique tags
		Set<String> tags = new TreeSet<>();
		for (SwModel model : models) {
			String metadata = model.getMetadata();
			if (metadata != null)
				tags.add(metadata);
		}
		
		IOUtils.writeLines(tags, "\n", new FileOutputStream(tagsFile), "utf-8");
	}
	

	/**
	 * <pre>
	 *  {
   	 * "id":1,
   	 * "name": "M1",
     * "source": "ecore",
     * "url": "https://m1.org",
     * "metadata": "metadata1"
     * }
     * </pre>

	 * @param db
	 * @param outputFile
	 */
	public void export(IExternalMetadataProvider metadataProvider, DatasetDb db, File result) throws IOException, SyntaxError {
		Collection<? extends SwModel> models = db.getModels();
				
		int i = 0;
		List<Object> jsonModels = new ArrayList<Object>();
		for (SwModel swModel : models) {
			if (!swModel.isTagged())
				continue;
			
			i++;
			Map<String, Object> data = new HashMap<>();
			
			String name = swModel.getName();
			ModelData provider = metadataProvider.getExternalMetadata(swModel);
			if (provider != null) {
				name = provider.getName();
			}

			data.put("id", swModel.getRepo() + "_" + i);
			data.put("name", name);
			data.put("metadata", filter(swModel.toMetadata().getData()));
			data.put("url", metadataProvider.getURL(swModel));
			data.put("source", swModel.getRepo());

			jsonModels.add(data);
		}
		
		ObjectWriter writer = new ObjectMapper().writer(new DefaultPrettyPrinter());
		writer.writeValue(result, jsonModels);
	}


	private Map<String, List<String>>  filter(Map<String, List<String>> data) {
		Map<String, List<String>> new_ = new HashMap<String, List<String>>();
		List<String> category = data.get("category");
		List<String> type = data.get("type"); // main-diagram!!
		List<String> tags = data.get("tags"); 
		List<String> purpose = data.get("purpose"); 
		List<String> tool = data.get("tool"); 
		List<String> url = data.get("url"); 
		List<String> reference = data.get("reference"); 
		
		if (category != null) new_.put("category", category);
		if (type != null) new_.put("type", type);
		if (tags != null) new_.put("tags", tags);
		if (purpose != null) new_.put("purpose", purpose);
		if (tool != null) new_.put("tool", tool);
		if (url != null) new_.put("url", url);
		if (reference != null) new_.put("reference", reference);

		return new_;
	}
	
}
