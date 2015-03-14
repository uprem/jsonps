/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author prem
 */
public class GeneralEx {

    private static final Logger logger=Logger.getLogger(GeneralEx.class.getName());

    public GeneralEx() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void exercizeParser() {
        String json="{\"a\":\"y  e a!\", \"b\":\"bee\\\"\", \"newobj\":\"go\\\"ood\",\"test\":[{\"arrayname\":[]}], \"t2\":{ \"num\":0.23e-3}, \"bool\":   trUe, \"nullname\":nuLl, \"nottrue\":False}";
        Expected[] expEventSeq={
            new Expected(ParsingEvent.STARTOBJ,         ""),
            new Expected(ParsingEvent.NAME,             "a"),
            new Expected(ParsingEvent.VALUE,            "y  e a!"),
            new Expected(ParsingEvent.NAME,             "b"),
            new Expected(ParsingEvent.VALUE,            "bee\\\""),
            new Expected(ParsingEvent.NAME,             "newobj"),
            new Expected(ParsingEvent.VALUE,            "go\\\"ood"), // "go\"ood"
            new Expected(ParsingEvent.NAME,             "test"), // "test"
            new Expected(ParsingEvent.STARTARR,         ""),
            new Expected(ParsingEvent.STARTOBJ,         ""),
            new Expected(ParsingEvent.NAME,             "arrayname"),
            new Expected(ParsingEvent.STARTARR,         ""),
            new Expected(ParsingEvent.ENDARR,           ""),
            new Expected(ParsingEvent.ENDOBJ,           ""),
            new Expected(ParsingEvent.ENDARR,           ""),
            new Expected(ParsingEvent.NAME,             "t2"), // "t2"
            new Expected(ParsingEvent.STARTOBJ,         ""),
            new Expected(ParsingEvent.NAME,             "num"),
            new Expected(ParsingEvent.NUM,              "0.23e-3"),
            new Expected(ParsingEvent.ENDOBJ,           ""),
            new Expected(ParsingEvent.NAME,             "bool"), //bool
            new Expected(ParsingEvent.SEQ,              "true"),
            new Expected(ParsingEvent.NAME,             "nullname"),
            new Expected(ParsingEvent.SEQ,              "null"),
            new Expected(ParsingEvent.NAME,             "nottrue"),
            new Expected(ParsingEvent.SEQ,              "false"),
            new Expected(ParsingEvent.ENDOBJ,           ""),
        };

        testInstance(json, expEventSeq);
        testPrettyPrint(json);
    }

    private void testInstance(String json, Expected[] expEventSeq) {
        JsonParsingEventListener eventListener=new TestListener(expEventSeq);
        JsonParser jp;
        int i, len;
        char[] buf=json.toCharArray();

        jp=new JsonParser(eventListener);

        len=json.length();
        for(i=0;  i<len;  i++) {
            jp.process(buf[i]);
        }
    }

    private void testPrettyPrint(String json) {
        JsonParsingEventListener eventListener=new PrettyPrintingListener(System.out);
        JsonParser jp;
        int i, len;
        char[] buf=json.toCharArray();

        jp=new JsonParser(eventListener);

        len=json.length();
        for(i=0;  i<len;  i++) {
            jp.process(buf[i]);
        }
    }

    private static enum ParsingEvent {
        STARTOBJ,
        ENDOBJ,
        STARTARR,
        ENDARR,
        NAME,
        VALUE,
        SEQ,
        NUM
    }

    private static class Expected {
        ParsingEvent event;
        String strValue;

        public Expected(ParsingEvent event, String strValue) {
            this.event = event;
            this.strValue = strValue;
        }
    }

    private static class TestListener extends JsonParsingEventListener {

        private final Expected[] expectedEventSeq;
        private int nextExpectedEventIdx=0;

        private TestListener(Expected[] expectedEventSeq) {
            this.expectedEventSeq=expectedEventSeq;
        }

        @Override
        public void startObject() {
            logger.log(Level.INFO, "got start object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.STARTOBJ);
            nextExpectedEventIdx++;
        }

        @Override
        public void endObject() {
            logger.log(Level.INFO, "got end object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.ENDOBJ);
            nextExpectedEventIdx++;
        }

        @Override
        public void startArray() {
            logger.log(Level.INFO, "got start array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.STARTARR);
            nextExpectedEventIdx++;
        }

        @Override
        public void endArray() {
            logger.log(Level.INFO, "got end array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.ENDARR);
            nextExpectedEventIdx++;
        }

        @Override
        public void name(String name) {
            logger.log(Level.INFO, "got name:[{0}]", name);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.NAME);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, name);
            nextExpectedEventIdx++;
        }

        @Override
        public void value(String value) {
            logger.log(Level.INFO, "got value:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.VALUE);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }

        @Override
        public void sequence(String value) {
            logger.log(Level.INFO, "got sequence:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.SEQ);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }

        @Override
        public void number(String value) {
            logger.log(Level.INFO, "got number:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, ParsingEvent.NUM);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }
    }
}
