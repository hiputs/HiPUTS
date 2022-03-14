package pl.edu.agh.scheduler.exception;

public class InsufficientSystemResourcesException extends RuntimeException {

    public InsufficientSystemResourcesException(String message) {
        super(message);
    }
}
