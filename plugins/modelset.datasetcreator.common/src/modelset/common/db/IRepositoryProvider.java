package modelset.common.db;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface IRepositoryProvider {

	@Nullable
	Repository getRepository(@NonNull String repoId);

	IDatasetOrigin getOrigin();
}
