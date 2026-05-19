package model.exception;

import model.resource.ResourceType;


public class InsufficientResourceException extends RuntimeException {
    private final ResourceType resourceType;
    private final int required;
    private final int available;

    public InsufficientResourceException(ResourceType type, int required, int available) {
        super("Insufficient " + type.getDisplayName()
                + ": required " + required + ", available " + available);
        this.resourceType = type;
        this.required = required;
        this.available = available;
    }

    public ResourceType getResourceType() { return resourceType; }
    public int getRequired()  { return required; }
    public int getAvailable() { return available; }
}
