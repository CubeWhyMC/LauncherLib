package org.cubewhy.server;

import co.gongzh.procbridge.Server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

@SuppressWarnings("unused")
public class LoginService {
    public final Server server = new Server(28189, (method, args) -> method != null ? handleRequest(method, args) : null);
    public final Queue<String> queue = new LinkedList<>();

    /**
     * Start the server
     */
    public void startServer() throws InterruptedException {
        server.start(); // do start
    }

    public void addAccount(String authLink) {
        queue.add(authLink); // add a authLink
    }

    /**
     * Handles LunarClient requests
     *
     * @return json of callbackInfo
     */
    private HashMap<String, String> handleRequest(String method, Object args) {
        HashMap<String, String> result = new HashMap<>();
        if (method.equals("open-window")) {
            // Pop a token url
            String auth = queue.poll();
            if (auth == null) {
                result.put("status", "CLOSED_WITH_NO_URL");
            } else {
                result.put("status", "MATCHED_TARGET_URL");
                result.put("url", auth);
            }
        }
        return result;
    }
}
