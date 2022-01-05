package org.hunters.server.services;

import org.hunters.server.models.ChatMessage;
import org.hunters.server.models.users.MatchesTable;
import org.hunters.server.models.users.User;
import org.hunters.server.models.users.UserTable;
import org.hunters.server.security.Authorizer;
import org.hunters.server.utils.Csv;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

public class UserService {
    private final static UserTable inMemUserTable = new UserTable();
    private final static MatchesTable matchesTable = new MatchesTable();
    private final static Csv data = new Csv("server/src/main/resources/data.csv");

    public final static HashSet<Long> socketsToClose = new HashSet<>();

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

        // TODO: Get initial matches from file
        var admin = getById(0);
        for(int i = 0; i < 5; i++) {
            var match = getIf(user -> admin.suggestUser(user));
            if(match == null)
                break;
            int matchId = matchesTable.addMatch(admin.id, match.id);
            admin.addNewMatch(matchId, match.id);
            match.addNewMatch(matchId, admin.id);
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

    private static User getIf(Predicate<User> p){
        for(User user : inMemUserTable.getAll()) {
            if(p.test(user))
                return user;
        }

        return null;
    }

    private static String formattedMatch(int matchId, int userId) {
        var user = inMemUserTable.getById(userId);
        return "{\"id\":\"" + matchId + "\"," + summary(user) + '}';
    }

    /*
    * Fetch all matches as formatted string
    */
    private static String getMatches(int id) {
        StringBuilder sb = new StringBuilder("[");
        for(var matchId : inMemUserTable.getById(id).getMatches()) {
            sb.append(formattedMatch(matchId, matchesTable.getMatch(matchId, id)));
            sb.append(',');
        }
        if(sb.length() > 1)
            sb.setCharAt(sb.length()-1, ']');
        else
            sb.append(']');
        return sb.toString();
    }

    /*
     * It is assumed that json schema is valid
     */
    private static User userFromJson(Json user) {
        return new User(user.get("name"), user.get("birthday"), user.get("gender"),
                user.get("interest"), user.get("email"), user.get("password"));
    }

    private static String summary(User user) {
        return "\"name\":\"" + user.name + "\"," +
                "\"email\":\"" + user.email + "\"," +
                "\"birthday\":\"" + user.getBday() + '\"' +
            (user.image != null ? "\"image\":\"" + user.image : "");
    }

    /*
     * User as formatted json without password
     */
    public static String omitHash(User user) {
        return "\"id\":\"" + user.id + "\"," +
                summary(user) + ',' +
                "\"gender\":\"" + user.gender + "\"," +
                "\"interest\":\"" + user.interest + "\"";
    }

    /*
    * User as formatted json without password with token
    */
    public static String omitHash(User user, String token) {
        return "{" + omitHash(user) + ",\"token\":\"" + token + "\"}";
    }

    /**
    * Authenticating provided email and password.
    *
    * @return formatted user as string without password. If user with provided
     * email does not exist, null returned. If password is wrong empty string is returned
    */
    public static Response authenticate(String reqEmail, String reqPassword) {
        User user = getIf(user1 -> user1.email.equals(reqEmail));
        if(user == null) {
            System.out.println("User does not exist");
            return new Response(404);
        }

        if(Arrays.hashCode(Authorizer.encrypt(reqPassword)) == Arrays.hashCode(user.hash)) {
            var res = new Response(200, omitHash(user, Authorizer.token(user.id)));
            res.json.put("matches", getMatches(user.id));
            return res;
        }

        return new Response(401);
    }

    /**
     * Register new user in table,
     *
     * @param user Json payload
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

    public static int getMatchUser(int chatId, int userId) {
        return matchesTable.getMatch(chatId, userId);
    }

    public static long getSocketId(int userId) {
        return inMemUserTable.getById(userId).socketId;
    }

    public static void setSocketId(int userId, long socketId) {
        var user = inMemUserTable.getById(userId);
        if(user.socketId != -1)
            socketsToClose.add(user.socketId);
        user.socketId = socketId;
    }

    public static void appendMessage(int userId, int chatId, String msg) {
        getById(userId).pendingMessages.add(new ChatMessage(chatId, msg));
    }
}
