package pl.edu.agh.hiputs.partition.mapper.queue;

public interface ServiceQueue <P, R> {

  R executeAll(P graph);
}
