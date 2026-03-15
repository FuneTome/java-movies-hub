package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesHandler moviesHandler;
    private final Gson gson = new GsonBuilder().create();

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        server = new MoviesServer(new MoviesStore(), 8080);
        moviesHandler = server.getMoviesHandler();
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_returnsEmptyArray() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertEquals("[]", resp.body().trim(), "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_returnsNotEmptyArray() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        movies.put(2, new Movie(2, "2", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));

        List<Movie> expected = new ArrayList<>(movies.values());
        String expectedJson = gson.toJson(expected);
        assertEquals(expectedJson, resp.body());
    }

    @Test
    void getMovies_returnMovieWhenCorrectId() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertEquals(gson.toJson(movies.get(1)), resp.body());
    }

    @Test
    void getMovies_returnMovieWhenIdNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/a"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("ID должен быть числом"));
    }

    @Test
    void getMovies_returnMovieWhenIdNotFound() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Фильм с ID 2 не найден"));
    }

    @Test
    void getMovies_returnMovieWhenCorrectYear() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        movies.put(2, new Movie(2, "2", 2001));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2000"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));

        List<Movie> expected = List.of(new Movie(1, "1", 2000));
        assertEquals(gson.toJson(expected), resp.body());
    }

    @Test
    void getMovies_returnMovieWhenYearIsNotInArray() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2001"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertEquals("[]", resp.body());
    }

    @Test
    void getMovies_returnMovieWhenYearIsNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=a"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Год должен быть числом"));
    }

    @Test
    void deleteMovies_deleteMovieWhenCorrectId() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        movies.put(2, new Movie(2, "2", 2001));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode());
    }

    @Test
    void deleteMovies_deleteMovieWhenIdNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/a"))
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("ID должен быть числом"));
    }

    @Test
    void deleteMovies_deleteMovieWhenIdNotFound() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie(1, "1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);
        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Фильм с ID 2 не найден"));
    }

    @Test
    void postMovies_postMovieWhereCorrectData() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie inputMovie = new Movie("1", 2000);
        String inputJson = gson.toJson(inputMovie);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(inputJson))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));

        Movie expected = new Movie(1, "1", 2000);
        assertEquals(gson.toJson(expected), resp.body());
    }

    @Test
    void postMovies_postMovieWhereBlankTitle() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie movie = new Movie("", 2000);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Название не должно быть пустым"));
    }

    @Test
    void postMovies_postMovieWhereLongTitle() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie movie = new Movie("1".repeat(101), 2000);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Название должно быть короче 100 символов"));
    }

    @Test
    void postMovies_postMovieWhereIncorrectYear() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie movie = new Movie("1", 1887);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Год должен быть между 1888 и " + (LocalDate.now().getYear() + 1)));
    }

    @Test
    void postMovies_postMovieWhereIncorrectContentType() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie movie = new Movie("1", 2000);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .headers("Content-Type", "html/text")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode());
        assertTrue(resp.body().contains("Требуется Content-Type: application/json"));
    }

    @Test
    void postMovies_postMovieWhereIncorrectJson() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("titl", "1");
        jsonObject.addProperty("releaseYear", 2000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(jsonObject)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());
        assertEquals("application/json; charset=UTF-8", resp.headers().firstValue("Content-Type").orElse(""));
        assertTrue(resp.body().contains("Название не должно быть пустым"));
    }

    @Test
    void testWhenMethodNotAllowed() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", "1");
        jsonObject.addProperty("releaseYear", 2000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(jsonObject)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode());
    }
}