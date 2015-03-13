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
public class JsonParser {

    private static final Logger logger=Logger.getLogger(JsonParser.class.getName());

    private static enum ParserState {
        EXPECTING_START_OBJECT_OR_ARRAY,
        EXPECTING_END_OBJECT_OR_NAME,
        EXPECTING_END_ARRAY_OR_VALUE,
        EXPECTING_NAME,
        EXPECTING_COLON,
        EXPECTING_VALUE,
        EXPECTING_END_OBJECT_OR_COMA,
        EXPECTING_END_ARRAY_OR_COMA,
    }

    private ParserState parserState;
    private int lexerStatus;
    private char[] buf=new char[10240];

    public JsonParser() {
        parserState=ParserState.EXPECTING_START_OBJECT_OR_ARRAY;
    }

    public void process(char c) {
        switch(parserState) {
            case EXPECTING_START_OBJECT_OR_ARRAY:
                if(Utils.isWhiteSpace(c)) break;
                if(c=='{') {
                    parserState=ParserState.EXPECTING_END_OBJECT_OR_NAME;
                    break;
                }
                if(c=='[') {
                    parserState=ParserState.EXPECTING_END_ARRAY_OR_VALUE;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'", c));
                break;
        }
    }

    private void raiseError(String message) {
        logger.log(Level.SEVERE, message);
    }

}
