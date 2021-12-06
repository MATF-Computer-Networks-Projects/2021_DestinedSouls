package server.services;

import server.models.User;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

/*
{{1, new User( "Aleksa",
                           "2021-11-08",
                           "1",
                           "2",
                           "forsaken.veselic@gmail.com",
                           "123"
                        )
            }};
*/

public class UserService {
    private static Set<User> inMemUserTable = new HashSet();

    public static void load() {
        inMemUserTable.add(new User("Aleksa",
                                "2021-11-08",
                                "1",
                                "2",
                                "forsaken.veselic@gmail.com",
                                "123"
                                    )
        );
    }

    public static User getById(int id) {
        for(User user : inMemUserTable) {
            if(user.id == id)
                return user;
        }
        return null;
    }

    private static User getIf(Predicate<User> p){
        for(User user : inMemUserTable) {
            if(p.test(user))
                return user;
        }

        return null;
    }

    public static String omitHash(User user) {
        return "\"id\":\"" + user.id + "\"," +
                "\"name\":\"" + user.name + "\"," +
                "\"email\":\"" + user.email + "\"," +
                "\"birthday\":\"" + user.getBday() + "\"," +
                "\"gender\":\"" + user.gender + "\"," +
                "\"interest\":\"" + user.interest + "\"";
    }

    /*
    * Returns formatted user as string without password
    * If user with provided email does not exist, null returned
    * If password is wrong empty string is returned
    */
    public static String authenticate(String reqEmail, String reqPassword) {
        User user = getIf(user1 -> user1.email.equals(reqEmail));
        if(user == null) {
            System.out.println("User does not exist");
            return null;
        }

        try {
            byte[] hashpass = MessageDigest.getInstance("SHA-256").digest(reqPassword.getBytes(StandardCharsets.UTF_8));

            if(Arrays.hashCode(hashpass) == Arrays.hashCode(user.hash))
                return omitHash(user);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}
