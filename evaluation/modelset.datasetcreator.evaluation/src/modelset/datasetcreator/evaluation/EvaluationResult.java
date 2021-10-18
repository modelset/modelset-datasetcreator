package modelset.datasetcreator.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import modelset.common.db.SwModel;

public class EvaluationResult {

	private List<Streak> streaks = new ArrayList<>();
	private Map<String, List<Streak>> streakByDomain = new HashMap<>();
	
	public Streak addStreak(SwModel first, String domain) {
		Streak streak = new Streak(first, domain);
		this.streaks.add(streak);
		List<Streak> domainStreaks = streakByDomain.computeIfAbsent(domain, k -> new ArrayList<>());
		domainStreaks.add(streak);
		return streak;
	}
	
	@Override
	public String toString() {
		double total = 0;
		int largest = 0;
		int quantities[] = new int[streaks.size()];
		
		int i = 0;
		for (Streak streak : streaks) {
			total += streak.getSize();
			if (streak.getSize() > largest) {
				largest = streak.getSize();
			}
			quantities[i++] = streak.getSize();
		}
		
//		int totalStreaks
//		streakByDomain.forEach((d, s) -> {
//			
//		});

		double repetitionAvg = streaks.size() / ((double) streakByDomain.size());
		
		Arrays.sort(quantities);
		int median = quantities[quantities.length / 2];
		
		double avgStreak = total / streaks.size();
		return String.format(Locale.US, 
				"Avg, Median, Max, Ravg\n" +
				"%.2f, %d, %d, %.2f", avgStreak, median, largest, repetitionAvg);
	}

	public void saveTo(String folder) {
		File output = new File(folder + File.separator + "streaks.csv");
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(output), CSVFormat.DEFAULT)) {
			int i = 0;
			for (Streak streak : streaks) {
				printer.printRecord(i, streak.getFirst().getId(), streak.getDomain(), streak.getSize());
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		output = new File(folder + File.separator + "repetitions.csv");
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(output), CSVFormat.DEFAULT)) {
			for (Entry<String, List<Streak>> entry : streakByDomain.entrySet()) {
				String domain = entry.getKey();
				int i = 0;
				for (Streak streak : entry.getValue()) {
					printer.printRecord(domain, i, streak.getFirst().getId());
					for (SwModel swModel : streak.getSecondary()) {
						printer.printRecord(domain, i, swModel.getId());						
					}
					i++;
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			output = new File(folder + File.separator + "stats.csv");
			try(FileWriter writer = new FileWriter(output)) {
				writer.append(this.toString());	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
}
