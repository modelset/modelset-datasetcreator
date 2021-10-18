package modelset.common.db;

import org.eclipse.jdt.annotation.Nullable;

public interface IDatasetOrigin {

	public String getURL(String id);
	public @Nullable String getFullURL(String id);
		
	public static class GithubDataset implements IDatasetOrigin {

		@Override
		public String getURL(String id) {
			String[] parts = id.split("/");
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (part.equals("data")) {
					// The next two elements are user/project
					if (i + 2 < parts.length) {
						return "http://github.com/" + parts[i + 1] + "/" + parts[i + 2];
					}
				}
			}
			return id;
		}

		@Override
		public @Nullable String getFullURL(String id) {
			String[] parts = id.split("/");
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (part.equals("data")) {
					// The next two elements are user/project
					String r = "http://github.com";
					for(int j = i; j < parts.length; j++) {
						r += "/" + parts[j];
					}
					return r;
				}
			}
			return id;
		}
		
	}

	public static class GenMyModelDataset implements IDatasetOrigin {

		@Override
		public String getURL(String id) {
			String[] parts = id.split("/");
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (part.equals("data")) {
					// The next two elements are user/project
					if (i + 1 < parts.length) {
						String prefix = "https://app.genmymodel.com/api/projects/";
						return prefix + parts[i + 1].replaceAll("\\.xmi$", "") + "/xmi";
					}
				}
			}
			return null;
		}

		@Override
		public @Nullable String getFullURL(String id) {
			return getURL(id);
		}
	}
}
