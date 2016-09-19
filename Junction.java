package junctionbox;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Vector;

/**
 * The Junction class defines a section of a touch interface that represents the intersection among Contacts (touches),
 * graphical output, and the sending of Open Sound Control (OSC) messages. A Junction is defined as a section of the interface by
 * providing a shape (rectangle or ellipse), the x and y coordinates of the center, and the width and height. Junctions do not have any
 * graphical output but the values for shape and area can be used to control graphical output. In addition to graphical output, variety
 * of actions such as translation, rotation, scaling, and others can be performed on Junctions.
 * Those actions can then be mapped to OSC messages.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 * @see <a href="http://opensoundcontrol.org/spec-1_0">The Open Sound Control 1.0 Specification</a>
 */
public class Junction {
	/**
	 * Constant for a rectangle shape, with rectangle as the default if no shape is specified.
	 */
	private static final int RECT = 0;
	
	/**
	 * Constant for an ellipse shape.
	 */
	private static final int ELLIPSE = 1;
	
    /**
     * Flag that determines whether a Junction can be rotated with 1 Contact. The default value is false.
     */
    private boolean rotatable1 = false;
    
    /**
     * Flag that determines whether a Junction can be rotated with 2 Contacs. The default value is false.
     */
    private boolean rotatable2 = false;
    
    /**
     * Flag that determines whether a Junction can be scaled. The default value is false.
     */
    private boolean scalable = false;
    
    /**
     * Flag that determines whether width can be scaled. The default value is true.
     */
    private boolean scalableWidth = true;
    
    /**
     * Flag that determines whether height can be scaled. The default value is true.
     */
    private boolean scalableHeight = true;
    
    /**
     * Flag that determines whether a Junction can be translated. The default value is false.
     */
    private boolean translatable = false;
    
    /**
     * The minimum number of Contacts to use for translation.
     */
    private int minTranslationContacts = 0;
    
    /**
     * The maximum number of Contacts to use for translation.
     */
    private int maxTranslationContacts = 0;
    
    /**
     * Flag that determines whether translation in the x direction is allowed. The default value is true.
     */
    private boolean translatableX = true;
    
    /**
     * Flag that determines whether translation in the y direction is allowed. The default value is true.
     */
    private boolean translatableY = true;
    
    /**
     * Flag that determines whether this Junction responds to Contact events. The default value is true.
     */
    private boolean live = true;
    
    /**
     * The master Contact map, index by the unique identifier associated with each Contact.
     */
    private ConcurrentHashMap<Integer,Contact> contactMap;
    
    /**
     * The master list of subjunctions.
     */
    private CopyOnWriteArrayList<Junction> junctionList;
    
    /**
     * The shape of this Junction.
     */
    private int shape;
    
    /**
     * The label given to this Junction.
     */
    private String label = "";
    
    /**
     * The center x coordinate.
     */
    private float centerX;
    
    /**
     * The center y coordinate.
     */
    private float centerY;
    
    /**
     * The width. The default value is 100.
     */
    private float width = 100.0f;
    
    /**
     * The height. The default value is 100.
     */
    private float height = 100.0f;
    
    /**
     * Convenience constant for two times Pi.
     */
    private static final float TWO_PI = (float)(2.0*Math.PI);
    
    /**
     * The angle. The default value is 0.
     */
    private float angle = 0.0f;
    
    /**
     * The current toggle state.
     */
    private boolean toggleOn = false;
    
    /**
     * The last number of rotations that this Junction underwent.
     */
    private int lastRotationCount = 0;
    
    /**
     * The number of rotations that this Junction has undergone.
     */
    private int rotationCount = 0;
    
    /**
     * Contact count for comparing with the current count.
     */
    private int lastContactCount = 0;
    
    /**
     * Whether minimum and maximum angle values should be used.
     */
    private boolean limitAngle = false;
    
    /**
     * The minimum value for rotation.
     */
    private float minAngle;
    
    /**
     * The maximum value for rotation.
     */
    private float maxAngle;
    
    /**
     * The minimum value for translation in the x direction.
     */
    private float minTranslateX;
    
    /**
     * The maximum value for translation in the x direction.
     */
    private float maxTranslateX;
    
    /**
     * The minimum value for translation in the y direction.
     */
    private float minTranslateY;
    
    /**
     * The maximum value for translation in the y direction.
     */
    private float maxTranslateY;
    
    /**
     * The minimum width value.
     */
    private float minWidth;
    
    /**
     * The maximum width value.
     */
    private float maxWidth;
    
    /**
     * The minimum height value.
     */
    private float minHeight;
    
    /**
     * The maximum height value.
     */
    private float maxHeight;
    
    /**
     * The IP address of the target.
     */
    private String targetAddress = "";
    
    /**
     * The port of the target.
     */
    private int targetPort;
    
    /**
     * The Relay used to send messages to the target.
     */
    private Relay targetRelay;
    
    /**
     * Determines whether to relay the active versus non-active state.
     */
    private boolean relayActive = false;
    
    /**
     * Determines whether to relay the current state of the toggle.
     */
    private boolean relayToggle = false;
    
    /**
     * Determines whether to relay the rotation angle.
     */
    private boolean relayRotate = false;
    
    /**
     * Determines whether to relay the rotation angle with 1 Contact.
     */
    private boolean relayRotate1 = false;
    
    /**
     * Determines whether to relay the rotation angle with 2 Contacts.
     */
    private boolean relayRotate2 = false;
    
    /**
     * Determines whether to relay the current scale value.
     */
    private boolean relayScale = false;
    
    /**
     * Determines whether to relay the current width value.
     */
    private boolean relayScaleWidth = false;
    
    /**
     * Determines whether to relay the current height value.
     */
    private boolean relayScaleHeight = false;
    
    /**
     * Determines whether to relay the current center x value.
     */
    private boolean relayTranslateX = false;
    
    /**
     * Determines whether to relay the current center y value.
     */
    private boolean relayTranslateY = false;
    
    /**
     * Determines whether to relay the current center x,y values in a single message.
     */
    private boolean relayTranslateXY = false;
    
    /**
     * Determines whether to relay contact x values.
     */
    private boolean relayContactX = false;
    
    /**
     * Determines whether to relay contact y values.
     */
    private boolean relayContactY = false;
    
    /**
     * Determines whether to relay both contact x and y values.
     */
    private boolean relayContact = false;
    
    /**
     * Determines whether to relay contact r values.
     */
    private boolean relayContactR = false;
    
    /**
     * Determines whether to relay contact theta values.
     */
    private boolean relayContactTheta = false;
    
    /**
     * Determines whether to relay the number of Contacts.
     */
    private boolean relayContactCount = false;
    
    /**
     * Determines whether to relay the number of Rotations.
     */
    private boolean relayRotationCount = false;
    
    /**
     * The list of messages that will send the current active state value.
     */
    private Vector<String> activeList;
    
    /**
     * The list of messages that will send the current toggle value.
     */
    private Vector<String> toggleList;
    
    /**
     * The list of messages that will send the current rotation angle.
     */
    private Vector<String> rotateList;
    
    /**
     * The list of messages that will send the current rotation angle with 1 Contact.
     */
    private Vector<String> rotate1List;
    
    /**
     * The list of messages that will send the current rotation angle with 2 Contacts.
     */
    private Vector<String> rotate2List;
    
    /**
     * The list of messages that will send the current scale value.
     */
    private Vector<String> scaleList;
    
    /**
     * The list of messages that will send the current width value.
     */
    private Vector<String> scaleWidthList;
    
    /**
     * The list of messages that will send the current height value.
     */
    private Vector<String> scaleHeightList;
    
    /**
     * The list of Relays that will send the current center x value.
     */
    private Vector<String> translateXList;
    
    /**
     * The list of Relays that will send the current center y value.
     */
    private Vector<String> translateYList;
    
    /**
     * The list of Relays that will send the current center x,y values.
     */
    private Vector<String> translateXYList;
    
    /**
     * The list of Relays that will send contact x values.
     */
    private Vector<String> contactXList;
    
    /**
     * The list of Relays that will send contact y values.
     */
    private Vector<String> contactYList;
    
    /**
     * The list of Relays that will send both contact x and y values.
     */
    private Vector<String> contactList;
    
    /**
     * The list of messages that send contact r values.
     */
    private Vector<String> contactRList;
    
    /**
     * The list of messages that send contact theta values.
     */
    private Vector<String> contactThetaList;
    
    /**
     * The list of Relays that will send the current Contact count.
     */
    private Vector<String> contactCountList;
    
    /**
     * The list of Relays that will send the current Rotation count.
     */
    private Vector<String> rotationCountList;
    
    /**
     * The old distance between two Contacts. Used for the scaling gesture.
     */
    private float oldContactDist = 0;
    
    /**
     * The new distance between two Contacts. Used for the scaling gesture.
     */
    private float newContactDist = 0;
    
    /**
     * The old angle between two Contacts. Used for the 2-Contact rotation gesture.
     */
    private float oldContactTheta = 0;
    
    /**
     * The new angle between two Contacts. Used for the 2-Contact rotation gesture.
     */
    private float newContactTheta = 0;
    
    /**
     * Determines whether interactions with this Junction should be recorded.
     */
    private boolean recordable = false;
    
    /**
     * Determines whether Junction state should be saved.
     */
    private boolean savable = false;
    
    /**
     * Constructs a new Junction with the specified values for box width, box height, center x, center y, width, and height.
     * The box width and height refer to the total size of the touch interface.
     * Typically, this would be the size of the screen and should match the size set for the originator of TUIO messages.
     * 
     * @param bw the box width
     * @param bh the box height
     * @param x the x coordinate of the center point
     * @param y the y coordinate of the center point
     * @param w the width
     * @param h the height
     */
    public Junction(float bw, float bh, float x, float y, float w, float h) {
        contactMap = new ConcurrentHashMap<Integer,Contact>();
        junctionList = new CopyOnWriteArrayList<Junction>();
        
        // Set internal values
        centerX = x;
        centerY = y;
        width = w;
        height = h;
        
        // Create a rectangular junction by default
        shape = RECT;
        
        // Create message lists
        activeList = new Vector<String>();
        toggleList = new Vector<String>();
        rotateList = new Vector<String>();
        rotate1List = new Vector<String>();
        rotate2List = new Vector<String>();
        scaleList = new Vector<String>();
        scaleWidthList = new Vector<String>();
        scaleHeightList = new Vector<String>();
        translateXList = new Vector<String>();
        translateYList = new Vector<String>();
        translateXYList = new Vector<String>();
        contactXList = new Vector<String>();
        contactYList = new Vector<String>();
        contactList = new Vector<String>();
        contactRList = new Vector<String>();
        contactThetaList = new Vector<String>();
        contactCountList = new Vector<String>();
        rotationCountList = new Vector<String>();
        
        // Set default translation limits
        minTranslateX = 0.0f;
        maxTranslateX = bw;
        minTranslateY = 0.0f;
        maxTranslateY = bh;
        
        // Set default size limits
        minWidth = 1.0f;
        maxWidth = bw;
        minHeight = 1.0f;
        maxHeight = bh;
    }
    
    /**
     * Determines whether rotation is allowed. Setting this to true will enable 1 and 2 Contact rotations.
     * 
     * @param r true to allow rotation, false otherwise
     */
    public void allowRotation(boolean r) {
        rotatable1 = r;
        rotatable2 = r;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowRotation(r);
            }
        }
    }
    
    /**
     * Determines whether rotation is allowed by setting 1 and 2 Contact rotations separately.
     * 
     * @param r1
     * @param r2
     */
    public void allowRotation(boolean r1, boolean r2) {
        rotatable1 = r1;
        rotatable2 = r2;
    }
    
    /**
     * Determines whether rotation is allowed and how many Contacts are used. Values for Contact count are 1 or 2.
     * 
     * @param r true to allow rotation, false otherwise
     * @param c either 1 or 2
     */
    public void allowRotation(boolean r, int c) {
        if (c == 1) {
            rotatable1 = r;
        }
        
        if (c == 2) {
            rotatable2 = r;
        }
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowRotation(rotatable1, rotatable2);
            }
        }
    }
    
    /**
     * Determines whether scaling is allowed.
     * 
     * @param s true to allow scaling, false otherwise
     */
    public void allowScaling(boolean s) {
        scalable = s;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowScaling(s);
            }
        }
    }
    
    /**
     * Determines whether width scaling is allowed.
     * 
     * @param sw
     */
    public void allowScalingWidth(boolean sw) {
        scalableWidth = sw;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowScalingWidth(sw);
            }
        }
    }
    
    /**
     * Determines whether height scaling is allowed.
     * 
     * @param sh
     */
    public void allowScalingHeight(boolean sh) {
        scalableHeight = sh;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowScalingHeight(sh);
            }
        }
    }
    
    /**
     * Allows translation with 1 or 2 Contacts.
     * 
     * @param t true to allow translation, false otherwise
     */
    public void allowTranslation(boolean t) {
        translatable = t;
        
        minTranslationContacts = 1;
        maxTranslationContacts = 2;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowTranslation(t);
            }
        }
    }
    
    /**
     * Allows translation with the specified number of Contacts.
     * 
     * @param t true to allow translation, false otherwise
     * @param c the number of Contacts to translate
     */
    public void allowTranslation(boolean t, int c) {
        translatable = t;
        
        if (c > 0) {
            minTranslationContacts = c;
            maxTranslationContacts = c;
            
            if (!junctionList.isEmpty()) {
                for (Junction j : junctionList) {
                    j.allowTranslation(t, c);
                }
            }
        }
    }
    
    /**
     * Allows translation with Contacts in the specified range.
     * 
     * @param t true to allow translation, false otherwise
     * @param min the minimum number of Contacts for translation
     * @param max the maximum number of Contacts for translation
     */
    public void allowTranslation(boolean t, int min, int max) {
        translatable = t;
        
        if (min > 0 && max > 0) {
            minTranslationContacts = min;
            maxTranslationContacts = max;
            
            if (!junctionList.isEmpty()) {
                for (Junction j : junctionList) {
                    j.allowTranslation(t, min, max);
                }
            }
        }
    }
    
    /**
     * Determines whether x translation is allowed.
     * 
     * @param tx true to allow x translation, false otherwise
     */
    public void allowTranslationX(boolean tx) {
        translatableX = tx;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowTranslationX(tx);
            }
        }
    }
    
    /**
     * Determines whether y translation is allowed.
     * 
     * @param ty true to allow y translation, false otherwise
     */
    public void allowTranslationY(boolean ty) {
        translatableY = ty;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.allowTranslationY(ty);
            }
        }
    }
    
    /**
     * Sets the live status for this Junction. A live Junction will receive Contacts while a non-live Junction will not. Junctions are
     * live by default. Subjunctions inherit this status.
     * 
     * @param live
     */
    public void beLive(boolean live) {
        this.live = live;
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.beLive(this.live);
            }
        }
    }
    
    /**
     * Returns the live status of this Junction.
     * 
     * @return true if this Junction is live, false otherwise
     */
    public boolean isLive() {
        return live;
    }
    
    /**
     * Sets the shape for this Junction. The default shape is a rectangle.
     * 
     * @param shape the shape of the Junction
     */
    public void setShape(int shape) {
        // Set a shape for the junction to determine whether contacts fall inside of it
        this.shape = shape;
    }
    
    /**
     * Returns the shape of this Junction as an integer. Values for shape are represented in Processing by the constants RECT and ELLIPSE
     * for rectangle and ellipse respectively.
     * 
     * @return the int representing the shape
     */
    public int getShape() {
        return shape;
    }
    
    /**
     * Sets the label for this Junction.
     * 
     * @param l the label String
     */
    public void setLabel(String l) {
        label = l;
    }
    
    /**
     * Gets the label for this Junction.
     * 
     * @return the label String or an empty String if no label is set
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Sets the target for messages sent from this Junction. If a target has been provided to the Dispatcher, calling this method will
     * override that setting.
     * 
     * @param address the IP address of the target
     * @param port the port number of the target
     */
    public void setTarget(String address, int port) {
        targetAddress = address;
        targetPort = port;
        targetRelay = new Relay(targetAddress, targetPort);
    }
    
    /**
     * Sets the target to the specified Relay.
     * 
     * @param r the Relay to use for the target
     */
    public void setTarget(Relay r) {
        targetRelay = r;
        targetAddress = targetRelay.getIPAddress();
        targetPort = targetRelay.getPort();
    }
    
    /**
     * Returns the target IP address.
     * 
     * @return a String representing the IP address
     */
    public String getTargetAddress() {
        return targetAddress;
    }
    
    /**
     * Returns the target port.
     * 
     * @return an int representing the port
     */
    public int getTargetPort() {
        return targetPort;
    }
    
    /**
     * Sets the x and y coordinates for the center point of this Junction.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void setCenter(float x, float y) {
        setCenterX(x);
        setCenterY(y);
        
        if (relayTranslateXY) {
            if (targetRelay != null) {
                for (String s : translateXYList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addFloat(s, normal(centerX, minTranslateX, maxTranslateX));
                    targetRelay.addFloat(s, normal(centerY, minTranslateY, maxTranslateY));
                    targetRelay.send(s);
                }
            }
        }
    }
    
    /**
     * Changes the x and y coordinates for the center point of this Junction.
     * 
     * @param dx the amount to change x
     * @param dy the amount to change y
     */
    public void changeCenter(float dx, float dy) {
        setCenter(centerX + dx, centerY + dy);
    }
    
    /**
     * Sets the x coordinate for the center point of this Junction.
     * 
     * @param x the value of x
     */
    public void setCenterX(float x) {
        if (translatableX) {
            float holdX = centerX;
            
            if (x > maxTranslateX) {
                centerX = maxTranslateX;
            }
            else if (x < minTranslateX) {
                centerX = minTranslateX;
            }
            else {
                centerX = x;
            }
            
            if (centerX != holdX) {
                if (relayTranslateX) {
                    // Send changes in x
                    if (targetRelay != null) {
                        for (String s : translateXList) {
                            targetRelay.resetMessage(s);
                            targetRelay.addFloat(s, normal(centerX, minTranslateX, maxTranslateX));
                            targetRelay.send(s);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Change the x coordinate for the center of this Junction by adding or subtracting the specified amount.
     * 
     * @param dx the amount to be added or subtracted from the center x value
     */
    public void changeCenterX(float dx) {
        setCenterX(centerX + dx);
    }
    
    /**
     * Sets the y coordinate for the center point of this Junction.
     * 
     * @param y the value of y
     */
    public void setCenterY(float y) {
        if (translatableY) {
            float holdY = centerY;
            
            if (y > maxTranslateY) {
                centerY = maxTranslateY;
            }
            else if (y < minTranslateY) {
                centerY = minTranslateY;
            }
            else {
                centerY = y;
            }
            
            if (centerY != holdY) {
                if (relayTranslateY) {
                    // Send changes in y
                    if (targetRelay != null) {
                        for (String s : translateYList) {
                            targetRelay.resetMessage(s);
                            targetRelay.addFloat(s, normal(centerY, minTranslateY, maxTranslateY));
                            targetRelay.send(s);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Change the x coordinate for the center of this Junction by adding or subtracting the specified amount.
     * 
     * @param dy the amount to be added or subtracted from the center y value
     */
    public void changeCenterY(float dy) {
        setCenterY(centerY + dy);
    }
    
    /**
     * Sets the width of this Junction.
     * 
     * @param w the width value
     */
    public void setWidth(float w) {
        if (scalableWidth) {
            float holdWidth = width;
            
            if (w > maxWidth) {
                width = maxWidth;
            }
            else if (w < minWidth) {
                width = minWidth;
            }
            else {
                // Check height to preserve proportion
                if (w > width) {
                    if (height < maxHeight) {
                        width = w;
                    }
                }
                else if (w < width) {
                    if (height > minHeight) {
                        width = w;
                    }
                }
            }
            
            if (width != holdWidth) {
                if (relayScaleWidth) {
                    if (targetRelay != null) {
                        for (String s : scaleWidthList) {
                            targetRelay.resetMessage(s);
                            targetRelay.addFloat(s, normal(width, minWidth, maxWidth));
                            targetRelay.send(s);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Change the width by adding the specified amount.
     * 
     * @param dw the amount to add to the width
     */
    public void changeWidth(float dw) {
        setWidth(width + dw);
    }
    
    /**
     * Sets the height of this Junction.
     * 
     * @param h the height value
     */
    public void setHeight(float h) {
        if (scalableHeight) {
            float holdHeight = height;
            
            if (h > maxHeight) {
                height = maxHeight;
            }
            else if (h < minHeight) {
                height = minHeight;
            }
            else {
                // Check width to preserve proportion
                if (h > height) {
                    if (width < maxWidth) {
                        height = h;
                    }
                }
                else if (h < height) {
                    if (width > minWidth) {
                        height = h;
                    }
                }
            }
            
            if (height != holdHeight) {
                if (relayScaleHeight) {
                    if (targetRelay != null) {
                        for (String s : scaleHeightList) {
                            targetRelay.resetMessage(s);
                            targetRelay.addFloat(s, normal(height, minHeight, maxHeight));
                            targetRelay.send(s);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Change the height by adding the specified amount.
     * 
     * @param dh the amount to add to the height
     */
    public void changeHeight(float dh) {
        setHeight(height + dh);
    }
    
    /**
     * Sets the width and height of this Junction with a single method.
     * 
     * @param w the new width
     * @param h the new height
     */
    public void setScale(float w, float h) {
        setWidth(w);
        setHeight(h);
        
        // Send scale as a normalized ratio of areas
        if (relayScale) {
            if (targetRelay != null) {
                for (String s : scaleList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addFloat(s, normal(width, minWidth, maxWidth));
                    targetRelay.addFloat(s, normal(height, minHeight, maxHeight));
                    targetRelay.send(s);
                }
            }
        }
    }
    
    /**
     * Changes the width and height of this Junction with a single method.
     * 
     * @param dw the amount to add to the width
     * @param dh the amount to add to the height
     */
    public void changeScale(float dw, float dh) {
        setScale(width + dw, height + dh);
    }
    
    /**
     * Sets the angle of this Junction.
     * 
     * @param a the angle value
     */
    public void setAngle(float a) {
        if (rotatable1 || rotatable2) {
            // Hold values to test for changes
            float holdAngle = angle;
            int holdRotationCount = rotationCount;
            
            if (a > TWO_PI) {
                angle = a - TWO_PI;
                rotationCount += 1;
            }
            else if (a < (0 - TWO_PI)) {
                angle = a + TWO_PI;
                rotationCount -= 1;
            }
            else {
                angle = a;
            }
            
            // Only send messages if angle value has changed
            if (angle != holdAngle) {
                if (relayRotate) {
                    if (targetRelay != null) {
                        for (String s : rotateList) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
                
                if (relayRotate1 && getContactCount() == 1) {
                    if (targetRelay != null) {
                        for (String s : rotate1List) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
                
                if (relayRotate2 && getContactCount() == 2) {
                    if (targetRelay != null) {
                        for (String s : rotate2List) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
            }
            
            // Only send messages if rotation count has changed
            if (rotationCount != holdRotationCount) {
                if (relayRotationCount) {
                    relayRotationCount();
                }
            }
        }
    }
    
    private void setAngle(float a, int contactCount) {
        if (rotatable1 || rotatable2) {
            // Hold values to test for changes
            float holdAngle = angle;
            int holdRotationCount = rotationCount;
            
            if (a > TWO_PI) {
                angle = a - TWO_PI;
                rotationCount += 1;
            }
            else if (a < (0 - TWO_PI)) {
                angle = a + TWO_PI;
                rotationCount -= 1;
            }
            else {
                angle = a;
            }
            
            // Only send messages if angle value has changed
            if (angle != holdAngle) {
                if (relayRotate) {
                    if (targetRelay != null) {
                        for (String s : rotateList) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
                
                if (relayRotate1 && contactCount == 1) {
                    if (targetRelay != null) {
                        for (String s : rotate1List) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
                
                if (relayRotate2 && contactCount == 2) {
                    if (targetRelay != null) {
                        for (String s : rotate2List) {
                            targetRelay.resetMessage(s);
                            if (limitAngle) {
                                targetRelay.addFloat(s, normal(angle, minAngle, maxAngle));
                            }
                            else {
                                targetRelay.addFloat(s, normal(angle, 0.0f, TWO_PI));
                            }
                            targetRelay.send(s);
                        }
                    }
                }
            }
            
            // Only send messages if rotation count has changed
            if (rotationCount != holdRotationCount) {
                if (relayRotationCount) {
                    relayRotationCount();
                }
            }
        }
    }
    
    /**
     * Changes the angle of this Junction.
     * 
     * @param da the amount to change the angle in radians
     */
    public void changeAngle(float da) {
        if (rotatable1 || rotatable2) {
            if (limitAngle) {
                if (angle + da < minAngle) {
                    setAngle(minAngle);
                }
                else if (angle + da > maxAngle) {
                    setAngle(maxAngle);
                }
                else {
                    setAngle(angle + da);
                }
            }
            else {
                setAngle(angle + da);
            }
        }
    }
    
    private void changeAngle(float da, int contactCount) {
        if (rotatable1 || rotatable2) {
            if (limitAngle) {
                if (angle + da < minAngle) {
                    setAngle(minAngle, contactCount);
                }
                else if (angle + da > maxAngle) {
                    setAngle(maxAngle, contactCount);
                }
                else {
                    setAngle(angle + da, contactCount);
                }
            }
            else {
                setAngle(angle + da, contactCount);
            }
        }
    }
    
    /**
     * Sets minimum and maximum vales for scaling width.
     * 
     * @param min the minimum value for width scaling
     * @param max the maximum value for width scaling
     */
    public void limitScalingWidth(float min, float max) {
        minWidth = min;
        maxWidth = max;
    }
    
    /**
     * Sets minimum and maximum vales for scaling height.
     * 
     * @param min the minimum value for height scaling
     * @param max the maximum value for height scaling
     */
    public void limitScalingHeight(float min, float max) {
        minHeight = min;
        maxHeight = max;
    }
    
    /**
     * Sets minimum and maximum vales for translation in the x direction. The default minimum is 0 and the default
     * maximum is the value of the box width for this Junction.
     * 
     * @param min the minimum value for x translation
     * @param max the maximum value for x translation
     */
    public void limitTranslationX(float min, float max) {
        minTranslateX = min;
        maxTranslateX = max;
        
        // Check for subjunctions that inherit values from this Junction
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.limitTranslationX(min, max);
            }
        }
    }
    
    /**
     * Sets minimum and maximum values for translation in the y direction. The default minimum is 0 and the default
     * maximum is the value of the box height for this Junction.
     * 
     * @param min the minimum value for y translation
     * @param max the maximum value for y translation
     */
    public void limitTranslationY(float min, float max) {
        minTranslateY = min;
        maxTranslateY = max;
        
        // Check for subjunctions that inherit values from this Junction
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.limitTranslationY(min, max);
            }
        }
    }
    
    /**
     * Sets a minimum and maximum value for the rotation angle of this Junctions.
     * 
     * @param min the minimum angle
     * @param max the maximum angle
     */
    public void limitRotation(float min, float max) {
        minAngle = min;
        maxAngle = max;
        limitAngle = true;
        
        // Check for subjunctions that inherit values from this Junction
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.limitRotation(min, max);
            }
        }
    }
    
    /**
     * Returns the x coordinate of the current center point.
     * 
     * @return the current x coordinate of the center
     */
    public float getCenterX() {
        return centerX;
    }
    
    /**
     * Returns the y coordinate of the current center point.
     * 
     * @return the current y coordinate of the center
     */
    public float getCenterY() {
        return centerY;
    }
    
    /**
     * Returns the current width.
     * 
     * @return the width value
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Returns the current height.
     * 
     * @return the height value
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Returns the current angle.
     * 
     * @return the angle in radians
     */
    public float getAngle() {
        return angle;
    }
    
    /**
     * Returns the number of rotations that this Junction has undergone. Counterclockwise rotations will subtract from this value with
     * one counterclockwise rotation subtracting 1, two counterclockwise rotations subtracting 2, etc.
     * 
     * @return the number of rotations
     */
    public int getRotationCount() {
        return rotationCount;
    }
    
    /**
     * Returns the minimum width allowed for scaling.
     * 
     * @return the minimum width
     */
    public float getMinScalingWidth() {
        return minWidth;
    }
    
    /**
     * Returns the maximum width allowed for scaling.
     * 
     * @return the maximum width
     */
    public float getMaxScalingWidth() {
        return maxWidth;
    }
    
    /**
     * Returns the minimum height allowed for scaling.
     * 
     * @return the minimum height
     */
    public float getMinScalingHeight() {
        return minHeight;
    }
    
    /**
     * Returns the maximum height allowed for scaling.
     * 
     * @return the maximum height
     */
    public float getMaxScalingHeight() {
        return maxHeight;
    }
    
    /**
     * Returns the minimum value set for translation in the x direction.
     * 
     * @return the minimum value for x translation
     */
    public float getMinTranslationX() {
        return minTranslateX;
    }
    
    /**
     * Returns the maximum value set for translation in the x direction.
     * 
     * @return the maximum value for x translation
     */
    public float getMaxTranslationX() {
        return maxTranslateX;
    }
    
    /**
     * Returns the minimum value set for translation in the y direction.
     * 
     * @return the minimum value for y translation
     */
    public float getMinTranslationY() {
        return minTranslateY;
    }
    
    /**
     * Returns the maximum value set for translation in the y direction.
     * 
     * @return the maximum value for y translation
     */
    public float getMaxTranslationY() {
        return maxTranslateY;
    }
    
    /**
     * Sets the current state of the toggle boolean.
     * 
     * @param t a boolean representing the current toggle state
     */
    public void setToggle(boolean t) {
        toggleOn = t;
    }
    
    /**
     * Returns the current state of a boolean. The initial state of the toggle is off/false.
     * 
     * @return true if the toggle is on, false if it is off
     */
    public boolean getToggle() {
        return toggleOn;
    }
    
    /**
     * Adds a junction to this Junction.
     * 
     * @param j junction to be added
     */
    public void addJunction(Junction j) {
        // Junctions inherit behavior from the parent
        j.allowRotation(rotatable1, rotatable2);
        
        if (limitAngle) {
            j.limitRotation(minAngle, maxAngle);
        }
        
        j.allowScaling(scalable);
        j.allowScalingWidth(scalableWidth);
        j.allowScalingHeight(scalableHeight);
        j.limitScalingWidth(minWidth, maxWidth);
        j.limitScalingHeight(minHeight, maxHeight);
        
        j.allowTranslation(translatable, minTranslationContacts, maxTranslationContacts);
        j.allowTranslationX(translatableX);
        j.allowTranslationY(translatableY);
        j.limitTranslationX(minTranslateX, maxTranslateX);
        j.limitTranslationY(minTranslateY, maxTranslateY);
        
        junctionList.add(j);
    }
    
    /**
     * Removes the specified junction from this Junction.
     * 
     * @param j junction to be removed
     */
    public void removeJunction(Junction j) {
        if (junctionList.contains(j)) {
            junctionList.remove(j);
        }
    }
    
    /**
     * Updates values that are inherited by any subjunctions added to this Junction. Inherited values include the center location, the rotation angle
     * and the scale. The actual inherited values are not passed on to subjuctions. Instead, only the change in the values is sent.
     * 
     * @param dx the change in the center x location
     * @param dy the change in the center y location
     * @param da the change in the angle
     * @param dw the change in the width
     * @param dh the change in the height
     */
    protected void updateJunction(float dx, float dy, float da, float dw, float dh, int cc) {
        // Check for inherited behaviors
        if (translatable && (cc >= minTranslationContacts && cc <= maxTranslationContacts)) {
            if (cc > 1) {
                changeCenter(dx/(float)cc, dy/(float)cc);
            }
            else {
                changeCenter(dx, dy);
            }
        }
        
        if (rotatable1 || rotatable2) {
            changeAngle(da, cc);
        }
        
        if (scalable) {
            changeScale(dw, dh);
        }
        
        // Check for subjunctions that inherit values from this Junction
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.updateJunction(dx, dy, da, dw, dh, cc);
            }
        }
    }
    
    /**
     * Gets an array of any Junctions associated with this Junction.
     * 
     * @return an array of Junctions
     */
    public Junction[] getJunctions() {
        // Force the creation of an array that is exactly the same size as the list without a separate size() call
        return junctionList.toArray(new Junction[0]);
    }
    
    /**
     * Returns the number of subjunctions contained in this Junction.
     * 
     * @return the subjunction count
     */
    public int getJunctionCount() {
        return junctionList.size();
    }
    
    /**
     * Adds a contact to this Junction.
     * 
     * @param id the unique identifier for the Contact
     * @param x the center x value
     * @param y the center y value
     */
    public void addContact(int id, float x, float y) {
        boolean added = false;
        
        // Check for subjunctions first
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                if (j.inside(x, y)) {
                    j.addContact(id, x, y);
                    added = true;
                    break;
                }
            }
        }
        
        if (!added) {
            // Only send an active message when a new Contact is added to an empty map
            if (relayActive && contactMap.isEmpty()) {
                if (targetRelay != null) {
                    for (String s : activeList) {
                        targetRelay.resetMessage(s);
                        targetRelay.addInteger(s, 1);
                        targetRelay.send(s);
                    }
                }
            }
            
            // Now put new Contact into map
            contactMap.put(id, new Contact(id, x, y));
            
            // Change toggle for new contacts
            if (toggleOn) {
                toggleOn = false;
            }
            else {
                toggleOn = true;
            }
            
            if (relayToggle) {
                if (targetRelay != null) {
                    for (String s : toggleList) {
                        targetRelay.resetMessage(s);
                        
                        if (toggleOn) {
                            targetRelay.addInteger(s, 1);
                        }
                        else {
                            targetRelay.addInteger(s, 0);
                        }
                        
                        targetRelay.send(s);
                    }
                }
            }
            
            // Contact count
            if (relayContactCount) {
                relayContactCount();
            }
        }
    }
    
    /**
     * Updates the specified contact.
     * 
     * @param id the unique identifier for the Contact
     * @param x an updated center x value
     * @param y an updated center y value
     */
    public void updateContact(int id, float x, float y) {
        boolean updated = false;
        int contactCount = 0;
        
        // Check for subjunctions first
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                if (j.containsContact(id)) {
                    j.updateContact(id, x, y);
                    updated = true;
                    break;
                }
            }
        }
        
        if (!updated) {
            float dx = 0;
            float dy = 0;
            float da = 0;
            float ds = 0;
            
            if (contactMap.containsKey(id)) {
                // Get the contact to be updated
                Contact contact = contactMap.get(id);
                
                float newX = x;
                float newY = y;
                float oldX = contact.getX();
                float oldY = contact.getY();
                
                // New contact values
                contact.setX(x);
                contact.setY(y);
                
                // The distance between old and new center points
                float d = dist(newX, newY, oldX, oldY);
                
                // Check d before anything else
                if (d > 0) {
                    // Contacts must be inside of a Junction to be mappable
                    if (inside(x,y)) {
                        if (shape == RECT) {
                            if (relayContact) {
                                for (String s : contactList) {
                                    targetRelay.resetMessage(s);
                                    targetRelay.addInteger(s, id);
                                    targetRelay.addFloat(s, normal(oldX-(centerX-(width/2)), 0, width));
                                    targetRelay.addFloat(s, 1-normal(oldY-(centerY-(height/2)), height, 0));
                                    targetRelay.send(s);
                                }
                            }
                            
                            if (relayContactX) {
                                if (targetRelay != null) {
                                    for (String s : contactXList) {
                                        targetRelay.resetMessage(s);
                                        targetRelay.addInteger(s, id);
                                        targetRelay.addFloat(s, normal(oldX-(centerX-(width/2)), 0, width));
                                        targetRelay.send(s);
                                    }
                                }
                            }
                            
                            if (relayContactY) {
                                if (targetRelay != null) {
                                    for (String s : contactYList) {
                                        targetRelay.resetMessage(s);
                                        targetRelay.addInteger(s, id);
                                        targetRelay.addFloat(s, 1-normal(oldY-(centerY-(height/2)), height, 0));
                                        targetRelay.send(s);
                                    }
                                }
                            }
                        }
                        else if (shape == ELLIPSE) {
                            if (relayContact) {
                                for (String s : contactList) {
                                    targetRelay.resetMessage(s);
                                    targetRelay.addInteger(s, id);
                                    
                                    double theta = Math.atan2(newY-centerY, newX-centerX);
                                    
                                    if (theta < 0) {
                                        theta = Math.abs(theta);
                                    }
                                    else if (theta > 0) {
                                        theta = Math.abs(theta - TWO_PI);
                                    }
                                    
                                    if (width == height) {
                                        targetRelay.addFloat(s, normal(dist(newX, newY, centerX, centerY), 0, width/2));
                                    }
                                    else {
                                        // Getting the point on an ellipse (for normalizing) requires more math
                                        double t = (Math.atan((width/height)*Math.tan(theta))); // The parametric angle
                                        float edgeX = centerX + (float)((width/2)*Math.cos(t));
                                        float edgeY = centerY + (float)((height/2)*Math.sin(t));
                                        float edge = dist(edgeX, edgeY, centerX, centerY);
                                        targetRelay.addFloat(s, normal(dist(newX, newY, centerX, centerY), 0, edge));
                                    }
                                    
                                    targetRelay.addFloat(normal((float)theta, 0, TWO_PI));
                                    targetRelay.send(s);
                                }
                            }
                            
                            if (relayContactR) {
                                for (String s : contactRList) {
                                    targetRelay.resetMessage(s);
                                    targetRelay.addInteger(s, id);
                                    
                                    if (width == height) {
                                        targetRelay.addFloat(s, normal(dist(newX, newY, centerX, centerY), 0, width/2));
                                    }
                                    else {
                                        double theta = Math.atan2(newY-centerY, newX-centerX);
                                        
                                        if (theta < 0) {
                                            theta = Math.abs(theta);
                                        }
                                        else if (theta > 0) {
                                            theta = Math.abs(theta - TWO_PI);
                                        }
                                        
                                        // Getting the point on an ellipse (for normalizing) requires more math
                                        double t = (Math.atan((width/height)*Math.tan(theta))); // The parametric angle
                                        float edgeX = centerX + (float)((width/2)*Math.cos(t));
                                        float edgeY = centerY + (float)((height/2)*Math.sin(t));
                                        float edge = dist(edgeX, edgeY, centerX, centerY);
                                        targetRelay.addFloat(s, normal(dist(newX, newY, centerX, centerY), 0, edge));
                                    }
                                    
                                    targetRelay.send(s);
                                }
                            }
                            
                            if (relayContactTheta) {
                                for (String s : contactThetaList) {
                                    targetRelay.resetMessage(s);
                                    targetRelay.addInteger(s, id);
                                    
                                    double theta = Math.atan2(newY-centerY, newX-centerX);
                                    
                                    if (theta < 0) {
                                        theta = Math.abs(theta);
                                    }
                                    else if (theta > 0) {
                                        theta = Math.abs(theta - TWO_PI);
                                    }
                                    
                                    targetRelay.addFloat(normal((float)theta, 0, TWO_PI));
                                    targetRelay.send(s);
                                }
                            }
                        }
                    }
                    
                    // Positive = moving up, negative = moving down
                    dx = newX - oldX;
                    dy = newY - oldY;
                    
                    contactCount = contactMap.size();
                    
                    switch (contactCount) {
                        case 1:
                            // Translate
                            if (translatable && (contactCount >= minTranslationContacts && contactCount <= maxTranslationContacts)) {
                                changeCenter(dx, dy);
                            }
                            
                            // Rotate
                            if (rotatable1) {
                                float oldTheta = (float)(Math.atan2((double)oldY-centerY, (double)oldX-centerX));
                                float newTheta = (float)(Math.atan2((double)newY-centerY, (double)newX-centerX));
                                
                                // Clockwise
                                if (oldTheta > 0 && newTheta < 0) {
                                    da = Math.abs(oldTheta - Math.abs(newTheta));
                                }
                                // Counterclockwise
                                else if (oldTheta < 0 && newTheta > 0) {
                                    da = 0-(Math.abs(newTheta - Math.abs(oldTheta)));
                                }
                                else {
                                    da = newTheta-oldTheta;
                                }
                                
                                changeAngle(da);
                            }
                            break;
                        case 2:
                            if (translatable && (contactCount >= minTranslationContacts && contactCount <= maxTranslationContacts)) {
                                changeCenter(dx/2, dy/2);
                            }
                            
                            if (rotatable2) {
                                Contact[] contacts = new Contact[2];
                                contactMap.values().toArray(contacts);
                                
                                if (contacts[0] != null && contacts[1] != null) {
                                    newContactTheta = (float)(Math.atan2((double)contacts[0].getY()-contacts[1].getY(), (double)contacts[0].getX()-contacts[1].getX()));
                                    
                                    if (oldContactTheta != 0) {
                                        // Clockwise
                                        if (oldContactTheta > 0 && newContactTheta < 0) {
                                            da = Math.abs(oldContactTheta - Math.abs(newContactTheta));
                                        }
                                        // Counterclockwise
                                        else if (oldContactTheta < 0 && newContactTheta > 0) {
                                            da = 0-(Math.abs(newContactTheta - Math.abs(oldContactTheta)));
                                        }
                                        else {
                                            da = newContactTheta-oldContactTheta;
                                        }
                                        
                                        changeAngle(da);
                                    }
                                    
                                    oldContactTheta = newContactTheta;
                                }
                            }
                            
                            if (scalable) {
                                // Get distance between 2 contacts
                                Contact[] contacts = new Contact[2];
                                contactMap.values().toArray(contacts);
                                
                                if (contacts[0] != null && contacts[1] != null) {
                                    newContactDist = dist(contacts[0].getX(), contacts[0].getY(), contacts[1].getX(), contacts[1].getY());
                                    // Do not scale on initial 2 touches
                                    if (oldContactDist != 0) {
                                        ds = newContactDist - oldContactDist;
                                    }
                                    oldContactDist = newContactDist;
                                }
                                
                                // Change scale proportionally
                                changeScale(ds, ds);
                            }
                            break;
                        default:
                            if (translatable && (contactCount >= minTranslationContacts && contactCount <= maxTranslationContacts)) {
                                changeCenter(dx/contactCount, dy/contactCount);
                            }
                            break;
                                
                    }
                }
            }
            
            // Check for subjunctions that inherit values from this Junction
            if (!junctionList.isEmpty()) {
                for (Junction j : junctionList) {
                    // Subjunctions take delta values since their centers need to move with the parent Junction
                    j.updateJunction(dx, dy, da, ds, ds, contactCount);
                }
            }
        }
    }
    
    /**
     * Removes the specified contact from this Junction.
     * 
     * @param id the unique identifier for the Contact
     */
    public void removeContact(int id) {
        if (contactMap.containsKey(id)) {
            contactMap.remove(id);
        }
        
        // Reset distance values for less than 2 Contacts
        if (contactMap.values().size() < 2) {
            oldContactDist = 0;
            newContactDist = 0;
            oldContactTheta = 0;
            newContactTheta = 0;
        }
        
        // Contact count has changed
        if (relayContactCount) {
            relayContactCount();
        }
        
        // Only send inactive if this is the very last contact
        if (relayActive && contactMap.isEmpty()) {
            if (targetRelay != null) {
                for (String s : activeList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addInteger(s, 0);
                    targetRelay.send(s);
                }
            }
        }
        
        // Check for subjunctions
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                if (j.containsContact(id)) {
                    j.removeContact(id);
                }
            }
        }
    }
    
    /**
     * Removes all Contacts contained in this Junction.
     */
    public void clearContacts() {
        contactMap.clear();
        
        oldContactDist = 0;
        newContactDist = 0;
        oldContactTheta = 0;
        newContactTheta = 0;
        
        // Contact count has changed
        if (relayContactCount) {
            relayContactCount();
        }
        
        // Junction is no longer active
        if (relayActive) {
            if (targetRelay != null) {
                for (String s : activeList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addInteger(s, 0);
                    targetRelay.send(s);
                }
            }
        }
        
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                j.clearContacts();
            }
        }
    }
    
    /**
     * Returns true if this Junction contains the specified Contact.
     * 
     * @param id the unique identifier for the Contact
     * @return true is contact is contained in this Junction
     */
    protected boolean containsContact(int id) {
        boolean contains = false;
        
        if (contactMap.containsKey(id)) {
            contains = true;
        }
        
        // Now check subjunctions
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                if (j.containsContact(id)) {
                    contains = true;
                }
            }
        }
        
        return contains;
    }
    
    /**
     * Returns the specified Contact if it is contained within this Junction.
     * 
     * @param id the unique identifier for the Contact
     * @return the Contact specified or null if that Contact is not contained in this Junction
     */
    protected Contact getContact(int id) {
        return contactMap.get(id);
    }
    
    /**
     * Returns the number of Contacts currently associated with this Junction.
     * 
     * @return the Contact count
     */
    public int getContactCount() {
        return (contactMap.size());
    }
    
    /**
     * Returns a list that is a copy of the current contact map values.
     * 
     * @return a Contact list or an empty list of no Contacts are contained in this Junction
     */
    public Contact[] getContacts() {
        // Create new Contacts to avoid giving handles to Contacts that may be in use
        ArrayList<Contact> contactList = new ArrayList<Contact>();
        
        for (Contact c : contactMap.values()) {
            contactList.add(new Contact(c.getID(), c.getX(), c.getY()));
        }
        
        return contactList.toArray(new Contact[contactList.size()]);
    }
    
    /**
     * Determines whether interactions with this Junction will be recorded by the Dispatcher.
     * 
     * @param r true to allow recording, false otherwise
     */
    public void allowRecording(boolean r) {
        recordable = r;
    }
    
    /**
     * Returns true if recording should happen and false otherwise.
     * 
     * @return true to record, false otherwise
     */
    public boolean isRecordable() {
        return recordable;
    }
    
    /**
     * Determines whether state saving should be allowed.
     * 
     * @param s true to allow saving, false otherwise
     */
    public void allowSaving(boolean s) {
        savable = s;
    }
    
    /**
     * Returns true is saving should happen, false otherwise.
     * 
     * @return true to save, false otherwise
     */
    public boolean isSavable() {
        return savable;
    }
    
    /**
     * Maps the specified Action to the specified OSC message.
     * 
     * @param action the Action to be associated with the message
     * @param message the message to send for the specified Action
     */
    public void mapMessage(Action action, String message) {
        if (targetRelay != null) {
            // First add the new message to the Relay
            targetRelay.addMessage(message);
            
            // Now add the message to the appropriate list
            switch (action) {
                case ACTIVATE:
                    relayActive = true;
                    activeList.add(message);
                    break;
                case TOGGLE:
                    relayToggle = true;
                    toggleList.add(message);
                    break;
                case ROTATE:
                    relayRotate = true;
                    rotateList.add(message);
                    break;
                case ROTATE_1:
                    relayRotate1 = true;
                    rotate1List.add(message);
                    break;
                case ROTATE_2:
                    relayRotate2 = true;
                    rotate2List.add(message);
                    break;
                case SCALE:
                    relayScale = true;
                    scaleList.add(message);
                    break;
                case SCALE_WIDTH:
                    relayScaleWidth = true;
                    scaleWidthList.add(message);
                    break;
                case SCALE_HEIGHT:
                    relayScaleHeight = true;
                    scaleHeightList.add(message);
                    break;
                case TRANSLATE:
                    relayTranslateXY = true;
                    translateXYList.add(message);
                    break;
                case TRANSLATE_X:
                    relayTranslateX = true;
                    translateXList.add(message);
                    break;
                case TRANSLATE_Y:
                    relayTranslateY = true;
                    translateYList.add(message);
                    break;
                case CONTACT:
                    relayContact = true;
                    contactList.add(message);
                    break;
                case CONTACT_X:
                    relayContactX = true;
                    contactXList.add(message);
                    break;
                case CONTACT_Y:
                    relayContactY = true;
                    contactYList.add(message);
                    break;
                case CONTACT_R:
                    relayContactR = true;
                    contactRList.add(message);
                    break;
                case CONTACT_THETA:
                    relayContactTheta = true;
                    contactThetaList.add(message);
                    break;
                case COUNT_CONTACTS:
                    relayContactCount = true;
                    contactCountList.add(message);
                    break;
                case COUNT_ROTATIONS:
                    relayRotationCount = true;
                    rotationCountList.add(message);
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Removes the mapping of the specified Action to the specified OSC message.
     * 
     * @param action the Action to unmap
     * @param message the message to unmap
     */
    public void unmapMessage(Action action, String message) {
        if (targetRelay != null) {
            targetRelay.removeMessage(message);
            
            switch (action) {
                case ACTIVATE:
                    activeList.remove(message);
                    break;
                case TOGGLE:
                    toggleList.remove(message);
                    break;
                case ROTATE:
                    rotateList.remove(message);
                    break;
                case ROTATE_1:
                    rotate1List.remove(message);
                    break;
                case ROTATE_2:
                    rotate2List.remove(message);
                    break;
                case SCALE:
                    scaleList.remove(message);
                    break;
                case SCALE_WIDTH:
                    scaleWidthList.remove(message);
                    break;
                case SCALE_HEIGHT:
                    scaleHeightList.remove(message);
                    break;
                case TRANSLATE:
                    translateXYList.remove(message);
                    break;
                case TRANSLATE_X:
                    translateXList.remove(message);
                    break;
                case TRANSLATE_Y:
                    translateYList.remove(message);
                    break;
                case CONTACT:
                    contactList.remove(message);
                    break;
                case CONTACT_X:
                    contactXList.remove(message);
                    break;
                case CONTACT_Y:
                    contactYList.remove(message);
                    break;
                case CONTACT_R:
                    contactRList.remove(message);
                    break;
                case CONTACT_THETA:
                    contactThetaList.remove(message);
                    break;
                case COUNT_CONTACTS:
                    contactCountList.remove(message);
                    break;
                case COUNT_ROTATIONS:
                    rotationCountList.remove(message);
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Returns true if the given x,y point is inside the current bounds of this Junction. Tests for insideness are determined by the
     * shape set for this Junction.
     * 
     * @param x the x coordinate of the point to be tested
     * @param y the y coordinate of the point to be tested
     * @return true if the point is inside this Junction
     */
    protected boolean inside(float x, float y) {
        boolean inside = false;
        
        // Check subjunctions first
        if (!junctionList.isEmpty()) {
            for (Junction j : junctionList) {
                if (j.inside(x, y)) {
                    inside = true;
                }
            }
        }
        
        if (!inside) {
            switch (shape) {
                case ELLIPSE:
                    // Circle
                    if (width == height) {
                        if (dist(x, y, centerX, centerY) < width/2) {
                            inside = true;
                        }
                    }
                    else {
                        if (angle == 0) {
                            /* x,y is point and x0,y0 is center of ellipse
                             * d = (x-x0/width)^2 + (y-y0/height)^2
                             * if d <= 1 then x,y is inside
                             */
                            double d = Math.pow((x - centerX)/(width/2), 2) + Math.pow((y - centerY)/(height/2), 2);
                            
                            if (d <= 1) {
                                inside = true;
                            }
                        }
                        else {
                            // Rotated
                            float cosAngle = (float)Math.cos(0.0f - angle);
                            float sinAngle = (float)Math.sin(0.0f - angle);
                            float rotX;
                            float rotY;
                            
                            // Rotate the point in order to check for insideness
                            rotX = (x-centerX)*cosAngle - (y-centerY)*sinAngle;
                            rotY = (x-centerX)*sinAngle + (y-centerY)*cosAngle;
                            
                            // Translate the center to 0,0 for testing
                            double d = Math.pow(rotX/(width/2), 2) + Math.pow(rotY/(height/2), 2);
                            
                            if (d <= 1) {
                                inside = true;
                            }
                        }
                    }
                    break;
                case RECT:
                    /*
                     *  Check for insideness when a rectangle is rotated by moving touch points by the angle of rotation
                     *  
                     *  Matrix multiplication where x'y' is point to test
                     *  x' = x*cos(angle) - y*sin(angle)
                     *  y' = x*sin(angle) + y*cos(angle)
                     *  
                     */
                    if (angle == 0) {
                        // NOT rotated
                        if (x > centerX - width/2 && x < centerX + width/2) {
                            if (y > centerY - height/2 && y < centerY + height/2) {
                                inside = true;
                            }
                        }
                    }
                    else {
                        // Rotated
                        float cosAngle = (float)Math.cos(0.0f - angle);
                        float sinAngle = (float)Math.sin(0.0f - angle);
                        float rotX;
                        float rotY;
                        
                        // Rotate the point in order to check for insideness
                        rotX = (x-centerX)*cosAngle - (y-centerY)*sinAngle;
                        rotY = (x-centerX)*sinAngle + (y-centerY)*cosAngle;
                        
                        // Translate the center of the rectangle to 0,0 for testing
                        if (rotX > (0 - width/2) && rotX < width/2) {
                            if (rotY > (0 - height/2) && rotY < height/2) {
                                inside = true;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        
        return inside;
    }
    
    /**
     * Returns true if this Junction has any Contacts.
     * 
     * @return true if the Junction contains one or more Contacts
     */
    public boolean isActive() {
        boolean active = false;
        
        if (contactMap.size() > 0) {
            active = true;
        }
        
        return active;
    }
    
    /**
     * Sends the current Contact count for this Junction.
     */
    private void relayContactCount() {
        int contactCount = contactMap.size();
        
        if (contactCount != lastContactCount) {
            if (targetRelay != null) {
                for (String s : contactCountList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addInteger(s, contactCount);
                    targetRelay.send(s);
                }
            }
            
            lastContactCount = contactCount;
        }
    }
    
    /**
     * Sends the current rotation count for this Junction.
     */
    private void relayRotationCount() {
        if (rotationCount != lastRotationCount) {
            if (targetRelay != null) {
                for (String s : rotationCountList) {
                    targetRelay.resetMessage(s);
                    targetRelay.addInteger(s, rotationCount);
                    targetRelay.send(s);
                }
            }
            
            lastRotationCount = rotationCount;
        }
    }
    
    /**
     * Returns the distance between the specified points.
     * 
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @return the distance between the first and second points
     */
    private float dist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
    }
    
    /**
     * Returns a normalized version of the input value given the specified minimum and maximum vales.
     * 
     * @param n the value to be normalized
     * @param min the minimum value in a range
     * @param max the maximum value in a range
     * @return the normalized value
     */
    private float normal(float n, float min, float max) {
        return min == 0 ? n/max : (n-min)/(max-min);
    }
}
