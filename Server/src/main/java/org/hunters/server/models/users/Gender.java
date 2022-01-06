package org.hunters.server.models.users;


import java.util.Arrays;
import java.util.Optional;

public enum Gender {
    Male(1),
    Female(2),
    Other(3);

    private final int value;
    Gender(int value) { this.value = value; }
    public static Optional<Gender> valueOf(int value) {
        return Arrays.stream(values())
                .filter(gen -> gen.value == value)
                .findFirst();
    }
    public static Gender fromString(String g){
        if(!g.isEmpty() && g.charAt(0) <= '9')
        {
            return Gender.valueOf(g.charAt(0) - '0').get();
        }

        switch (g.toLowerCase()) {
            case "male":   return Gender.Male;
            case "female": return Gender.Female;
            case "other":
            case "both":   return Gender.Other;
            default: throw new IllegalArgumentException("Invalid gender type");
        }
    }

    public int toInt() {
        return this.value;
    }

    public boolean acceptable(Gender interest) {
        return (interest == Other) || (interest == this);
    }
}