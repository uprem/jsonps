/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jsonps;

/**
 *
 * @author prem
 */
public class Utils {

    private Utils() {} // prevent instantiation

    public static boolean isWhiteSpace(char c) {
        if(c==' ' ) return true;
        if(c=='\t') return true;
        if(c=='\r') return true;
        if(c=='\n') return true;
        return false;
    }
    
    public static boolean isDigit(char c) {
        return (c>='0' && c<='9');
    }
}
