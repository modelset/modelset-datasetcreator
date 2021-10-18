package modelset.datasetcreator.ui;

import java.util.Map;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;

import modelset.common.db.Event;
import modelset.common.db.SwModel;

public class EventComparator extends ViewerComparator {

	private Map<SwModel, Event> events;

	public EventComparator(Map<SwModel, Event> events) {
		this.events = events;
	}
		
	public int compare(Viewer viewer, Object e1, Object e2) {
		SwModel m1 = (SwModel) e1;
		SwModel m2 = (SwModel) e2;
		
		Event v1 = events.get(m1);
		Event v2 = events.get(m2);
		
		if (v1 == null && v2 == null) {
			return 0;
		}
		
		if (v1 == null) {
			return 1;
		}
		if (v2 == null) {
			return -1;
		}
		
		return -1 * Long.compare(v1.getTime(), v2.getTime());
	}

	public static class NoFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return true;
		}

	}

}
