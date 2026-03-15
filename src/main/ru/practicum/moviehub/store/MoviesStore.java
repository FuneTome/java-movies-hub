package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public MoviesStore() {
    }

    public MoviesStore(Map<Integer, Movie> movies) {
        this.movies.putAll(movies);
        nextId = movies.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    public Movie addMovie(Movie movie) {
        movie.setId(nextId++);
        movies.put(movie.getId(), movie);
        return movie;
    }

    public List<Movie> getMovies() {
        return new ArrayList<>(movies.values());
    }

    public Movie getMovie(int id) {
        return movies.get(id);
    }

    public List<Movie> getMoviesByYear(int year) {
        List<Movie> result = new ArrayList<>();
        for (Movie movie : movies.values()) {
            if (movie.getReleaseYear() == year) {
                result.add(movie);
            }
        }
        return result;
    }

    public boolean deleteMovie(int id) {
        return movies.remove(id) != null;
    }
}