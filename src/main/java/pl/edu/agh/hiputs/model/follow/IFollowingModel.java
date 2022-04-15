package pl.edu.agh.hiputs.model.follow;

public interface IFollowingModel {

  double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed);
}
