package persistence;

// dilempar ketika file save ga bisa parsing atau formatnya ga valid
public class SaveCorruptedException extends Exception {

    public SaveCorruptedException(String message) {
        super(message);
    }

    public SaveCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}