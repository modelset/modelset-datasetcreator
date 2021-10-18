package modelset.datasetcreator.transformations;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import modelset.common.annotations.AnnotationsValidator;
import modelset.common.annotations.AnnotationsValidator.ParsedMetadata;
import modelset.common.annotations.AnnotationsValidator.SyntaxError;
import modelset.common.db.SwModel;
import modelset.datasetcreator.controller.DatasetCreatorController;
import modelset.datasetcreator.controller.DatasetCreatorController.InvalidFormat;

public class MetadataTransformation {

	private Matcher matcher;
	private DatasetCreatorController controller;

	public MetadataTransformation(@NonNull File dbFile, @NonNull File dscFile) {
		try {
			this.controller = new DatasetCreatorController(dbFile, dscFile);
		} catch (IOException | InvalidFormat e) {
			throw new RuntimeException(e);
		}	
	}
	
	public MetadataTransformation match(@NonNull String key, @NonNull String value) {
		this.matcher = new Matcher(key, value); 
		return this;
	}
	
	public MetadataTransformation match(@NonNull String key) {
		this.matcher = new Matcher(key, null); 
		return this;
	}

	public void forEachRawModel(@NonNull Consumer<SwModel> consumer) {
		for (SwModel m : this.controller.getAllModels()) {
			consumer.accept(m);
		}
	}
	
	public void forEachModel(@NonNull Consumer<Match> consumer) {
		for (SwModel m : this.controller.getAllModels()) {
			try {
				consumer.accept(new Match(controller, m, m.toMetadata()));
			} catch (SyntaxError e) {
				throw new IllegalStateException("Invalid format " + m.getMetadata() + ". " + m.getId());
			}
		}
	}
	
	public void forEach(@NonNull Consumer<Match> consumer) {
		for (SwModel m : this.controller.getAllModels()) {
			try {
				if (! m.isTagged())
					continue;
				ParsedMetadata metadata = AnnotationsValidator.INSTANCE.toMetadata(m.getMetadata());
				if (matcher.matches(metadata)) {
					consumer.accept(new Match(this.controller, m, metadata));
				}
			} catch (SyntaxError e) {
				throw new IllegalStateException("Invalid format " + m.getMetadata() + ". " + m.getId());
			}
		}
	}
	
	public static class Match {
		private @NonNull SwModel model;
		private @NonNull ParsedMetadata metadata;
		private @NonNull DatasetCreatorController controller;

		public Match(@NonNull DatasetCreatorController controller, @NonNull SwModel model, @NonNull ParsedMetadata metadata) {
			this.controller = controller;
			this.model = model;
			this.metadata = metadata;
		}
		
		public ParsedMetadata getMetadata() {
			return metadata;
		}
		
		public void setMetadata(String metadata) throws SyntaxError {
			this.metadata = AnnotationsValidator.INSTANCE.toMetadata(metadata);
		}
		
		public boolean isTagged() {
			return model.isTagged();
		}
		
		public String getId() {
			return model.getId();
		}
		
		public void remove(@NonNull String key, @NonNull String value) {
			metadata.remove(key, value);
		}
		
		public void set(@NonNull String key, @NonNull String value) {
			metadata.set(key, value);
		}
		
		public void commit() {
			this.model.setMetadata(metadata.serialize());
			this.controller.getDb().updateModel(model, false);
		}

		@NonNull
		public List<? extends String> getValuesOrEmpty(String key) {
			List<? extends String> values = this.metadata.getValues(key);
			if (values == null)
				return Collections.emptyList();
			return values;
		}

		@NonNull
		public SwModel getModel() {
			return model;
		}
	}
	
	public static class Replacer {
		private @NonNull String key;

		public Replacer(@NonNull String key) {
			this.key = key;
		}
	}
	
	public static class Matcher {
		private @NonNull String key;
		private @Nullable String value;

		public Matcher(@NonNull String key, @Nullable String value) {
			this.key = key;
			this.value = value;
		}

		public boolean matches(ParsedMetadata metadata) {
			Collection<? extends String> values = metadata.getValues(key);			
			if (value == null)
				return values == null || values.isEmpty();
						
			return values != null && values.contains(value);
		}
	}
}
