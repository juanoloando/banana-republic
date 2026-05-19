package model.resource;

// contract utk resource bank
public interface Bank {

    // returns jlh available cards of the resource type
    int getAvailable(ResourceType type);

    // returns true if the bank punya at least {amount} cards 
    boolean hasResource(ResourceType type, int amount);

    /**
     * takes { amount} cards of the given type from the bank
     * caller harus verify with {hasResource}
     * throwws InsufficientResourceException if cards kurang
     */
    void takeResource(ResourceType type, int amount);


    void returnResource(ResourceType type, int amount);

    // capacity sesuai spek
    default int getCapacity() {
        return 19;
    }
}
