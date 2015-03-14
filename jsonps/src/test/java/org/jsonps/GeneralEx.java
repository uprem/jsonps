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
            new Expected(TestListener.EVENT_STARTOBJ,         ""),
            new Expected(TestListener.EVENT_NAME,             "a"),
            new Expected(TestListener.EVENT_VALUE,            "y  e a!"),
            new Expected(TestListener.EVENT_NAME,             "b"),
            new Expected(TestListener.EVENT_VALUE,            "bee\\\""),
            new Expected(TestListener.EVENT_NAME,             "newobj"),
            new Expected(TestListener.EVENT_VALUE,            "go\\\"ood"), // "go\"ood"
            new Expected(TestListener.EVENT_NAME,             "test"), // "test"
            new Expected(TestListener.EVENT_STARTARR,         ""),
            new Expected(TestListener.EVENT_STARTOBJ,         ""),
            new Expected(TestListener.EVENT_NAME,             "arrayname"),
            new Expected(TestListener.EVENT_STARTARR,         ""),
            new Expected(TestListener.EVENT_ENDARR,           ""),
            new Expected(TestListener.EVENT_ENDOBJ,           ""),
            new Expected(TestListener.EVENT_ENDARR,           ""),
            new Expected(TestListener.EVENT_NAME,             "t2"), // "t2"
            new Expected(TestListener.EVENT_STARTOBJ,         ""),
            new Expected(TestListener.EVENT_NAME,             "num"),
            new Expected(TestListener.EVENT_NUM,              "0.23e-3"),
            new Expected(TestListener.EVENT_ENDOBJ,           ""),
            new Expected(TestListener.EVENT_NAME,             "bool"), //bool
            new Expected(TestListener.EVENT_SEQ,              "true"),
            new Expected(TestListener.EVENT_NAME,             "nullname"),
            new Expected(TestListener.EVENT_SEQ,              "null"),
            new Expected(TestListener.EVENT_NAME,             "nottrue"),
            new Expected(TestListener.EVENT_SEQ,              "false"),
            new Expected(TestListener.EVENT_ENDOBJ,           ""),
        };

        testInstance(json, expEventSeq);
    }

    public void testInstance(String json, Expected[] expEventSeq) {
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
    
    private static class Expected {
        int event;
        String strValue;

        public Expected(int event, String strValue) {
            this.event = event;
            this.strValue = strValue;
        }
    }

    private static class TestListener extends JsonParsingEventListener {
        static final int EVENT_STARTOBJ = 1;
        static final int EVENT_ENDOBJ   = 2;
        static final int EVENT_STARTARR = 3;
        static final int EVENT_ENDARR   = 4;
        static final int EVENT_NAME     = 5;
        static final int EVENT_VALUE    = 6;
        static final int EVENT_SEQ      = 7;
        static final int EVENT_NUM      = 8;

        private final Expected[] expectedEventSeq;
        private int nextExpectedEventIdx=0;

        private TestListener(Expected[] expectedEventSeq) {
            this.expectedEventSeq=expectedEventSeq;
        }

        @Override
        public void startObject() {
            logger.log(Level.INFO, "got start object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_STARTOBJ);
            nextExpectedEventIdx++;
        }

        @Override
        public void endObject() {
            logger.log(Level.INFO, "got end object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_ENDOBJ);
            nextExpectedEventIdx++;
        }

        @Override
        public void startArray() {
            logger.log(Level.INFO, "got start array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_STARTARR);
            nextExpectedEventIdx++;
        }

        @Override
        public void endArray() {
            logger.log(Level.INFO, "got end array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_ENDARR);
            nextExpectedEventIdx++;
        }

        @Override
        public void name(String name) {
            logger.log(Level.INFO, "got name:[{0}]", name);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_NAME);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, name);
            nextExpectedEventIdx++;
        }

        @Override
        public void value(String value) {
            logger.log(Level.INFO, "got value:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_VALUE);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }

        @Override
        public void sequence(String value) {
            logger.log(Level.INFO, "got sequence:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_SEQ);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }

        @Override
        public void number(String value) {
            logger.log(Level.INFO, "got number:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].event, EVENT_NUM);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx].strValue, value);
            nextExpectedEventIdx++;
        }
    }
}
