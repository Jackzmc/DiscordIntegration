package me.jackz.discordintegration;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.xml.ws.http.HTTPException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Util {
    public static UUID getUUID(String name) throws ParseException {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int statusCode = connection.getResponseCode();
            if(statusCode == 200) {
                JSONParser parser = new JSONParser();
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)){
                    JSONObject root = (JSONObject) parser.parse(reader);
                    String idString = root.get("id").toString();
                    if(idString != null) {
                        return UUID.fromString(idString.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
                    }else{
                        return null;
                    }
                }
            }else if(statusCode == 204) return null;
            else throw new HTTPException(statusCode);
        }
        catch (java.io.IOException e1) {
            return null;
        }
    }
    public static UUID stringToUUID(String id) {
        try {
            return UUID.fromString(id.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }catch(Exception ex) {
            return null;
        }
    }
}
