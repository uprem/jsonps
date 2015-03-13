/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.util.Stack;
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
        EXPECTING_COMA_IN_OBJECT,
        EXPECTING_COMA_IN_ARRAY,
        EXPECTING_END_OBJECT_OR_COMA,
        EXPECTING_END_ARRAY_OR_COMA,
        INSIDE_NAME,
        INSIDE_VALUE,
        EXPECTING_EOF
    }

    private static enum ValueType {
        STRING,
        OBJECT,
        ARRAY,
        TRUE,
        FALSE,
        NUMERIC,
        NONE
    }

    private static enum StringType {
        NAME,
        VALUE
    }

    private ParserState parserState;
    private int lexerStatus;
    private ValueType valType;
    private char[] buf=new char[10240];
    int bufpos;
    private StringType strType;

    private Stack<ParserState> psStack=new Stack<>();

    public JsonParser() {
        parserState=ParserState.EXPECTING_START_OBJECT_OR_ARRAY;
    }

    public void process(char c) {
        ValueType vt;

        switch(parserState) {
            case EXPECTING_START_OBJECT_OR_ARRAY:
                if(Utils.isWhiteSpace(c)) break;
                if(c=='{') {
                    psStack.push(parserState);
                    parserState=ParserState.EXPECTING_END_OBJECT_OR_NAME;
                    break;
                }
                if(c=='[') {
                    psStack.push(parserState);
                    parserState=ParserState.EXPECTING_END_ARRAY_OR_VALUE;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '{' or '['", c));
                break;

            case EXPECTING_END_OBJECT_OR_NAME:
                if(Utils.isWhiteSpace(c)) break;
                if(c=='}') {
                    endObjectOrArray();
                    break;
                }
                if(c=='"') {
                    parserState=ParserState.INSIDE_NAME;
                    strType=StringType.NAME;
                    bufpos=0;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '}' or '\"'", c));
                break;

            case EXPECTING_END_ARRAY_OR_VALUE:
                if(Utils.isWhiteSpace(c)) break;
                if(c==']') {
                    endObjectOrArray();
                    break;
                }
                if((vt=startOfValue(c))!=ValueType.NONE) {
                    parserState=ParserState.INSIDE_VALUE;
                    strType=StringType.VALUE;
                    valType=vt;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ']' or start of value", c));
                break;

            case EXPECTING_COLON:
                if(Utils.isWhiteSpace(c)) break;
                if(c==':') {
                    parserState=ParserState.EXPECTING_VALUE;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ':'", c));
                break;

            case EXPECTING_COMA_IN_OBJECT:
                if(Utils.isWhiteSpace(c)) break;
                if(c==',') {
                    parserState=ParserState.EXPECTING_NAME;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ','", c));
                break;

            case EXPECTING_COMA_IN_ARRAY:
                if(Utils.isWhiteSpace(c)) break;
                if(c==',') {
                    parserState=ParserState.EXPECTING_VALUE;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ','", c));
                break;

            case INSIDE_NAME:
                if(c=='"') {
                    // end string;
                    // raise string event with string from buffer
                }
                else {
                    // append character to string buffer;
                    if(bufpos>=buf.length) {
                        raiseError("too long a string encountered. can deal with 8K max");
                        break;
                    }
                    buf[bufpos]=c;
                    bufpos++;
                }
                break;

            case EXPECTING_END_OBJECT_OR_COMA:
                if(Utils.isWhiteSpace(c)) break;
                if(c=='}') {
                    endObjectOrArray();
                    break;
                }
                if(c==',') {
                    parserState=ParserState.EXPECTING_NAME;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '}' or ','", c));
                break;

            case EXPECTING_END_ARRAY_OR_COMA:
                if(Utils.isWhiteSpace(c)) break;
                if(c==']') {
                    endObjectOrArray();
                    break;
                }
                if(c==',') {
                    parserState=ParserState.EXPECTING_VALUE;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '}' or ','", c));
                break;

            case EXPECTING_VALUE:
                if(Utils.isWhiteSpace(c)) break;
                if((vt=startOfValue(c))!=ValueType.NONE) {
                    parserState=ParserState.INSIDE_VALUE;
                    valType=vt;
                    strType=StringType.VALUE;
                }
                break;

            case INSIDE_VALUE:
                parserState=ParserState.INSIDE_NAME;
                process(c);
                break;

            case EXPECTING_NAME:
                if(Utils.isWhiteSpace(c)) break;
                if(c=='"') {
                    parserState=ParserState.INSIDE_NAME;
                    strType=StringType.NAME;
                    bufpos=0;
                }
                break;

        }
    }

    private void endObjectOrArray() {
        ParserState ps;

        switch(ps=psStack.pop()) {
            case EXPECTING_START_OBJECT_OR_ARRAY:
                parserState=ParserState.EXPECTING_EOF;
                break;
            case EXPECTING_COLON:
                parserState=ParserState.EXPECTING_END_OBJECT_OR_COMA;
                break;
            case EXPECTING_END_ARRAY_OR_VALUE:
                parserState=ParserState.EXPECTING_END_ARRAY_OR_COMA;
                break;
            case EXPECTING_END_ARRAY_OR_COMA:
                parserState=ParserState.EXPECTING_END_ARRAY_OR_COMA;
                break;
            default:
                raiseError("internal error.");
                logger.log(Level.SEVERE, "got unexpected state from stack:{0}", ps.name());
        }
    }

    private void raiseError(String message) {
        logger.log(Level.SEVERE, message);
    }

    private ValueType startOfValue(char c) {
        if(c=='"') return ValueType.STRING;
        if(c=='{') return ValueType.OBJECT;
        if(c=='[') return ValueType.ARRAY;
        if(c=='t') return ValueType.TRUE;
        if(c=='f') return ValueType.FALSE;
        if(c=='T') return ValueType.TRUE;
        if(c=='F') return ValueType.FALSE;
        if(Utils.isDigit(c)) return ValueType.NUMERIC;
        return ValueType.NONE;
    }

}
