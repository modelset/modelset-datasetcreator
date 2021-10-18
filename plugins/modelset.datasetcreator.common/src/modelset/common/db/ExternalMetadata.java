package modelset.common.db;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents metadata about a repository which has been computed somehow and
 * provided through a csv file.
 * 
 * Example metadatafile:
 * <pre>
 *   data/_e_ZJAPnjEeeyruBoe7-QtQ.xmi,classDiagramForRobotSimulatorProgram,https://app.genmymodel.com/api/projects/_e_ZJAPnjEeeyruBoe7-QtQ/xmi,alexSilvestre,1516014109072,1516015410784
 *   data/_JoMBgLnSEeehVczkwiTSNA.xmi,GestaoPessoas,https://app.genmymodel.com/api/projects/_JoMBgLnSEeehVczkwiTSNA/xmi,rafinha.amigodedeus,1508969789848,1508970657293
 *   data/_6Tg0IO5MEeiH57IZcqf8WA.xmi,Test,https://app.genmymodel.com/api/projects/_6Tg0IO5MEeiH57IZcqf8WA/xmi,jazajaza,1542887473250,1543236525006
 *   data/16b9834b-efab-443a-8e04-90b124303069.xmi,csce361-soccer2,https://app.genmymodel.com/api/projects/16b9834b-efab-443a-8e04-90b124303069/xmi,kvsmail,1551283593077,1551288598994
 * </pre>
 */
public class ExternalMetadata {

	public static interface IExternalMetadataProvider {

		@Nullable
		ModelData getExternalMetadata(@NonNull SwModel m);

		@Nullable
		String getURL(@NonNull SwModel model);
		
		@Nullable
		String getFullURL(@NonNull SwModel model);		
	}
	
	private Map<String, ModelData> data = new HashMap<>();
	
	@NonNull
	public static ExternalMetadata loadFromFile(@NonNull String fileName, @NonNull String repoId) throws IOException {
		ExternalMetadata m = new ExternalMetadata();
		
		CSVParser p = CSVParser.parse(new File(fileName), Charset.defaultCharset(), CSVFormat.DEFAULT);
		for(CSVRecord r : p) {
			String id = r.get(0);
			String name = r.get(1);
			m.data.put(SwModel.modelId(repoId, id), new ModelData(name));
		}
		
		return m;
	}

	@Nullable
	public ModelData getData(String id) {
		return data.get(id);
	}

	public static class ModelData {
		@NonNull
		private final String name;
		
		public ModelData(@NonNull String name) {
			this.name = name;
		}
		
		@NonNull
		public String getName() {
			return name;
		}
	}
}
