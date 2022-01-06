package org.hunters.server.services;

import org.hunters.server.models.ChatMessage;
import org.hunters.server.models.users.MatchesTable;
import org.hunters.server.models.users.User;
import org.hunters.server.models.users.UserTable;
import org.hunters.server.security.Authorizer;
import org.hunters.server.utils.Csv;
import org.hunters.server.utils.FileInfo;
import org.hunters.server.utils.Json;
import org.hunters.server.utils.Response;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Predicate;

public class UserService {
    private final static UserTable inMemUserTable = new UserTable();
    private final static MatchesTable matchesTable = new MatchesTable();
    private final static Csv data = new Csv(FileInfo.RESOURCES_DIR + "/data.csv");

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
        assert admin != null;
        inMemUserTable.mapFirstNThat(5, admin::suggestUser,
                entry -> {
                    int chatId = matchesTable.addMatch(admin.id, entry.id);
                    entry.addNewMatch(chatId, admin.id);
                    admin.addNewMatch(chatId, entry.id);
                    return entry;
                });
    }

    public static User getById(int id) {
        if(inMemUserTable.hasId(id))
            return inMemUserTable.getById(id);
        return null;
    }

    /**
     * Fetching compatible users with a limit of 10 per request.
     *
     * @return Potential matches chosen using [criteria]
     */
    public static User[] getSwipes(int id) {
        User user = inMemUserTable.getById(id);
        // TODO: Add timeout

        User[] picks = inMemUserTable.getFirstNThat(3, user::suggestUser);

        // TODO: First pick bigger batch, and afterwards select 10 by giving them probability (based on age deviation)

        return picks;
    }

    public static void handleSwipeVote(int userId, int swipeId, boolean like) {
        User user = inMemUserTable.getById(userId);
        User swipe = inMemUserTable.getById(swipeId);
        if(like) {
            if(swipe.hasLiked(user.id)) {
                System.out.println("New match: " + user.name + " " + swipe.name);
                addMatch(user, swipe);
                return;
            }
            if(!swipe.suggestUser(user)) {
                user.blacklistUser(swipe);
                return;
            }
            user.addLike(swipe.id);
        }
        else
            user.blacklistUser(swipe);
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

    private static void addMatch(User user1, User user2) {
        int chatId = matchesTable.addMatch(user1.id, user2.id);
        user1.addNewMatch(chatId, user2.id);
        user2.addNewMatch(chatId, user1.id);
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
                ",\"image\":\"" + user.image + '\"';
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
        User user = inMemUserTable.getFirstThat(user1 -> user1.email.equals(reqEmail));
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
        if(inMemUserTable.getFirstThat(user1 -> user1.email.equals(user.get("email"))) != null)
            return new Response(403);
        User newUser = userFromJson(user);
        inMemUserTable.add(newUser);
        return new Response(200, omitHash(newUser, Authorizer.token(newUser.id)) );
    }

    public static Response addImage(int id, Path imagePath) {
        var user = getById(id);
        if(user == null)
            return new Response(404);
        user.setImage(imagePath.toString().replace('\\','/'));

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
