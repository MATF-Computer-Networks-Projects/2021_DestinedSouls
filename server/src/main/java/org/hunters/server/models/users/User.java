package org.hunters.server.models.users;

import org.hunters.server.models.ChatMessage;
import org.hunters.server.security.Authorizer;

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
    public String image = "placeholder.png";

    private final HashSet<Integer> blacklist = new HashSet<>();
    private final HashSet<Integer> matches   = new HashSet<>();
    private final HashSet<Integer> liked     = new HashSet<>();
    public  long  socketId = -1;

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

    public User( String name,
                 String birthday,
                 String gender,
                 String interest,
                 String email,
                 String password,
                 String image
                ) {
        this(name,
             formatDate(birthday),
             gender,
             interest,
             email,
             password
        );
        this.image = image;
    }

    public User(ArrayList<String> args){
        this(args.remove(0),
             args.remove(0),
             args.remove(0),
             args.remove(0),
             args.remove(0),
             args.remove(0)
            );
        if(!args.isEmpty())
            this.image = args.get(0);
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
                ",\"image\":\"" + image + "\"}";
    }

    public void setImage(String path) {
        this.image = path;
    }

    public boolean suggestUser(User user) {
        return  user.id != this.id
                && user.gender.acceptable(interest)
                && gender.acceptable(user.interest)
                && !blacklist.contains(user.id);
    }

    public void addNewMatch(int matchId, int userId) {
        this.matches.add(matchId);
        this.blacklist.add(userId); // to avoid suggesting a match
        this.liked.remove(userId);
    }

    public void blacklistUser(User user) {
        this.blacklist.add(user.id);
        if(!this.matches.remove(user.id))
            this.liked.remove(user.id);

        user.blacklist.add(this.id);
        if(!user.matches.remove(this.id))
            user.liked.remove(this.id);
    }

    public Set<Integer> getMatches() {
        return matches;
    }

    public void addLike(int userId) {
        this.liked.add(userId);
    }

    public boolean hasLiked(int userId) {
        return this.liked.contains(userId);
    }
}
