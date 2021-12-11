package server.services;

import server.middleware.Authorizer;
import server.models.users.User;
import server.models.users.UserTable;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

public class UserService {
    private static UserTable inMemUserTable = new UserTable();

    public static void load() {
        try {
            Authorizer.load("SHA-256", "34dc0dcf-b1c6-4a2d-a639-4e513387d067"); // TODO: Load from config
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        inMemUserTable.add(new User("Aleksa",
                                "2021-11-08",
                                "1",
                                "2",
                                "forsaken.veselic@gmail.com",
                                "000000"
                                    )
        );
    }

    public static User getById(int id) {
        return inMemUserTable.getById(id);
    }

    private static User getIf(Predicate<User> p){
        for(User user : inMemUserTable.getAll()) {
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

        if(Arrays.hashCode(Authorizer.encrypt(reqPassword)) == Arrays.hashCode(user.hash))
            return omitHash(user);

        return "";
    }
}
