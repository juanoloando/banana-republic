package model.exception;

public class InvalidBuildException extends RuntimeException {
    public InvalidBuildException(String message) {
        super(message);
    }
}
