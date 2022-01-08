package org.hunters.server.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Csv {
    public Csv(String csvPath) { path = csvPath; openStream();}
    private void openStream(){
        try{
            in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(path)
                    ));
            numOfFields = in.readLine().split(",").length;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> getEntry(){
        try {
            String line = in.readLine();
            if(line == null)
                return null;
            Matcher matcher = Parsers.csvPattern.matcher(line);
            ArrayList<String> parsed = new ArrayList<>(numOfFields);

            int len =0;
            while(matcher.find() && len++<numOfFields){
                parsed.add(matcher.group(1).replaceAll("^\"|\"$", ""));
            }
            if(len == numOfFields - 1)
                parsed.add("placeholder.png");
            return parsed;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedReader in;
    private String path;
    private static int numOfFields;
}
