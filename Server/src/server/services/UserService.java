package server.services;

import server.middleware.Authorizer;
import server.models.users.User;
import server.models.users.UserTable;
import server.utils.Json;
import server.utils.Response;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

public class UserService {
    private static UserTable inMemUserTable = new UserTable();

    public static void load() {
        try {
            // TODO: Load from config
            Authorizer.load(
                    "SHA-256",
                    "34dc0dcf-b1c6-4a2d-a639-4e513387d067"
            );
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

    /*
    * User as formatted json without password
    */
    public static String omitHash(User user) {
        return "{\"id\":\"" + user.id + "\"," +
                "\"name\":\"" + user.name + "\"," +
                "\"email\":\"" + user.email + "\"," +
                "\"birthday\":\"" + user.getBday() + "\"," +
                "\"gender\":\"" + user.gender + "\"," +
                "\"interest\":\"" + user.interest + "\"}";
    }

    /*
    * Returns formatted user as string without password
    * If user with provided email does not exist, null returned
    * If password is wrong empty string is returned
    */
    public static Response authenticate(String reqEmail, String reqPassword) {
        User user = getIf(user1 -> user1.email.equals(reqEmail));
        if(user == null) {
            System.out.println("User does not exist");
            return new Response(404,null);
        }

        if(Arrays.hashCode(Authorizer.encrypt(reqPassword)) == Arrays.hashCode(user.hash))
            return new Response(200, omitHash(user));

        return new Response(401, null);
    }


    public static Response register(Json user) {
        if(getIf(user1 -> user1.email.equals(user.get("email"))) != null)
            return new Response(403, null);
        User newUser = new User(user.get("name"), user.get("birthday"), user.get("gender"),
                                user.get("interest"), user.get("email"), user.get("password"));
        inMemUserTable.add(newUser);
        return new Response(200, omitHash(newUser));
    }
}
