package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private MoviesStore moviesStore;
    private final Gson gson = new GsonBuilder().create();

    public MoviesHandler() {
        this.moviesStore = new MoviesStore();
    }

    public void setMoviesStore(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

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
                sendError(ex, 405, "Method Not Allowed", "Метод не поддерживается");
        }
    }

    private void handleGetRequest(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getRawQuery();
        String[] pathParts = path.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("movies")) {
            if (query == null) {
                List<Movie> movies = moviesStore.getMovies();
                String json = gson.toJson(movies);
                sendJson(ex, 200, json);
            } else {
                String[] queryParts = query.split("=");
                if (queryParts.length == 2 && "year".equals(queryParts[0])) {
                    String yearStr = queryParts[1];
                    try {
                        int year = Integer.parseInt(yearStr);
                        if (year < 1888 || year > LocalDate.now().getYear() + 1) {
                            sendError(ex, 400, "Bad Request", "Некорректный год: " + yearStr);
                            return;
                        }
                        List<Movie> movies = moviesStore.getMoviesByYear(year);
                        String json = gson.toJson(movies);
                        sendJson(ex, 200, json);
                    } catch (NumberFormatException e) {
                        sendError(ex, 400, "Bad Request", "Год должен быть числом");
                    }
                } else {
                    sendError(ex, 400, "Bad Request", "Некорректный параметр запроса");
                }
            }
            return;
        }

        if (pathParts.length == 3 && pathParts[1].equals("movies")) {
            String idStr = pathParts[2];
            try {
                int id = Integer.parseInt(idStr);
                Movie movie = moviesStore.getMovie(id);
                if (movie == null) {
                    sendError(ex, 404, "Not Found", "Фильм с ID " + id + " не найден");
                } else {
                    String json = gson.toJson(movie);
                    sendJson(ex, 200, json);
                }
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Bad Request", "ID должен быть числом");
            }
            return;
        }

        sendError(ex, 400, "Bad Request", "Некорректный путь");
    }

    private void handlePostRequest(HttpExchange ex) throws IOException {
        List<String> ct = ex.getRequestHeaders().get("Content-Type");
        if (ct == null || !ct.getFirst().equalsIgnoreCase("application/json")) {
            sendError(ex, 415, "Unsupported Media Type", "Требуется Content-Type: application/json");
            return;
        }

        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (!(pathParts.length == 2 && pathParts[1].equals("movies"))) {
            sendError(ex, 400, "Bad Request", "Некорректный путь");
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes());
        Movie movie;
        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (JsonSyntaxException e) {
            sendError(ex, 400, "Bad Request", "Некорректный JSON");
            return;
        }

        String validationError = validateMovie(movie);
        if (validationError != null) {
            sendError(ex, 422, "Unprocessable Entity", validationError);
            return;
        }

        Movie created = moviesStore.addMovie(movie);
        String json = gson.toJson(created);
        sendJson(ex, 201, json);
    }

    private void handleDeleteRequest(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 3 && pathParts[1].equals("movies")) {
            String idStr = pathParts[2];
            try {
                int id = Integer.parseInt(idStr);
                boolean deleted = moviesStore.deleteMovie(id);
                if (deleted) {
                    sendNoContent(ex);
                } else {
                    sendError(ex, 404, "Not Found", "Фильм с ID " + id + " не найден");
                }
            } catch (NumberFormatException e) {
                sendError(ex, 400, "Bad Request", "ID должен быть числом");
            }
            return;
        }
        sendError(ex, 400, "Bad Request", "Некорректный путь");
    }

    private String validateMovie(Movie movie) {
        if (movie == null) {
            return "Тело запроса не может быть пустым";
        }
        String title = movie.getTitle();
        if (title == null || title.isBlank()) {
            return "Название не должно быть пустым";
        }
        if (title.length() > 100) {
            return "Название должно быть короче 100 символов";
        }
        int year = movie.getReleaseYear();
        int currentYear = LocalDate.now().getYear();
        if (year < 1888 || year > currentYear + 1) {
            return "Год должен быть между 1888 и " + (currentYear + 1);
        }
        return null;
    }
}