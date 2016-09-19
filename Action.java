package junctionbox;

/**
 * This enumeration contains a set of constants that represent actions that can be mapped to messages in Junctions.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 */
public enum Action {
    /**
     * Perform an action if the activation state has changed.
     */
    ACTIVATE,
    /**
     * Perform an action if the state of the toggle has changed.
     */
    TOGGLE,
    /**
     * Perform an action based on the value of the angle.
     */
    ROTATE,
    /**
     * Perform an action based on changing the angle with 1 Contact.
     */
    ROTATE_1,
    /**
     * Perform an action based on changing the angle with 2 Contacts.
     */
    ROTATE_2,
    /**
     * Perform an action based on the width and height scaling.
     */
    SCALE,
    /**
     * Perform an action based on the width scaling.
     */
    SCALE_WIDTH,
    /**
     * Perform an action based on the height scaling.
     */
    SCALE_HEIGHT,
    /**
     * Perform an action based on the value of x.
     */
    TRANSLATE_X,
    /**
     * Perform an action based on the value of y.
     */
    TRANSLATE_Y,
    /**
     * Perform an action based on both x and y values.
     */
    TRANSLATE,
    /**
     * Perform an action based on the x values of Contacts.
     */
    CONTACT_X,
    /**
     * Perform an action based on the y values of Contacts.
     */
    CONTACT_Y,
    /**
     * Perform an action based on both x and y values of Contacts.
     */
    CONTACT,
    /**
     * Perform an action based on the distance of a Contact from the center.
     */
    CONTACT_R,
    /**
     * The angle of a Contact relative to the center.
     */
    CONTACT_THETA,
    /**
     * The number of Contacts currently associated with a Junction.
     */
    COUNT_CONTACTS,
    /**
     * The number of rotations that a Junction has undergone.
     */
    COUNT_ROTATIONS
}
