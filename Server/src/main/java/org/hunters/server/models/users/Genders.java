package org.hunters.server.models.users;

public class Genders {
    private Genders() {}

    public static Gender fromInt(int g){
        switch (g) {
            case 1: return Gender.Male;
            case 2: return Gender.Female;
            case 3: return Gender.Other;
            // default: throw new IllegalArgumentException("Invalid gender type");
            default: return Gender.Other;
        }
    }

    public static Gender fromString(String g){
        if(!g.isEmpty() && g.charAt(0) <= '9')
        {

            return fromInt(g.charAt(0) - '0');
        }

        switch (g.toLowerCase()) {
            case "male":   return Gender.Male;
            case "female": return Gender.Female;
            case "other":
            case "both":   return Gender.Other;
            default: throw new IllegalArgumentException("Invalid gender type");
        }
    }


    public static String toString(Gender g) {
        switch (g) {
            case Male: return "1";
            case Female: return "2";
            case Other: return "3";
            default: return "GenderTypeErr";
        }
    }
}
