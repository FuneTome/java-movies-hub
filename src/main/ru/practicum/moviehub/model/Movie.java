package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    final private String title;
    final private int releaseYear;

    public Movie(String title, int releaseYear) {
        this.title = title;
        this.releaseYear = releaseYear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, releaseYear);
    }

    public int getReleaseYear() {
        return releaseYear;
    }
}