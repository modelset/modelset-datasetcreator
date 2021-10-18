package modelset.datasetcreator.visualizer.uml;

import java.io.File;
import java.io.IOException;

import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;


public class InteractionDiagramVisualizer {
	private PlantUmlText text;

	public InteractionDiagramVisualizer() {
		this.text = new PlantUmlText();		
	}
	
	public void visualize(Interaction interaction, File file) throws IOException {
		text.start();
	
		for(Message message : interaction.getMessages()) {
			MessageOccurrenceSpecification source = (MessageOccurrenceSpecification) message.getReceiveEvent();
			MessageOccurrenceSpecification target = (MessageOccurrenceSpecification) message.getSendEvent();
			if(source != null && target != null) {
				String sourceLifeline = (source.getCovered() == null) ? "Unnamed" : source.getCovered().getName();
				String targetLifeline = (target.getCovered() == null) ? "Unnamed" : target.getCovered().getName();
				text.line(sourceLifeline + "->" + targetLifeline + ":" + message.getName());
			}
		}
		
		text.end();
		text.toText(System.out);
		text.toImage(file);
	}

}
