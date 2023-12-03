package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class UserAPI {

    private static final String baseUrl = "https://jsonplaceholder.typicode.com/users";

    public String createUser(String userData) throws IOException {
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (Writer writer = new FileWriter("user.json")) {
            writer.write(userData);
        }

        return getResponse(connection);
    }

    public String updateUser(int userId, String updatedData) throws IOException {
        URL url = new URL(baseUrl + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        try (Writer writer = new FileWriter("updated_user.json")) {
            writer.write(updatedData);
        }

        return getResponse(connection);
    }

    public void deleteUser(int userId) throws IOException {
        URL url = new URL(baseUrl + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("User " + userId + " deleted successfully.");
        } else {
            System.out.println("Failed to delete user. Status code: " + responseCode);
        }
    }

    public String getAllUsers() throws IOException {
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public String getUserById(int userId) throws IOException {
        URL url = new URL(baseUrl + "/" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public String getUserByUsername(String username) throws IOException {
        URL url = new URL(baseUrl + "?username=" + username);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        return getResponse(connection);
    }

    public void getCommentsForLastPost(int userId) throws IOException {
        // Assuming the last post is the one with the highest id
        URL postUrl = new URL("https://jsonplaceholder.typicode.com/users/" + userId + "/posts");
        HttpURLConnection postConnection = (HttpURLConnection) postUrl.openConnection();
        postConnection.setRequestMethod("GET");

        String postsJson = getResponse(postConnection);

        // Parse postsJson to get the last post id
        int lastPostId = parseLastPostId(postsJson);

        // Get comments for the last post
        URL commentsUrl = new URL("https://jsonplaceholder.typicode.com/posts/" + lastPostId + "/comments");
        HttpURLConnection commentsConnection = (HttpURLConnection) commentsUrl.openConnection();
        commentsConnection.setRequestMethod("GET");

        String commentsJson = getResponse(commentsConnection);

        // Write comments to a file
        try (Writer writer = new FileWriter("user-" + userId + "-post-" + lastPostId + "-comments.json")) {
            writer.write(commentsJson);
        }

        System.out.println("Comments for user " + userId + ", post " + lastPostId + " written to file.");
    }

    private static String getResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                return response.toString();
            }
        } else {
            System.out.println("Failed to get response. Status code: " + responseCode);
            return null;
        }
    }

    private int parseLastPostId(String postsJson) {
        JsonArray postsArray = JsonParser.parseString(postsJson).getAsJsonArray();
        int lastPostId = 0;

        for (JsonElement postElement : postsArray) {
            JsonObject postObject = postElement.getAsJsonObject();
            int postId = postObject.get("id").getAsInt();

            if (postId > lastPostId) {
                lastPostId = postId;
            }
        }

        return lastPostId;
    }
    public static void getOpenTasksForUser(int userId) throws IOException {
        URL todosUrl = new URL(baseUrl + "/" + userId + "/todos");
        HttpURLConnection todosConnection = (HttpURLConnection) todosUrl.openConnection();
        todosConnection.setRequestMethod("GET");

        String todosJson = getResponse(todosConnection);

        if (todosJson != null) {
            JsonArray todosArray = JsonParser.parseString(todosJson).getAsJsonArray();
            System.out.println("Open tasks for user " + userId + ":");

            for (JsonElement todoElement : todosArray) {
                JsonObject todoObject = todoElement.getAsJsonObject();
                boolean completed = todoObject.get("completed").getAsBoolean();
                String title = todoObject.get("title").getAsString();

                if (!completed) {
                    System.out.println(" - " + title);
                }
            }
        } else {
            System.out.println("Failed to get tasks for user " + userId);
        }
    }

    public static void main(String[] args) {
        UserAPI userAPI = new UserAPI();

        try {
            // Example usage
            String newUserData = "{\"name\":\"John Doe\",\"username\":\"johndoe\",\"email\":\"johndoe@example.com\"}";
            userAPI.createUser(newUserData);

            // Assuming userId is known
            int userId = 3;

           // String updatedUserData = "{\"name\":\"Updated Name\",\"email\":\"updatedemail@example.com\"}";
            //userAPI.updateUser(userId, updatedUserData);

            userAPI.deleteUser(userId);

            String allUsers = userAPI.getAllUsers();
            System.out.println("All users: " + allUsers);

            String userById = userAPI.getUserById(userId);
            System.out.println("User by id: " + userById);

            String userByUsername = userAPI.getUserByUsername("Bret");
            System.out.println("User by username: " + userByUsername);

            userAPI.getCommentsForLastPost(userId);
            UserAPI.getOpenTasksForUser(userId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
