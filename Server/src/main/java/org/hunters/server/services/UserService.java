package org.hunters.server.services;

import org.hunters.server.models.users.User;
import org.hunters.server.models.users.UserTable;
import org.hunters.server.security.Authorizer;
import org.hunters.server.utils.Csv;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class UserService {
    private static UserTable inMemUserTable = new UserTable();
    private static UserTable inMemUserTableOnline = new UserTable();
    private static Csv data = new Csv("server/src/main/resources/data.csv");

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

        ArrayList<String> res;
        while(!((res = data.getEntry()) == null)) {
            inMemUserTable.add(new User(res));
        }
    }

    public static User getById(int id) {
        if(inMemUserTable.hasId(id))
            return inMemUserTable.getById(id);
        return null;
    }

    public static List<User> getAll(int id) {
        if(inMemUserTable.hasId(id))
        {
            return new LinkedList<User>(inMemUserTable.getAll());
        }
        return null;
    }

    public static List<User> getOnline(int id) {
        if(inMemUserTable.hasId(id))
        {
            return getAll(user -> user.id != id);
        }
        return null;
    }

    private static User getIf(Predicate<User> p){
        for(User user : inMemUserTable.getAll()) {
            if(p.test(user))
                return user;
        }

        return null;
    }


    private static List<User> getAll(Predicate<User> p) {
        var users = new LinkedList<User>();
        for(User user : inMemUserTable.getAll())
            if(p.test(user))
                users.add(user);
        return users;
    }

    /*
     * It is assumed that json schema is valid
     */
    private static User userFromJson(Json user) {
        return new User(user.get("name"), user.get("birthday"), user.get("gender"),
                user.get("interest"), user.get("email"), user.get("password"));
    }


    /*
     * User as formatted json without password
     */
    public static String omitHash(User user) {
        return "\"id\":\"" + user.id + "\"," +
                "\"name\":\"" + user.name + "\"," +
                "\"email\":\"" + user.email + "\"," +
                "\"birthday\":\"" + user.getBday() + "\"," +
                "\"gender\":\"" + user.gender + "\"," +
                "\"interest\":\"" + user.interest + "\"";

    }

    /*
    * User as formatted json without password with token
    */
    public static String omitHash(User user, String token) {
        return "{" + omitHash(user) + ",\"token\":\"" + token + "\"}";
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
            return new Response(404);
        }

        if(Arrays.hashCode(Authorizer.encrypt(reqPassword)) == Arrays.hashCode(user.hash)){
            inMemUserTableOnline.add(user);
            return new Response(200, omitHash(user, Authorizer.token(user.id)));
        }

        return new Response(401);
    }

    /**
     * Register new user in table,
     *
     * @param Json user
     * @return Formatted user as string without password. If user with provided email does not exist, null returned
     */
    public static Response register(Json user) {
        if(getIf(user1 -> user1.email.equals(user.get("email"))) != null)
            return new Response(403);
        User newUser = userFromJson(user);
        inMemUserTable.add(newUser);
        return new Response(200, omitHash(newUser, Authorizer.token(newUser.id)) );
    }

    public static Response addImage(int id, Path imagePath) {
        var user = getById(id);
        if(user == null)
            return new Response(404);
        user.setImage(imagePath);

        String strPath = StorageService.uploadsDir.toString() + "/" + imagePath.getFileName();
        return new Response(200, "{\"img\":\"" + strPath + "\"}");
    }
}
