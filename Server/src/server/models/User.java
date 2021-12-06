package server.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class User {
    public int id;
    public String name;
    public Date birthday;
    public Gender gender;
    public Gender interest;
    public String email;
    public byte[] hash;

    private static int idObj = 1;

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
            this.gender   = Genders.fromString(gender);
            this.interest = Genders.fromString(interest);
            this.hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | NoSuchAlgorithmException e) {
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

}
