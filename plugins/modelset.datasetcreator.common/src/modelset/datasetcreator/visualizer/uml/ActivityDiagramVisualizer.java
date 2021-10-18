package modelset.datasetcreator.visualizer.uml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.StartObjectBehaviorAction;

/**
 * Docs: https://plantuml.com/en/state-diagram
 * 
 */
public class ActivityDiagramVisualizer {
	
	private PlantUmlText text;

	public ActivityDiagramVisualizer() {
		this.text = new PlantUmlText();		
	}
	
	public void visualize(Activity activity, File file) throws IOException {
		text.start();
		
		for (ActivityEdge edge : activity.getEdges()) {
			if (! edge.getInPartitions().isEmpty())
				continue;
			mapEdge(edge);
		}
		
		for (ActivityPartition p : activity.getPartitions()) {
			mapPartition(p);
		}
		
		// mapRegion(r);		
		text.end();
		text.toText(System.out);
		text.toImage(file);
	}

	private void mapPartition(ActivityPartition p) {
		text.line("partition " + normalizeName(p.getName()) + " {");
		for (ActivityEdge edge : p.getEdges()) {
			if (! edge.getInPartitions().contains(p))
				continue;

			mapEdge(edge);
		}
		text.line("}");
	}
	
	public void mapEdge(ActivityEdge edge) {
		ActivityNode source = edge.getSource();
		ActivityNode target = edge.getTarget();
		if (source == null || target == null) {
			System.out.println("Malformed model, edge without source or target: " + edge);
			return;
		}
		String src = toId(source);
		String tgt = toId(target);
		text.line(src + " --> " + tgt);		
	}

	// repo-uml-pruned/sm/lituss/LobbyServer/LobbyServer/master/sim.uml
		
	private Map<ActivityNode, String> names = new HashMap<>();
	
	public String toId(ActivityNode vertex) {
		if (names.containsKey(vertex))
			return names.get(vertex);
		
		String name = toId_(vertex, names.size());
		names.put(vertex, name);
		return name;
	}

	public String toId_(ActivityNode node, int idx) {		
		if (node instanceof InitialNode) {
			return "(*)";
		} else if (node instanceof StartObjectBehaviorAction) {
			// PlantUML can't do this, emulate with an initial node
			return "(*)";			
		} else if (node instanceof ActivityNode) {
			ActivityNode state = (ActivityNode) node;
			if (state.getName() == null)
				return normalizeName(node.eClass().getName() + "_no_name_" + idx);
			return normalizeName(state.getName());
		}
		/*
		else if (vertex instanceof Pseudostate) {
			Pseudostate state = (Pseudostate) vertex;

			switch(state.getKind()) {
			case INITIAL_LITERAL:
				return "[*]";
			case CHOICE_LITERAL:
			case DEEP_HISTORY_LITERAL:
			case SHALLOW_HISTORY_LITERAL:
				return toSynthesizedName(state.getName(), "H", idx);
			case ENTRY_POINT_LITERAL:
			case EXIT_POINT_LITERAL:
			case FORK_LITERAL:
				if (vertex.getName() == null)
					return "fork_" + idx;
				return vertex.getName() + "_" + idx;
			case JOIN_LITERAL:
				if (vertex.getName() == null)
					return "join_" + idx;
				return vertex.getName() + "_" + idx;
			case JUNCTION_LITERAL:
				return "junction_" + idx;
			case TERMINATE_LITERAL:
			default:
				break;
			
			}
		}
		*/
		throw new UnsupportedOperationException("Vertex not supported: " + node);
	}

	private String normalizeName(String name) {
		return "\"" + name.replaceAll("\"", "") + "\"";
	}
	
	private String toSynthesizedName(String name, String hint, int idx) {
		name = normalizeName(name);
		return name == null ? (hint + "_" + idx) : (name + "_" + hint);
	}
	
}
