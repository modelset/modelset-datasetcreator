package modelset.datasetcreator.visualizer.uml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.sourceforge.plantuml.SourceStringReader;

public class PlantUmlText {

	private StringBuilder builder;

	public PlantUmlText() {
		builder = new StringBuilder();
	}
	
	public PlantUmlText line(String string) {
		builder.append(string);
		builder.append("\n");
		return this;
	}

	public void start() {
		line("@startuml");
	}
	
	public void end() {
		line("@enduml");
	}
	
	public void toImage(File file) throws IOException {
		SourceStringReader reader = new SourceStringReader(builder.toString());
		String d = reader.outputImage(new FileOutputStream(file)).getDescription();
		System.out.println(d);
	}

	public void toText(PrintStream out) {
		out.println(builder.toString());
	}
}
