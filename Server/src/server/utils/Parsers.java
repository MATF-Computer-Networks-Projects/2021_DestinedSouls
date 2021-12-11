package server.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parsers {
    public static String EmailRegex = "([\\W\\w]+@[\\w]+\\.[\\w]+)";
    public static String PasswordRegex = "([a-zA-Z0-9.,*/#']+)";
    private static String loginRegex = "\\{\"email\":\"" + EmailRegex + "\",\"password\":\"" + PasswordRegex + "\"}";
    public static Pattern loginPattern = Pattern.compile(loginRegex);

    public static String jsonRegex = "\\\"([a-zA-Z0-9]+)\":\"([a-zA-Z0-9.@]+)\"";
    public static Pattern jsonPattern = Pattern.compile(jsonRegex);


}
