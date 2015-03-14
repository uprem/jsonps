/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author prem
 */
public class JsonParsingEventListener {

    private static final Logger logger=Logger.getLogger(JsonParsingEventListener.class.getName());

    public void startObject() {
        logger.log(Level.INFO, "got start object");
    }

    public void endObject() {
        logger.log(Level.INFO, "got end object");
    }

    public void startArray() {
        logger.log(Level.INFO, "got start array");
    }

    public void endArray() {
        logger.log(Level.INFO, "got end array");
    }

    public void name(String name) {
        logger.log(Level.INFO, "got name:[{0}]", name);
    }

    public void value(String value) {
        logger.log(Level.INFO, "got value:[{0}]", value);
    }

    public void sequence(String value) {
        logger.log(Level.INFO, "got sequence:[{0}]", value);
    }
}
