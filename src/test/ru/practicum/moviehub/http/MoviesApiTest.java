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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesHandler moviesHandler;
    private Gson gson = new GsonBuilder()
            .create();

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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.equals("{}"), "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_returnsNotEmptyArray() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        movies.put(2, new Movie("2", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals(gson.toJson(movies), body);
    }

    @Test
    void getMovies_returnMovieWhenCorrectId() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies/1 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals(gson.toJson(movies.get(1)), body);
    }

    @Test
    void getMovies_returnMovieWhenIdNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/a"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies/a должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 400\nСообщение: Bad Request\nОписание: Некорректный ID", body);
    }

    @Test
    void getMovies_returnMovieWhenIdNotFound() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies/2 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 404\nСообщение: Not Found\nОписание: Фильм не найден", body);
    }

    @Test
    void getMovies_returnMovieWhenCorrectYear() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        movies.put(2, new Movie("2", 2001));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2000"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=2000 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Map<Integer, Movie> mov = new HashMap<>();
        mov.put(1, new Movie("1", 2000));

        String body = resp.body();
        assertEquals(gson.toJson(mov), body);
    }

    @Test
    void getMovies_returnMovieWhenYearIsNotInArray() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2001"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=2001 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("{}", body);
    }

    @Test
    void getMovies_returnMovieWhenYearIsNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=a"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies?year=a должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 400\nСообщение: Bad Request\nОписание: Некорректный параметр запроса — a", body);
    }

    @Test
    void deleteMovies_deleteMovieWhenCorrectId() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        movies.put(2, new Movie("2", 2001));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "DELETE /movies/1 должен вернуть 204");
    }

    @Test
    void deleteMovies_deleteMovieWhenIdNotNumber() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/a"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "DELETE /movies/a должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 400\nСообщение: Bad Request\nОписание: Некорректный ID", body);
    }

    @Test
    void deleteMovies_deleteMovieWhenIdNotFound() throws Exception {
        Map<Integer, Movie> movies = new HashMap<>();
        movies.put(1, new Movie("1", 2000));
        MoviesStore moviesStore = new MoviesStore(movies);

        moviesHandler.setMoviesStore(moviesStore);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "DELETE /movies/2 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 404\nСообщение: Not Found\nОписание: Фильм не найден", body);
    }

    @Test
    void postMovies_postMovieWhereCorrectData() throws Exception {
        moviesHandler.setMoviesStore(new MoviesStore());

        Movie movie = new Movie("1", 2000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .headers("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("{\"4480\":{\"title\":\"1\",\"releaseYear\":2000}}", body);
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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 422\nСообщение: Unprocessable Entity\nОписание: Название не должно быть пустым", body);
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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 422\nСообщение: Unprocessable Entity\nОписание: Название должно быть короче 100 символов", body);
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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 422\nСообщение: Unprocessable Entity\nОписание: Год должен быть между 1888 и 2026", body);
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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");

        String body = resp.body();
        assertEquals("Код ошибки: 415\nСообщение: Unsupported Media Type\nОписание: Неправильное значение Content-Type", body);
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

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "POST /movies должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        assertEquals("Код ошибки: 400\nСообщение: Bad Request\nОписание: Некорректный json", body);
    }
}