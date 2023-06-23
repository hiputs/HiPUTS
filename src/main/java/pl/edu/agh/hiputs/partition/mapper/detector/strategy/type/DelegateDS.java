package pl.edu.agh.hiputs.partition.mapper.detector.strategy.type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.agh.hiputs.partition.mapper.corrector.Corrector;
import pl.edu.agh.hiputs.partition.mapper.detector.strategy.context.DetectorContext;
import pl.edu.agh.hiputs.partition.mapper.queue.EditableQueue;

@Service
@RequiredArgsConstructor
public class DelegateDS implements DetectorStrategy{
  private final EditableQueue<Corrector> correctorQueue;

  @Override
  public void execute(DetectorContext detectorContext) {
    detectorContext.getPreparedCorrector()
        .ifPresent(correctorQueue::registerService);
  }
}
