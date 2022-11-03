package pl.edu.agh.hiputs.visualization.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationStartedEvent extends ApplicationEvent {

    public ApplicationStartedEvent(Object source) {
        super(source);
    }
}
