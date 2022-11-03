package pl.edu.agh.hiputs.visualization.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationResumedEvent extends ApplicationEvent {

    public ApplicationResumedEvent(Object source) {
        super(source);
    }
}
