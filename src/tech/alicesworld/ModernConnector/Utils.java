/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tech.alicesworld.ModernConnector;

import java.util.Vector;

/**
 *
 * @author NealShah
 */
public class Utils {
    public static String[] split(String input, char delimiter) {
        Vector stringParts = new Vector();
        
        char[] inputChars = input.toCharArray();
        StringBuffer currentString = new StringBuffer();
        int lastIndex = 0;
        int index = input.indexOf(delimiter);
        
        while (index != -1) {
            stringParts.addElement(input.substring(lastIndex, index));
            lastIndex = index + 1;
            index = input.indexOf(delimiter, lastIndex);
            System.out.println(index);
        }
        stringParts.addElement(input.substring(lastIndex));
        
        String[] returnParts = new String[stringParts.size()];
        for (int i = 0; i < stringParts.size(); i++) {
            returnParts[i] = (String)stringParts.elementAt(i);
        }
        return returnParts;
    }
}
