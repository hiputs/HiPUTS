package pl.edu.agh.hiputs.visualization.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationStoppedEvent extends ApplicationEvent {

    public ApplicationStoppedEvent(Object source) {
        super(source);
    }
}
