package pl.edu.agh.model.follow;

public interface IFollowingModel {
    double calculateAcceleration(double speed, double desiredSpeed, double distance, double deltaSpeed);
}
