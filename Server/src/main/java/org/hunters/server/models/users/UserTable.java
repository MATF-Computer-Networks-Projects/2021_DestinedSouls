package org.hunters.server.models.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class UserTable {
    private Map<Integer, User> inMemTable;

    public UserTable() {
        this.inMemTable = new HashMap<>();
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

    public User getFirstThat(Predicate<User> pred) {
        for(var entry: inMemTable.values()) {
            if(pred.test(entry))
                return entry;
        }
        return null;
    }

    private final User[] emptyUserArray = new User[0];
    /**
     * @return first n users that satisfies predicate or all if less than n. Null returned for negative n.
     */
    public User[] getFirstNThat(int n, Predicate<User> pred) {
        if(n < 0) return null;
        if(n == 0) return emptyUserArray;

        var selected = new LinkedList<User>();
        for(var entry: inMemTable.values()) {
            if(pred.test(entry)) {
                selected.add(entry);
                if(selected.size() == n)
                    break;
            }
        }
        return selected.toArray(emptyUserArray);
    }

    /**
     * Selects n or less users that satisfies predicate and applies transformation
     */
    public User[] mapFirstNThat(int n, Predicate<User> pred, Function<User,User> map) {
        if(n < 0) return null;
        if(n == 0) return emptyUserArray;

        var selected = new LinkedList<User>();
        for(var entry : inMemTable.values()) {
            if(pred.test(entry)) {
                selected.add(map.apply(entry));
                if(selected.size() == n)
                    break;
            }
        }
        return selected.toArray(emptyUserArray);
    }

}
