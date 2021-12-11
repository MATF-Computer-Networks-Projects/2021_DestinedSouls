package server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsers {
    public static String jsonRegex = "\"([a-zA-Z0-9]+)\":\"([a-zA-Z0-9\\-.@]+)\"";
    public static Pattern jsonPattern = Pattern.compile(jsonRegex);

    /*
    * Authorization: Bearer ynRs+M+fdAQx5lHBIaL/t5KkE1GFy+yTpVfjuBi3xjU=
    * */
    private static String authTokenRegex = "[Aa]uthorization: [Bb]earer (.*)\r\n";
    private static Pattern authTokenPattern = Pattern.compile(authTokenRegex);
    public static String parseToken(String request) {
        Matcher matcher = authTokenPattern.matcher(request);
        if(matcher.find()) {
            System.err.println("Token: " + matcher.group(1));
            return matcher.group(1);
        }
        return null;
    }
}
