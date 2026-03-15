package ru.practicum.moviehub.api;

public class ErrorResponse {
    private int errorCode;
    private String errorMessage;
    private String detailedDescription;

    public ErrorResponse(int errorCode, String errorMessage, String detailedDescription) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.detailedDescription = detailedDescription;
    }

    @Override
    public String toString() {
        return String.format("Код ошибки: %d\nСообщение: %s\nОписание: %s",
                errorCode, errorMessage, detailedDescription);
    }
}