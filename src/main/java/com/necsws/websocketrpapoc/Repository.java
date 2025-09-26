package com.necsws.websocketrpapoc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a non spring managed Repository that allows access to the
 * two maps for the RPAProcessor objects
 * Non Spring managed as it is used in the cucumber context
 */
public class Repository {

    private static final Logger log = LoggerFactory.getLogger(Repository.class);

    private static ConcurrentMap<String, RPAProcessor> processorMapBySession;

    /* Hide the public constructor */
    private Repository() {
    }

    /**
     * Setup a map to keep track of Processors connected to the server
     * Maps by websocket id so we can look them up to accept messages
     * This is used by the websocket handler
     */
    public static ConcurrentMap<String, RPAProcessor> processorMapBySession() {
        if (processorMapBySession == null) {
            log.info("Creating processorMapBySession");
            processorMapBySession = new ConcurrentHashMap<>();
        }
        return processorMapBySession;
    }

}
