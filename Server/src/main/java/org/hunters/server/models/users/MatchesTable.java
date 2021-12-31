package org.hunters.server.models.users;

import java.util.HashMap;

public class MatchesTable {
    private HashMap<Integer, UserPair> table;

    private static int idObj = 1;

    public MatchesTable() {
        this.table = new HashMap<>();
    }

    private class UserPair {
        public int user1;
        public int user2;

        public UserPair(int u1, int u2) {
            this.user1 = u1;
            this.user2 = u2;
        }

        public int getMatch(int id) {
            return user1 == id ? user2 : user1;
        }
    }

    private int getId() { // TODO: encode id
        return idObj++;
    }

    public int addMatch(int user1, int user2) {
        int id = getId();
        this.table.put(id, new UserPair(user1, user2));
        return id;
    }

    public int getMatch(int matchId, int userId) {
        return table.get(matchId).getMatch(userId);
    }
}
