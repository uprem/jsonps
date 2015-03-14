/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author prem
 */
public class JsonPrettyPrinterOutputStream extends OutputStream {

    private OutputStream targetOS;
    private PrintStream out;
    private JsonParsingEventListener eventListener;
    private JsonParser jp;

    public JsonPrettyPrinterOutputStream(OutputStream targetOS) {
        this.targetOS=targetOS;

        out=new PrintStream(targetOS);
        eventListener=new PrettyPrintingListener(out);
        jp=new JsonParser(eventListener);
    }

    @Override
    public void write(int b) throws IOException {
        jp.process((char)b);
    }

    @Override
    public void close() {
        jp.close();
        out.close();
    }

}
