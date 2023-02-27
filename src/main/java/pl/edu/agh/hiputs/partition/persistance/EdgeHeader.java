package pl.edu.agh.hiputs.partition.persistance;

enum EdgeHeader {
  source,
  target,
  length,
  max_speed,
  is_priority_road,
  is_one_way,
  patch_id,
  tags;
}
