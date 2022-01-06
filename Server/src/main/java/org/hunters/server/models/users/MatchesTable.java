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
            return user1 == id ? user2 : (user2 == id ? user1 : -2);
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

    /**
     * Get match's id from provided chatId and id of user sending a message.
     *
     * @return user id of match, or -1 if chat does not exist, or -2 if requesting user is not part of specified chat
     */
    public int getMatch(int chatId, int userId) {
        if(!table.containsKey(chatId))
            return -1;
        return table.get(chatId).getMatch(userId);
    }
}
