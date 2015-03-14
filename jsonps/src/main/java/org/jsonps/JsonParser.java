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
        EXPECTING_COMMA_IN_OBJECT,
        EXPECTING_COMMA_IN_ARRAY,
        EXPECTING_END_OBJECT_OR_COMMA,
        EXPECTING_END_ARRAY_OR_COMMA,
        INSIDE_NAME,
        INSIDE_VALUE,
        INSIDE_SEQUENCE,
        INSIDE_NUMBER,
        EXPECTING_EOF
    }

    private static enum StringType {
        NAME,
        VALUE,
    }

    private static enum ContainingType {
        OBJECT,
        ARRAY
    }

    private static final String SEQ_TRUE="true";
    private static final String SEQ_FALSE="false";
    private static final String SEQ_NULL="null";

    private ParserState parserState;
    private final char[] buf=new char[10240];
    int bufpos;
    private StringType strType;
    private boolean stringSequenceEscaped;
    private String sequence;
    private int seqpos;

    private final JsonParsingEventListener listener;

    private final Stack<ContainingType> ctStack=new Stack<>();

    public JsonParser(JsonParsingEventListener listener) {
        this.listener=listener;
        parserState=ParserState.EXPECTING_START_OBJECT_OR_ARRAY;
        listener.startParsing();
    }

    public void process(char c) {

        switch(parserState) {
            case EXPECTING_START_OBJECT_OR_ARRAY:
                if(Character.isWhitespace(c)) break;
                if(c=='{') {
                    ctStack.push(ContainingType.OBJECT);
                    parserState=ParserState.EXPECTING_END_OBJECT_OR_NAME;
                    listener.startObject();
                    break;
                }
                if(c=='[') {
                    ctStack.push(ContainingType.ARRAY);
                    parserState=ParserState.EXPECTING_END_ARRAY_OR_VALUE;
                    listener.startArray();
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '{' or '['", c));
                break;

            case EXPECTING_END_OBJECT_OR_NAME:
                if(Character.isWhitespace(c)) break;
                if(c=='}') {
                    endObjectOrArray();
                    listener.endObject();
                    break;
                }
                if(c=='"') {
                    parserState=ParserState.INSIDE_NAME;
                    stringSequenceEscaped=false;
                    strType=StringType.NAME;
                    bufpos=0;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '}' or '\"'", c));
                break;

            case EXPECTING_END_ARRAY_OR_VALUE:
                if(Character.isWhitespace(c)) break;
                if(c==']') {
                    endObjectOrArray();
                    listener.endArray();
                    break;
                }
                if(startOfValue(c)) break;
                raiseError(String.format("unexpected char:'%1$c'. expecting ']' or start of value", c));
                break;

            case EXPECTING_COLON:
                if(Character.isWhitespace(c)) break;
                if(c==':') {
                    parserState=ParserState.EXPECTING_VALUE;
                    listener.colon();
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ':'", c));
                break;

            case INSIDE_NAME:
                if(!stringSequenceEscaped && c=='"') {
                    // end string;
                    // raise string event with string from buffer
                    if(strType==StringType.NAME) {
                        listener.name(new String(buf, 0, bufpos));
                        parserState=ParserState.EXPECTING_COLON;
                    }
                    else
                    if(strType==StringType.VALUE) {
                        listener.value(new String(buf, 0, bufpos));
                        if(ctStack.peek()==ContainingType.OBJECT) {
                            parserState=ParserState.EXPECTING_END_OBJECT_OR_COMMA;
                        }
                        else {
                            parserState=ParserState.EXPECTING_END_ARRAY_OR_COMMA;
                        }
                    }
                    break;
                }

                // append character to string buffer;
                if(bufpos>=buf.length) {
                    raiseError("too long a string encountered. can deal with 8K max");
                    break;
                }
                buf[bufpos]=c;
                bufpos++;

                if(stringSequenceEscaped) {
                    stringSequenceEscaped=false;
                } else {
                    stringSequenceEscaped=(c=='\\');
                }
                break;

            case EXPECTING_END_OBJECT_OR_COMMA:
                if(Character.isWhitespace(c)) break;
                if(c=='}') {
                    endObjectOrArray();
                    listener.endObject();
                    break;
                }
                if(c==',') {
                    parserState=ParserState.EXPECTING_NAME;
                    listener.comma();
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '}' or ','", c));
                break;

            case EXPECTING_END_ARRAY_OR_COMMA:
                if(Character.isWhitespace(c)) break;
                if(c==']') {
                    endObjectOrArray();
                    listener.endArray();
                    break;
                }
                if(c==',') {
                    parserState=ParserState.EXPECTING_VALUE;
                    listener.comma();
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting ']' or ','", c));
                break;

            case EXPECTING_VALUE:
                if(Character.isWhitespace(c)) break;
                if(startOfValue(c)) break;
                raiseError(String.format("unexpected char:'%1$c'. expecting value", c));
                break;

            case INSIDE_VALUE:
                parserState=ParserState.INSIDE_NAME;
                process(c);
                break;

            case EXPECTING_NAME:
                if(Character.isWhitespace(c)) break;
                if(c=='"') {
                    parserState=ParserState.INSIDE_NAME;
                    stringSequenceEscaped=false;
                    strType=StringType.NAME;
                    bufpos=0;
                    break;
                }
                raiseError(String.format("unexpected char:'%1$c'. expecting '\"'", c));
                break;

            case INSIDE_SEQUENCE:
                if(Character.toLowerCase(c)==sequence.charAt(seqpos)) {
                    seqpos++;
                    if(seqpos>=sequence.length()) {
                        listener.sequence(sequence);
                        switch(ctStack.peek()) {
                            case OBJECT: parserState=ParserState.EXPECTING_END_OBJECT_OR_COMMA; break;
                            case ARRAY:  parserState=ParserState.EXPECTING_END_ARRAY_OR_COMMA; break;
                        }
                    }
                }
                else {
                    raiseError(String.format("unexpected char:'%1$c'. expecting '%2$s'", c, sequence.charAt(seqpos)));
                }
                break;

            case INSIDE_NUMBER:
                buf[bufpos]=c;
                bufpos++;
                if(Character.isDigit(c)) break;
                if(Character.toLowerCase(c)=='e') break;
                if(c=='+') break;
                if(c=='-') break;
                if(c=='.') break;

                //if(bufpos==1) {
                //    raiseError(String.format("unexpected char:'%1$c'. expecting number", c));
                //}

                listener.number(new String(buf, 0, bufpos-1));
                switch(ctStack.peek()) {
                    case OBJECT: parserState=ParserState.EXPECTING_END_OBJECT_OR_COMMA; break;
                    case ARRAY:  parserState=ParserState.EXPECTING_END_ARRAY_OR_COMMA; break;
                }
                process(c);
                break;

            default:
                logger.log(Level.SEVERE, "unhandled state:{0}", parserState);

        }
    }

    public void close() {
        listener.endParsing();
    }

    private void endObjectOrArray() {
        ctStack.pop();
        if(ctStack.isEmpty()) {
            parserState=ParserState.EXPECTING_EOF;
        } else {
            switch(ctStack.peek()) {
                case OBJECT: parserState=ParserState.EXPECTING_END_OBJECT_OR_COMMA; break;
                case ARRAY : parserState=ParserState.EXPECTING_END_ARRAY_OR_COMMA; break;
            }
        }
    }

    private void raiseError(String message) {
        logger.log(Level.SEVERE, message);
    }

    private boolean startOfValue(char c) {
        if(c=='"') {
            strType=StringType.VALUE;
            parserState=ParserState.INSIDE_VALUE;
            stringSequenceEscaped=false;
            bufpos=0;
            return true;
        }
        if(c=='{') {
            ctStack.push(ContainingType.OBJECT);
            parserState=ParserState.EXPECTING_END_OBJECT_OR_NAME;
            listener.startObject();
            return true;
        }
        if(c=='[') {
            ctStack.push(ContainingType.ARRAY);
            parserState=ParserState.EXPECTING_END_ARRAY_OR_VALUE;
            listener.startArray();
            return true;
        }
        if(c=='t' || c=='T') {
            parserState=ParserState.INSIDE_SEQUENCE;
            sequence=SEQ_TRUE;
            seqpos=1;
            return true;
        }
        if(c=='f' || c=='F') {
            parserState=ParserState.INSIDE_SEQUENCE;
            sequence=SEQ_FALSE;
            seqpos=1;
            return true;
        }
        if(c=='n' || c=='N') {
            parserState=ParserState.INSIDE_SEQUENCE;
            sequence=SEQ_NULL;
            seqpos=1;
            return true;
        }
        if(Character.isDigit(c)) {
            parserState=ParserState.INSIDE_NUMBER;
            buf[0]=c;
            bufpos=1;
            return true;
        }
        return false;
    }

}
