package pl.edu.agh.hiputs.visualization.web;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {
    private WebSocketSession session;
    private VisualizationService visualizationService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;

        visualizationService.sendCoordinates();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("");
    }

    public void sendMessage(String messsage) throws IOException {
        if (session != null)
            synchronized (session) {
                session.sendMessage(new TextMessage(messsage));
            }

    }

    public void subcribe(VisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
