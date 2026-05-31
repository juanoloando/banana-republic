package model.exception;

public class BuildLimitExceededException extends RuntimeException {
    public BuildLimitExceededException(String buildingType, int limit) {
        super("Build limit exceeded for " + buildingType + ": maximum is " + limit);
    }
}
