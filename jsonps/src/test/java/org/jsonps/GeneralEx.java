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
        int[] expEventSeq={
            TestListener.EVENT_STARTOBJ,
            TestListener.EVENT_NAME,
            TestListener.EVENT_VALUE,
            TestListener.EVENT_NAME,
            TestListener.EVENT_VALUE,
            TestListener.EVENT_NAME,
            TestListener.EVENT_VALUE, // "go\"ood"

            TestListener.EVENT_NAME,   // "test"
            TestListener.EVENT_STARTARR,
            TestListener.EVENT_STARTOBJ,
            TestListener.EVENT_NAME,
            TestListener.EVENT_STARTARR,
            TestListener.EVENT_ENDARR,
            TestListener.EVENT_ENDOBJ,
            TestListener.EVENT_ENDARR,

            TestListener.EVENT_NAME,  // "t2"
            TestListener.EVENT_STARTOBJ,
            TestListener.EVENT_NAME,
            TestListener.EVENT_NUM,
            TestListener.EVENT_ENDOBJ,

            TestListener.EVENT_NAME,
            TestListener.EVENT_SEQ,
            TestListener.EVENT_NAME,
            TestListener.EVENT_SEQ,
            TestListener.EVENT_NAME,
            TestListener.EVENT_SEQ,
        };

        testInstance(json, expEventSeq);
    }

    public void testInstance(String json, int[] expEventSeq) {
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

    private static class TestListener extends JsonParsingEventListener {
        static final int EVENT_STARTOBJ = 1;
        static final int EVENT_ENDOBJ   = 2;
        static final int EVENT_STARTARR = 3;
        static final int EVENT_ENDARR   = 4;
        static final int EVENT_NAME     = 5;
        static final int EVENT_VALUE    = 6;
        static final int EVENT_SEQ      = 7;
        static final int EVENT_NUM      = 8;

        private final int[] expectedEventSeq;
        private int nextExpectedEventIdx=0;

        private TestListener(int[] expectedEventSeq) {
            this.expectedEventSeq=expectedEventSeq;
        }

        @Override
        public void startObject() {
            logger.log(Level.INFO, "got start object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_STARTOBJ);
        }

        @Override
        public void endObject() {
            logger.log(Level.INFO, "got end object");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_ENDOBJ);
        }

        @Override
        public void startArray() {
            logger.log(Level.INFO, "got start array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_STARTARR);
        }

        @Override
        public void endArray() {
            logger.log(Level.INFO, "got end array");
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_ENDARR);
        }

        @Override
        public void name(String name) {
            logger.log(Level.INFO, "got name:[{0}]", name);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_NAME);
        }

        @Override
        public void value(String value) {
            logger.log(Level.INFO, "got value:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_VALUE);
        }

        @Override
        public void sequence(String value) {
            logger.log(Level.INFO, "got sequence:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_SEQ);
        }

        @Override
        public void number(String value) {
            logger.log(Level.INFO, "got number:[{0}]", value);
            Assert.assertEquals(expectedEventSeq[nextExpectedEventIdx++], EVENT_NUM);
        }
    }
}
