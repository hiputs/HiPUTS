package pl.edu.agh.hiputs.statistics.server;

import pl.edu.agh.hiputs.statistics.StageTimeService;

public interface StatisticSummaryService extends StageTimeService {

  void generateStatisticCSVs();

}
