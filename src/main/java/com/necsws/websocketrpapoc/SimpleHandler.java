package com.necsws.websocketrpapoc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleHandler.class);

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private ODBCPackageCall odbcPackageCall;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException, InterruptedException {
        JsonNode jsonMessage = extractJsonMessage(message);
        if (jsonMessage == null)
            return;

//        log.info("Received message is {} on {}", jsonMessage, session.getId());
        // How does this work, do we suspend the object and keep track of objects that
        // are runnable
        // OR create a thread and then keep track of that...
        if (Repository.processorMapBySession().containsKey(session.getId())) {
            log.info("Existing Processor");
            RPAProcessor rpaProcessor = Repository.processorMapBySession().get(session.getId());
            rpaProcessor.setCurrentJsonMessage(jsonMessage);
            synchronized (rpaProcessor) {
                rpaProcessor.notify();
            }
        } else {
            log.info("New Processor");
            RPAProcessor rpaProcessor = new RPAProcessor(session, configProperties, odbcPackageCall);
            rpaProcessor.setCurrentJsonMessage(jsonMessage);
            Repository.processorMapBySession().put(session.getId(), rpaProcessor);
            Thread thread = new Thread(rpaProcessor);
            thread.start();
        }
    }

    private JsonNode extractJsonMessage(TextMessage message) {
        try {
            return new ObjectMapper().readTree(message.getPayload());
        } catch (JsonProcessingException ex) {
            log.warn("Invalid payload received", ex);
            return null;
        }
    }

}
