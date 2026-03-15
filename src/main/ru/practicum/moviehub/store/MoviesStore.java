package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ru.practicum.moviehub.model.Movie;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private Map<Integer, Movie> movies = new HashMap<>();
    private Gson gson = new GsonBuilder()
            .create();

    public MoviesStore() {
    }

    public MoviesStore(Map<Integer, Movie> movies) {
        this.movies = movies;
    }

    public String addMovie(JsonObject jsonObject, String body) {
        String title = jsonObject.get("title").getAsString();
        int year = jsonObject.get("releaseYear").getAsInt();
        Movie movie = gson.fromJson(body, Movie.class);
        movies.put(movie.hashCode(), movie);

        JsonObject result = new JsonObject();
        result.add(String.valueOf(movie.hashCode()), gson.toJsonTree(movie));
        return gson.toJson(result);
    }

    public String getMovies() {
        return gson.toJson(movies);
    }

    public String getMovies(String i) {
        try {
            int id = Integer.parseInt(i);
            Movie movie = movies.get(id);
            if (movie == null) {
                return "404";
            }
            return gson.toJson(movie);
        } catch (NumberFormatException e) {
            return "400";
        }
    }

    public String getMovieForYear(String y) {
        try {
            int year = Integer.parseInt(y);
            if (year < 1888 || year > LocalDate.now().getYear() + 1) {
                return "400";
            }
            Map<Integer, Movie> mov = new HashMap<>();
            for (Map.Entry<Integer, Movie> entry : movies.entrySet()) {
                   if (entry.getValue().getReleaseYear() == year) {
                       mov.put(entry.getKey(), entry.getValue());
                   }
            }
            return gson.toJson(mov);
        } catch (NumberFormatException e) {
            return "400";
        }
    }

    public String deleteMovie(String i) {
        try {
            int id = Integer.parseInt(i);
            if (movies.containsKey(id)) {
                movies.remove(id);
                return "204";
            } else {
                return "404";
            }
        } catch (NumberFormatException e) {
            return "400";
        }
    }
}