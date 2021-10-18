package modelset.datasetcreator.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.datasetcreator.controller.DatasetCreatorController;

public class SearchFilter {

	@Nullable
	private ViewerFilter filter;

	public SearchFilter(@Nullable ViewerFilter f) {
		this.filter = f;
	}

	@NonNull
	public static SearchFilter fromText(@NonNull String text, @NonNull DatasetCreatorController controller) {
		Pattern pattern = Pattern.compile("cluster:([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        ViewerFilter f = null;
        if (matcher.find()) {
        	int cluster = Integer.parseInt(matcher.group(1));
        	f = new ClusterFilter(cluster);
        }
       
        if (f == null) {        
			Pattern pattern2 = Pattern.compile("name:(.+)", Pattern.CASE_INSENSITIVE);
	        Matcher matcher2 = pattern2.matcher(text);
	        if (matcher2.find()) {
	        	String name = matcher2.group(1);
	        	f = new NameFilter(name);
	        }
        }
        
        if (f == null) {
			Pattern pattern2 = Pattern.compile("category:(.+)", Pattern.CASE_INSENSITIVE);
	        Matcher matcher2 = pattern2.matcher(text);
	        if (matcher2.find()) {
	        	String name = matcher2.group(1);
	        	f = new CategoryFilter(name, controller);
	        }        	
        }

        
        if (f == null) {        
			Pattern pattern2 = Pattern.compile("(.+):(.+)", Pattern.CASE_INSENSITIVE);
	        Matcher matcher2 = pattern2.matcher(text);
	        if (matcher2.find()) {
	        	String key = matcher2.group(1);
	        	String name = matcher2.group(2);
	        	f = new AnnotationFilter(key, name);
	        }
        }

        return new SearchFilter(f);
	}

	public void applyTo(@NonNull StructuredViewer viewer) {
        if (filter == null) {
        	viewer.resetFilters();
        } else {
        	viewer.setFilters(filter);
        }		
	}
	
}
