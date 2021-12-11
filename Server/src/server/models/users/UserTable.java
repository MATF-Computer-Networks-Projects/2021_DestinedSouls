package server.models.users;

import java.util.*;

public class UserTable {
    private Map<Integer, User> inMemTable;

    public UserTable() {
        this.inMemTable = new HashMap<Integer, User>();
    }

    public void add(User user) {
        this.inMemTable.put(user.id, user);
    }

    public boolean hasId(int id) {
        return inMemTable.containsKey(id);
    }

    public User getById(int id) {
        return inMemTable.get(id);
    }

    public Collection<User> getAll() {
        return inMemTable.values();
    }
}