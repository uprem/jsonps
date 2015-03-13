/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author prem
 */
public class JsonPrettyPrinterOutputStream extends OutputStream {

    private OutputStream targetOS;

    public JsonPrettyPrinterOutputStream(OutputStream targetOS) {
        this.targetOS=targetOS;
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
