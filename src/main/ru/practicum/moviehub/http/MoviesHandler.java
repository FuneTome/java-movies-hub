package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MoviesHandler extends BaseHttpHandler {
    private MoviesStore moviesStore = new MoviesStore();
    private Gson gson = new GsonBuilder().create();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        switch (method) {
            case "GET":
                handleGetRequest(ex);
                break;
            case "POST":
                handlePostRequest(ex);
                break;
            case "DELETE":
                handleDeleteRequest(ex);
                break;
            default:
                sendJson(ex, 405, "Method Not Allowed");
                break;
        }
    }

    private void handleGetRequest(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getRawQuery();
        String[] queryParts = {""};
        if (query != null) {
            queryParts = query.split("=");
            query = queryParts[1];
        }
        String[] spl = ex.getRequestURI().getPath().split("/");

        if (spl.length == 2 && spl[1].equals("movies")) {
            if (query == null) {
                String json = moviesStore.getMovies();
                sendJson(ex, 200, json);
            } else if (queryParts[0].equals("year")) {
                String json = moviesStore.getMovieForYear(query);
                if (json.equals("400")) {
                    sendError(ex, 400, "Bad Request", "Некорректный параметр запроса — " + query);
                } else {
                    sendJson(ex, 200, json);
                }
            } else {
                sendError(ex, 400, "Bad Request", "Некорректный запрос");
            }
        } else if (spl.length == 3 && spl[1].equals("movies")) {
            String json = moviesStore.getMovies(spl[2]);
            if (json.equals("400")) {
                sendError(ex, 400, "Bad Request","Некорректный ID");
            } else if (json.equals("404")) {
                sendError(ex, 404, "Not Found", "Фильм не найден");
            } else {
                sendJson(ex, 200, json);
            }
        } else {
            sendError(ex, 400, "Bad Request", "Некорректный запрос");
        }
    }

    private void handlePostRequest(HttpExchange ex) throws IOException {
        String[] spl = ex.getRequestURI().getPath().split("/");
        Map<String, List<String>> headers = ex.getRequestHeaders();
        List<String> ct = headers.get("Content-Type");

        if (ct == null) {
            sendError(ex, 415, "Unsupported Media Type", "Отсутствует заголовок Content-Type");
            return;
        } else if (!ct.getFirst().equals("application/json")) {
            sendError(ex, 415, "Unsupported Media Type", "Неправильное значение Content-Type");
            return;
        }

        if (spl.length == 2 && spl[1].equals("movies")) {
            String body = new String(ex.getRequestBody().readAllBytes());
            String jsonObj = isValidTitleAndYear(body);

            if ("400".equals(jsonObj)) {
                sendError(ex, 400, "Bad Request", "Некорректный json");
            } else if ("blank title".equals(jsonObj)) {
                sendError(ex, 422, "Unprocessable Entity", "Название не должно быть пустым");
            } else if ("long title".equals(jsonObj)) {
                sendError(ex, 422, "Unprocessable Entity", "Название должно быть короче 100 символов");
            } else if ("invalid year".equals(jsonObj)) {
                sendError(ex, 422, "Unprocessable Entity", "Год должен быть между 1888 и 2026");
            } else {
                String json = moviesStore.addMovie(gson.fromJson(body, JsonObject.class), body);
                sendJson(ex, 201, json);
            }
        }
    }


    private void handleDeleteRequest(HttpExchange ex) throws IOException {
        String[] spl = ex.getRequestURI().getPath().split("/");
        if (spl.length == 3 && spl[1].equals("movies")) {
            String json = moviesStore.deleteMovie(spl[2]);
            if (json.equals("400")) {
                sendError(ex, 400, "Bad Request","Некорректный ID");
            } else if (json.equals("404")) {
                sendError(ex, 404, "Not Found", "Фильм не найден");
            } else {
                sendNoContent(ex);
            }
        } else {
            sendError(ex, 400, "Bad Request", "Некорректный запрос");
        }
    }

    public void setMoviesStore(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    public String isValidTitleAndYear(String body) {
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        if (jsonObject.has("title") && jsonObject.has("releaseYear")){
            String title = jsonObject.get("title").getAsString();
            int year = jsonObject.get("releaseYear").getAsInt();
            if (title.isBlank()) {
                return "blank title";
            } else if (title.length() > 100) {
                return "long title";
            } else if (year < 1888 || year > LocalDate.now().getYear() + 1) {
                return "invalid year";
            } else {
                return "ok";
            }
        } else {
            return "400";
        }
    }
}