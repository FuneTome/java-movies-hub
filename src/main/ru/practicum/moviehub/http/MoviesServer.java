package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesHandler moviesHandler;

    public MoviesServer(MoviesStore moviesStore, int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            moviesHandler = new MoviesHandler();
            server.createContext("/movies", moviesHandler);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public MoviesHandler getMoviesHandler() {
        return moviesHandler;
    }
}