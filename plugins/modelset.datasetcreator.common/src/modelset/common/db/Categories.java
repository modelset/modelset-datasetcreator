package modelset.common.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import lombok.NonNull;

public class Categories {

	private Map<String, List<String>> modelsByCategory = new HashMap<>();
	private Map<String, List<String>> categoryByModel = new HashMap<>();
	
	private Categories() { }
	
	public static Categories fromFile(@NonNull String fileName, @NonNull String repoId) throws IOException {
		Categories c = new Categories();
		
		CSVParser p = CSVParser.parse(new File(fileName), Charset.defaultCharset(), CSVFormat.DEFAULT);
		for(CSVRecord r : p) {
			String modelFile = r.get(0);
			String cat = r.get(1);
			String[] categories = cat.split(";");
			if (categories.length == 0) {
				continue;
			}
			
			List<String> clist = new ArrayList<String>();
			c.categoryByModel.put(SwModel.modelId(repoId, modelFile), clist);
			
			for (String str : categories) {
				List<String> models = c.modelsByCategory.computeIfAbsent(str, (k) -> new ArrayList<>());
				models.add(modelFile);
				clist.add(str);
			}			
			
			
		}
		
		return c;
	}

	public List<? extends String> getCategories(SwModel m) {
		List<String> categories = categoryByModel.get(m.getId());
		if (categories == null)
			return Collections.emptyList();
		return categories;
	}
	
}
