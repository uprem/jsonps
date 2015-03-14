/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author prem
 */
public class GeneralEx {

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
    public void manualEx() {
        String json="{\"a\":\"y  e a!\", \"b\":\"bee\\\"\", \"newobj\":\"go\\\"ood\",\"test\":[{\"arrayname\":[]}], \"t2\":{ \"num\":0.23e-3}, \"bool\":   trUe, \"nullname\":nuLl, \"nottrue\":False}";
        JsonParsingEventListener eventListener=new JsonParsingEventListener();
        JsonParser jp;
        int i, len;
        char[] buf=json.toCharArray();

        jp=new JsonParser(eventListener);

        len=json.length();
        for(i=0;  i<len;  i++) {
            jp.process(buf[i]);
        }
    }
}
