package org.hunters.server.models.users;

import org.hunters.server.models.ChatMessage;
import org.hunters.server.security.Authorizer;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class User {
    public int id;
    public String name;
    public Date birthday;
    public Gender gender;
    public Gender interest;
    public String email;
    public byte[] hash;
    public String image = null;

    private final HashSet<Integer> blacklist = new HashSet<>();
    private final HashSet<Integer> matches   = new HashSet<>();
    public  long  socketId = -1;
    private final boolean notifyUser = false;

    public final LinkedList<ChatMessage> pendingMessages = new LinkedList<>();

    private static int idObj = 0;

    public User( String name,
                 Date birthday,
                 String gender,
                 String interest,
                 String email,
                 String password
    ) {
        this.id       = idObj++;
        this.name     = name;
        this.birthday = birthday;
        this.email    = email;
        try {
            this.gender   = Gender.fromString(gender);
            this.interest = Gender.fromString(interest);
            this.hash = Authorizer.encrypt(password);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private static Date formatDate(String birthday) {
        try {
            return dateFormatter.parse(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBday() {
        return dateFormatter.format(this.birthday);
    }

    public User( String name,
                 String birthday,
                 String gender,
                 String interest,
                 String email,
                 String password
                ) {

        this( name,
              formatDate(birthday),
              gender,
              interest,
              email,
              password
        );
    }
    public User(ArrayList<String> args){
        this( args.toArray()[0].toString(),
                args.toArray()[1].toString(),
                args.toArray()[2].toString(),
                args.toArray()[3].toString(),
                args.toArray()[4].toString(),
                args.toArray()[5].toString()
                );
    }
    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id + "\"" +
                ",\"name\":\"" + name + "\"" +
                ",\"birthday\":\"" + birthday + "\"" +
                ",\"gender\":\"" + gender + "\"" +
                ",\"interest\":\"" + interest + "\"" +
                ",\"email\":\"" + email  + "\"" +
                (image != null ? ",\"image\":\"" + image + "\"" : "") +
                '}';
    }

    public void setImage(String path) {
        this.image = path;
    }

    public boolean suggestUser(User user) {
        return user.gender.acceptable(interest) && gender.acceptable(user.interest) && !blacklist.contains(user.id);
    }

    public void addNewMatch(int matchId, int userId) {
        this.matches.add(matchId);
        this.blacklist.add(userId); // to avoid suggesting a match
    }

    public Set<Integer> getMatches() {
        return matches;
    }
}
