package org.hunters.server.services;

import org.hunters.server.utils.Json;

public class Validator {
    public static boolean validateSchema(Json json, String[] options) {
        for(var op : options) {
            if(!json.hasKey(op))
            {
                json.put("error", op);
                return false;
            }
        }
        return true;
    }
}
