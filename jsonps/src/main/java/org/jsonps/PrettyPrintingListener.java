/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

import java.io.PrintStream;

/**
 * actually I want to name this class: PrettyPrintingParserEventListener..
 * but..
 *
 * @author prem
 */
public class PrettyPrintingListener extends JsonParsingEventListener {

    private final PrintStream out;
    private final int indentSize=4;

    private int indentLevel=0;
    private boolean atBoL=true;

    public PrettyPrintingListener(PrintStream out) {
        this.out = out;
    }

    @Override
    public void startObject() {
        printIndent();
        out.println('{');
        atBoL=true;
        indentLevel++;
    }

    @Override
    public void endObject() {
        if(!atBoL) {
            out.println();
            atBoL=true;
        }
        indentLevel--;
        printIndent();
        out.print('}');
        //atBoL=true;
    }

    @Override
    public void startArray() {
        printIndent();
        out.println('[');
        atBoL=true;
        indentLevel++;
    }

    @Override
    public void endArray() {
        if(!atBoL) {
            out.println();
            atBoL=true;
        }
        indentLevel--;
        printIndent();
        out.print(']');
        //atBoL=true;
    }

    @Override
    public void name(String name) {
        if(!atBoL) {
            out.println();
            atBoL=true;
        }
        printIndent();
        out.print('\"');
        out.print(name);
        out.print('\"');
        out.print(' ');
        out.print(':');
        out.print(' ');
    }

    @Override
    public void value(String value) {
        out.print('\"');
        out.print(value);
        out.print('\"');
    }

    @Override
    public void sequence(String value) {
        out.print(value);
    }

    @Override
    public void number(String value) {
        out.print(value);
    }

    @Override
    public void colon() { // name and val separator
        //logger.log(Level.INFO, "got colon");
    }

    @Override
    public void comma() { // val separator
        out.print(',');
        out.println();
        atBoL=true;
    }

    private void printIndent() {
        int i, len;

        if(atBoL) {
            atBoL=false;

            len=indentLevel*indentSize;
            for(i=0;  i<len;  i++) {
                out.print(' ');
            }
        }
    }
}
