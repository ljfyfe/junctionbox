package junctionbox;

/**
 * The Contact class represents a touch point on a two-dimensional grid. Contacts are dispatched by the Dispatcher
 * to Junctions to enable interactions.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 */
public class Contact {
    /**
     * The identifier.
     */
    private int id;
    
    /**
     * The x coordinate.
     */
    private float x;
    
    /**
     * The y coordinate.
     */
    private float y;
    
    /**
     * The first x coordinate.
     */
    private float firstX;
    
    /**
     * The first y coordinate.
     */
    private float firstY;
    
    /**
     * Constructs a new Contact with the specified coordinates.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Contact(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
        firstX = x;
        firstY = y;
    }
    
    /**
     * Returns the identifier.
     * 
     * @return the ID value
     */
    public int getID() {
        return id;
    }
    
    /**
     * Sets the x coordinate.
     * 
     * @param x the value of x
     */
    protected void setX(float x) {
        this.x = x;
    }
    
    /**
     * Sets the y coordinate.
     * 
     * @param y the value of y
     */
    protected void setY(float y) {
        this.y = y;
    }
    
    /**
     * Returns the x coordinate.
     * 
     * @return the value of x
     */
    public float getX() {
        return x;
    }
    
    /**
     * Returns the y coordinate.
     * 
     * @return the value of y
     */
    public float getY() {
        return y;
    }
    
    /**
     * Returns the first x coordinate set for this contact. The value of x can be updated and it can be useful to have the very first
     * value of x.
     * 
     * @return the first value of x
     */
    public float getFirstX() {
        return firstX;
    }
    
    /**
     * Returns the first y coordinate set for this contact. The value of y can be updated and it can be useful to have the very first
     * value of y.
     * 
     * @return the first value of y
     */
    public float getFirstY() {
        return firstY;
    }
}
