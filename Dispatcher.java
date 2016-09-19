package junctionbox;

import android.view.MotionEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.InterruptedException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
import TUIO.TuioBlob;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioListener;
import TUIO.TuioObject;
import TUIO.TuioTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Dispatcher takes touch input and dispatches the basic touch data to Junctions to enables interactions. The Dispatcher
 * allows for interactions with Junctions to be saved as XML and loaded back again, preserving the state of the interaction.
 * The Dispatcher can record interactions with Junctions that can then be played back, looped or time stretched.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 * @see <a href="http://opensoundcontrol.org/spec-1_0">The Open Sound Control 1.0 Specification</a>
 * @see <a href="http://tuio.org/?specification">TUIO 1.1 Protocol Specification</a>
 */
public class Dispatcher implements TuioListener {
    /**
     * The master list of Junctions.
     */
    private CopyOnWriteArrayList<Junction> junctionList;
    
    /**
     * The width of the designated touch area.
     */
    private float boxWidth;
    
    /**
     * The height of the designated touch area.
     */
    private float boxHeight;
    
    /**
     * The IP address of the target.
     */
    private String targetAddress;
    
    /**
     * The port number of the target
     */
    private int targetPort;
    
    /**
     * Whether to use the target that has been set in this Dispatcher.
     */
    private boolean useTarget = false;
    
    /**
     * The client for receiving TUIO messages.
     */
    private TuioClient tuioClient;
    
    /**
     * Determines whether a mouse click is a new Contact. This prevents new Contacts from being created when the mouse button is held down.
     */
    private boolean newMouseContact = true;
    
    /**
     * Determines whether the mouse is active. This is needed for ensuring that Contacts are removed only when they are active.
     */
    private boolean mouseActive = false;
    
    /**
     * The queue for recorded events.
     */
    private PriorityQueue<Event> eventQueue;
    
    /**
     * The number of recording made for a single event queue.
     */
    private int recordingCounter = -1;
    
    /**
     * The first nanosecond tick for recording events.
     */
    protected long firstRecordTick = 0;
    
    /**
     * The first nanosecond tick for event playback.
     */
    protected long firstPlayTick = 0;
    
    /**
     * The current event state: stopped, recording, or playing.
     */
    private volatile int state;
    
    /**
     * The default state.
     */
    private final int STOPPED = 0;
    
    /**
     * Event recording is active.
     */
    private final int RECORDING = 1;
    
    /**
     * The event playback thread is active.
     */
    private final int PLAYING = 2;
    
    /**
     * The event playback thread.
     */
    private Player playThread;
    
    /**
     * Determines whether to loop playback.
     */
    private volatile boolean looping = false;
    
    /**
     * Determines whether an event is the first to be recorded.
     */
    private boolean firstEvent = true;
    
    /**
     * The event recording time in nanoseconds.
     */
    private long recordTime = 0;
    
    /**
     * The event playback time in nanoseconds.
     */
    private long playbackTime = 0;
    
    /**
     * Builder for XML documents.
     */
    private DocumentBuilder builder;
    
    /**
     * The XML document to write.
     */
    private Document writeDoc;
    
    /**
     * The root of the XML document to write.
     */
    private Element writeRoot;
    
    /**
     * The XML document to read.
     */
    private Document readDoc;
    
    /**
     * The root of the XML document to read.
     */
    private Element readRoot;
    
    /**
     * An initial memory allocation to avoid further allocations for multiplications in scaleDelayTimes().
     */
    private BigDecimal scaledDelay;
    
    /**
     * An initial memory allocation to avoid further allocations for rounding in scaleDelayTimes().
     */
    private BigDecimal roundedDelay;
    
    /**
     * Event is an inner class associated with the Timetable class.
     */
    private class Event implements Delayed {
        private int recordingID;
        public static final int ADD = 0;
        public static final int UPDATE = 1;
        public static final int REMOVE = 2;
        private int type;
        private int id;
        private float x = 0;
        private float y = 0;
        long delay;
        
        /**
         * Constructs a new Event.
         * 
         * @param t the event type
         * @param id the event identifier
         * @param x the x value of the event
         * @param y the y value of the event
         * @param d the delay value for the event
         */
        public Event(int r, int t, int id, float x, float y, long d) {
            recordingID = r;
            this.id = id;
            this.x = x;
            this.y = y;
            type = t;
            delay = d;
        }
        
        /**
         * Constructs a new Event.
         * 
         * @param t the event type
         * @param id the event identifier
         * @param d the delay value for the event
         */
        public Event(int r, int t, int id, long d) {
            recordingID = r;
            this.id = id;
            type = t;
            delay = d;
        }
        
        /**
         * Compares delay values for Events.
         * 
         * @param d a Delayed object
         */
        public int compareTo(Delayed d) {
            // Comparison must be between actual delay values not result of getDelay() call
            Event e = (Event)d;
            if (this.delay < e.getDelayValue()) {
                return -1;
            }
            else if (this.delay > e.getDelayValue()) {
                return 1;
            }
            else {
                // Delay values are equal
                return 0;
            }
        }
        
        /**
         * Gets the delay value for this Event. Note that his is used by the Timetable to determine whether this Event should be
         * removed from the queue. It does not return the absolute delay value.
         * 
         * @param unit TimeUnit for the delay
         */
        public long getDelay(TimeUnit unit) {
            // Subtract delay value from current timer value to get expiration
            return unit.convert(delay - (System.nanoTime() - firstPlayTick), TimeUnit.NANOSECONDS);
        }
        
        /**
         * Gets the absolute delay value.
         * 
         * @return the absolute delay
         */
        public long getDelayValue() {
            return this.delay;
        }
        
        /**
         * Sets the delay value.
         * 
         * @param d the delay
         */
        public void setDelayValue(long d) {
            delay = d;
        }
        
        /**
         * Gets the recording ID for this Event.
         * 
         * @return the recording ID
         */
        public int getRecordingID() {
            return recordingID;
        }
        
        /**
         * Gets the type for this Event.
         * 
         * @return the event type
         */
        public int getType() {
            return type;
        }
        
        /**
         * Gets the ID associated with this Event.
         * 
         * @return the Event ID
         */
        public int getID() {
            return id;
        }
        
        /**
         * Gets the X value associated with this Event.
         * 
         * @return the x value
         */
        public float getX() {
            return x;
        }
        
        /**
         * Gets the Y value associated with this Event.
         * 
         * @return the y value
         */
        public float getY() {
            return y;
        }
    }
    
    /**
     * Creates a Thread to run playback of recorded Events.
     */
    private class Player extends Thread {
        private long startTimeTick = 0;
        
        public void run() {
            startTimeTick = System.nanoTime();
            
            int c = eventQueue.size();
            
            if (c > 0) {
                state = PLAYING;
                
                playing: // The label helps for breaking both inner and outer loops
                    do {
                        DelayQueue<Event> playQueue = new DelayQueue<Event>(eventQueue);
                        firstPlayTick = System.nanoTime();
                        
                        for (int i = 0; i < c; i++) {
                            Event e;
                            try {
                                // This should be waiting based on the delay
                                e = playQueue.take();
                                
                                if (e != null) {
                                    switch (e.getType()) {
                                        case Event.ADD:
                                            addContact(e.getID(), e.getX(), e.getY());
                                            break;
                                        case Event.UPDATE:
                                            updateContact(e.getID(), e.getX(), e.getY());
                                            break;
                                        case Event.REMOVE:
                                            removeContact(e.getID());
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                            catch (InterruptedException ie) {
                                clearContacts();
                                break playing;
                            }
                        }
                        
                        clearContacts();
                    }
                    while (looping);
                
                looping = false;
                state = STOPPED;
            }
            
            playbackTime = System.nanoTime() - startTimeTick;
        }
    }
    
    /**
     * Constructs a new Dispatcher with a box width and height that will be passed to Junctions.
     * 
     * @param w the box width
     * @param h the box height
     */
    public Dispatcher(float w, float h) {
        initialize();
        
        boxWidth = w;
        boxHeight = h;
    }
    
    /**
     * Constructs a new Dispatcher with a box width and height and a target address and port that will be passed to Junctions.
     * 
     * @param w the box width
     * @param h the box height
     * @param address the IP address of the target
     * @param port the port number of the target
     */
    public Dispatcher(float w, float h, String address, int port) {
        initialize();
        
        boxWidth = w;
        boxHeight = h;
        
        targetAddress = address;
        targetPort = port;
        useTarget = true;
    }
    
    /**
     * Initialize the list for Junctions, the TUIO client, the Event queue, and the XML document builder.
     */
    private void initialize() {
        junctionList = new CopyOnWriteArrayList<Junction>();
        
        // Event queue for this Timetable
        eventQueue = new PriorityQueue<Event>();
        
        // Default state
        state = STOPPED;
        
        // Set up XML input/output
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            writeDoc = builder.newDocument();
            writeRoot = writeDoc.createElement("junctionbox");
            writeDoc.appendChild(writeRoot);
        }
        catch (ParserConfigurationException e) {
            // Do nothing
        }
    }
    
    /**
     * Sets the IP address and port of the target to the specified values.
     * 
     * @param address the IP address
     * @param port the port number
     */
    public void setTarget(String address, int port) {
        targetAddress = address;
        targetPort = port;
        useTarget = true;
        
        for (Junction j : junctionList) {
            j.setTarget(address, port);
        }
    }
    
    /**
     * Returns the target IP address for this Dispatcher.
     * 
     * @return the IP address String
     */
    public String getTargetAddress() {
        return targetAddress;
    }
    
    /**
     * Returns the target port number for this Dispatcher.
     * 
     * @return the port number as an integer
     */
    public int getTargetPort() {
        return targetPort;
    }
    
    /**
     * Creates a new Junction with the specified parameters. In order to receive Contacts, a Junctions must either be created by or added to
     * this Dispatcher. Junctions can be added with the {@link #addJunction(Junction)} method. When a Junction is created with this method,
     * it inherits the target IP address and port from this Dispatcher. This is convenient for situations where there is only one target that
     * all Junctions can use without having to set individual targets for each Junction. The target IP address and port and still be set
     * individually for Junctions when {@link #addJunction(Junction)} is used.
     * 
     * @param x the x coordinate of the center point
     * @param y the y coordinate of the center point
     * @param w the width
     * @param h the height
     * 
     * @return the new Junction
     */
    public Junction createJunction(float x, float y, float w, float h) {
        Junction j = new Junction(boxWidth, boxHeight, x, y, w, h);
        
        // One set target for Junctions if a target has been set for this Dispatcher
        if (useTarget) {
            j.setTarget(targetAddress, targetPort);
        }
        
        junctionList.add(j);
        
        return j;
    }
    
    /**
     * Adds a Junction to this Dispatcher. In order to receive Contacts, a Junctions must either be created by or added to
     * this Dispatcher. Junctions can be created with the {@link #createJunction(float, float, float, float)} method. A Junction can be added only once.
     * 
     * @param j the new Junction to add
     */
    public void addJunction(Junction j) {
        // Only allow a junction to be added once
        if (!junctionList.contains(j)) {
            junctionList.add(j);
        }
    }
    
    /**
     * Removes a Junction from this Dispatcher. A Junction removed from this Dispatcher will not receive any Contacts.
     * 
     * @param j the Junction to remove
     */
    public void removeJunction(Junction j) {
        // This removes the first occurrence of this object but there should only be one...
        if (junctionList.contains(j)) {
            junctionList.remove(j);
        }
    }
    
    /**
     * Sets the order for a Junction in the list for this Dispatcher. The order determines which Junction will receive a Contact
     * in cases where Junctions overlap. Junctions with lower numbers have a higher priority for receiving Contacts.
     * 
     * @param o the value to set for the order
     * @param j the Junction to be ordered
     */
    public void orderJunction(int o, Junction j) {
        if (junctionList.contains(j)) {
            // Ensure that order value is valid
            if (o >= 0 && o < (junctionList.size())) {
                junctionList.remove(j);
                // This automatically shifts other elements in the list
                junctionList.add(o, j);
            }
        }
    }
    
    /**
     * Returns an array of the Junctions currently stored in this Dispatcher.
     * 
     * @return an array of current Junctions
     */
    public Junction[] getJunctions() {
        return junctionList.toArray(new Junction[0]);
    }
    
    /**
     * Removes all Junctions stored by this Dispatcher.
     */
    public void clearJunctions() {
        junctionList.clear();
    }
    
    /**
     * Starts the TUIO client that listens for touch events.
     */
    public void startTUIO() {
        // Start dispatching TUIO events
        tuioClient = new TuioClient();
        tuioClient.connect();
        tuioClient.addTuioListener(this);
    }
    
    /**
     * Stops the TUIO client.
     */
    public void stopTUIO() {
        // Stop dispatching TUIO events
        if (tuioClient != null && tuioClient.isConnected()) {
            tuioClient.disconnect();
            tuioClient.removeTuioListener(this);
        }
    }
    
    /**
     * Not currently implemented.
     *
     * @param tobj the TuioObject to add
     */
    public void addTuioObject(TuioObject tobj) {
        ;
    }

    /**
     * Not currently implemented.
     *
     * @param tobj the TuioObject to update
     */
    public void updateTuioObject(TuioObject tobj) {
        ;
    }

    /**
     * Not currently implemented.
     *
     * @param tobj the TuioObject to remove
     */
    public void removeTuioObject(TuioObject tobj) {
        ;
    }
    
    /**
     * Not currently implemented.
     * 
     * @param tblb the TuioBlob to add
     */
    public void addTuioBlob(TuioBlob tblb) {
        ;
    }
    
    /**
     * Not currently implemented.
     * 
     * @param tblb the TuioBlob to update
     */
    public void updateTuioBlob(TuioBlob tblb) {
        ;
    }
    
    /**
     * Not currently implemented.
     * 
     * @param tblb the TuioBlob to remove
     */
    public void removeTuioBlob(TuioBlob tblb) {
        ;
    }
    
    /**
     * Called when a new TuioCursor is added to the session. If the center point of the TuioCursor is contained within a Junction, then
     * a new Contact is created with data from the TuioCursor and passed to that Junction. The session identifier of the TuioCursor is
     * used in the corresponding Contact for ease of association.
     *
     * @param tcur the TuioCursor to add
     */
    public void addTuioCursor(TuioCursor tcur) {
        // For TUIO, scale by the width and height of the box (usually the screen size)
        addContact((int)tcur.getSessionID(), tcur.getX()*boxWidth, tcur.getY()*boxHeight); // It is probably okay to cast long to int for the ID
    }
    
    /**
     * Called when an existing TuioCursor is updated. If the session identifier of the TuioCursor matches the identifier of a Contact in
     * a Junction, then that Contact is updated with new data from the TuioCursor.
     *
     * @param tcur the TuioCursor to update
     */
    public void updateTuioCursor(TuioCursor tcur) {
        // For TUIO, scale by the width and height of the box (usually the screen size)
        updateContact((int)tcur.getSessionID(), tcur.getX()*boxWidth, tcur.getY()*boxHeight); // It is probably okay to cast long to int for the ID
    }
    
    /**
     * Called when an existing TuioCursor is removed from the session. The Contact that corresponds to the TuioCursor that is removed will
     * itself be removed.
     *
     * @param tcur the TuioCursor to remove
     */
    public void removeTuioCursor(TuioCursor tcur) {
        removeContact((int)tcur.getSessionID()); // It is probably okay to cast long to int for the ID
    }
    
    /**
     * Called to mark the end of a received TUIO message bundle.
     *
     * @param ftime the TuioTime for the current TUIO message bundle
     */
    public void refresh(TuioTime ftime) {
        // Nothing here yet...
    }
    
    /**
     * Routes Android touch input data contained in a MotionEvent object.
     * This method must be called in the Android Activity.dispatchTouchEvent(MotionEvent ev) method.
     * 
     * @param event
     */
    public void routeTouch(MotionEvent event) {
        int action = event.getAction();
        int count = event.getPointerCount();
        
        switch(action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                // Create and add contact
                addContact(event.getPointerId(0), event.getX(), event.getY());
                
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                
                // Beware of maximum pointer count!
                if (index < count) {
                    // Create and add contact
                    addContact(event.getPointerId(index), event.getX(index), event.getY(index));
                }
                
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int c = event.getPointerCount();
                for (int i = 0; i < c; i++) {
                    updateContact(event.getPointerId(i), event.getX(i), event.getY(i));
                }
                
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                // Remove contact
                removeContact(event.getPointerId(index));
                
                break;
            }
            case  MotionEvent.ACTION_UP: {
                // Remove all contacts
                //clearContacts();
                
                // How to get index for ACTION_UP?
                // It may not be 0!
                removeContact(event.getPointerId(0));
                
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                // Remove all contacts
                clearContacts();
                
                break;
            }
        }
    }
    
    /**
     * Routes mouse event data. This method should be called in a sketches draw() method to work properly.
     * 
     * @param pressed
     * @param button
     * @param x
     * @param y
     */
    public void routeMouse(boolean pressed, float x, float y) {
        if (pressed) {
        	if (newMouseContact) {
                addContact(0, x, y);
                newMouseContact = false;
                mouseActive = true;
            }
            else {
                updateContact(0, x, y);
            }
        }
        else {
            // This can only be run once without error!
            if (mouseActive) {
                removeContact(0);
                mouseActive = false;
            }
            newMouseContact = true;
        }
    }
    
    /**
     * Adds a Contact to this Dispatcher for distributing to Junctions.
     * 
     * @param id the identifier associated with the Contact
     * @param x the x location of the Contact
     * @param y the y location of the Contact
     */
    public void addContact(int id, float x, float y) {
        Junction[] junctionArray = junctionList.toArray(new Junction[0]);
        
        for (int i = junctionArray.length-1; i >= 0; i--) {
            if (junctionArray[i].isLive()) {
                if (junctionArray[i].inside(x, y)) {
                    if (state == RECORDING && junctionArray[i].isRecordable()) {
                        if (firstEvent) {
                            firstRecordTick = System.nanoTime();
                            queueAddEvent((id*(-1))-1, x, y, 0);
                            firstEvent = false;
                        }
                        else {
                            queueAddEvent((id*(-1))-1, x, y, System.nanoTime()-firstRecordTick);
                        }
                    }
                    junctionArray[i].addContact(id, x, y);
                    break;
                }
            }
        }
    }
    
    /**
     * Updates a Contact associated with a Junction via this Dispatcher.
     * 
     * @param id the identifier associated with the Contact
     * @param x the x location of the Contact
     * @param y the y location of the Contact
     */
    public void updateContact(int id, float x, float y) {
        Junction[] junctionArray = junctionList.toArray(new Junction[0]);
        
        for (int i = junctionArray.length-1; i >= 0; i--) {
            if (junctionArray[i].containsContact(id)) {
                if (state == RECORDING && junctionArray[i].isRecordable()) {
                    if (firstEvent) {
                        firstRecordTick = System.nanoTime();
                        queueUpdateEvent((id*(-1))-1, x, y, 0);
                        firstEvent = false;
                    }
                    else {
                        queueUpdateEvent((id*(-1))-1, x, y, System.nanoTime()-firstRecordTick);
                    }
                }
                junctionArray[i].updateContact(id, x, y);
                break; // No need to check more junctions
            }
        }
    }
    
    /**
     * Removes a Contact from a Junction via this Dispatcher.
     * 
     * @param id the identifier associated with the Contact
     */
    public void removeContact(int id) {
        Junction[] junctionArray = junctionList.toArray(new Junction[0]);
        
        for (int i = junctionArray.length-1; i >= 0; i--) {
            if (junctionArray[i].containsContact(id)) {
                if (state == RECORDING && junctionArray[i].isRecordable()) {
                    if (firstEvent) {
                        firstRecordTick = System.nanoTime();
                        queueRemoveEvent((id*(-1))-1, 0);
                        firstEvent = false;
                    }
                    else {
                        queueRemoveEvent((id*(-1))-1, System.nanoTime()-firstRecordTick);
                    }
                }
                junctionArray[i].removeContact(id);
            }
        }
    }
    
    /**
     * Removes all Contacts from all Junctions.
     */
    public void clearContacts() {
        Junction[] junctionArray = junctionList.toArray(new Junction[0]);
        
        for (int i = junctionArray.length-1; i >= 0; i--) {
            junctionArray[i].clearContacts();
        }
    }
    
    /**
     * Starts recording events for selected Junctions.
     */
    public void startRecording() {
        if (state == STOPPED) {
            clearEvents(); // This is only here until there is code for layering and appending recordings
            state = RECORDING;
            firstEvent = true;
            recordingCounter += 1;
        }
    }
    
    /**
     * Stops recording events.
     */
    public void stopRecording() {
        state = STOPPED;
        
        // Get the record time when stopping to make getting the record time more efficient
        if (!eventQueue.isEmpty()) {
            recordTime = Collections.max(eventQueue).getDelayValue();
        }
    }
    
    /**
     * Returns true if this Dispatcher is currently recording.
     * 
     * @return true if recording, false otherwise
     */
    public boolean isRecording() {
        if (state == RECORDING) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns the elapsed record time in nanoseconds.
     * 
     * @return nanoseconds of recorded time
     */
    public long getRecordTime() {
        return recordTime;
    }
    
    /**
     * Starts the playback of events. Note that this occurs in a new Thread.
     */
    public void startPlaying() {
        if (state == STOPPED) {
            playThread = new Player();
            playThread.start();
        }
    }
    
    /**
     * Stops playback of events.
     */
    public void stopPlaying() {
        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }
    }
    
    /**
     * Returns true if this Dispatcher is currently playing.
     * 
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        if (state == PLAYING) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Loops the playback of events. Looping can be stopped by calling stopPlayback().
     */
    public void loopPlaying() {
        if (state == STOPPED) {
            looping = true;
            startPlaying();
        }
    }
    
    /**
     * Starts the looping state. This method works independently of playback such that if this method is called during playback,
     * looping will begin as soon as the current playback ends.
     */
    public void startLooping() {
        looping = true;
    }
    
    /**
     * Stops the looping state. Calling this method will not cause playback to stop.
     */
    public void stopLooping() {
        looping = false;
    }
    
    /**
     * Returns true if the playback of this Dispatcher is looping.
     * 
     * @return true if looping; false otherwise
     */
    public boolean isLooping() {
        return looping;
    }
    
    /**
     * Returns the elapsed playback time in nanoseconds. The playback time includes time spent looping.
     * 
     * @return nanoseconds of playback time
     */
    public long getPlayTime() {
        return playbackTime;
    }
    
    /**
     * Queues an add Event.
     * 
     * @param id the Event identifier
     * @param x the center x value
     * @param y the center y value
     * @param d the nanosecond delay value
     */
    private void queueAddEvent(int id, float x, float y, long d) {
        eventQueue.add(new Event(recordingCounter, Event.ADD, id, x, y, d));
    }
    
    /**
     * Queues an update Event.
     * 
     * @param id the Event identifier
     * @param x the center x value
     * @param y the center y value
     * @param d the nanosecond delay value
     */
    private void queueUpdateEvent(int id, float x, float y, long d) {
        eventQueue.add(new Event(recordingCounter, Event.UPDATE, id, x, y, d));
    }
    
    /**
     * Queues a remove Event.
     * 
     * @param id the Event identifier
     * @param d the nanosecond delay value
     */
    private void queueRemoveEvent(int id, long d) {
        eventQueue.add(new Event(recordingCounter, Event.REMOVE, id, d));
    }
    
    /**
     * Gets the total number of events in the event queue.
     * 
     * @return the event count
     */
    public int getEventCount() {
        return eventQueue.size();
    }
    
    /**
     * Clears all events from the event queue.
     */
    public void clearEvents() {
        if (state == STOPPED) {
            eventQueue.clear();
            recordTime = 0;
            playbackTime = 0;
            recordingCounter = -1;
        }
    }
    
    /**
     * Returns an array of Events from the event queue.
     * 
     * @return an Event array
     */
    private Event[] getEvents() {
        if (state == STOPPED) {
            Event[] eventArray = new Event[eventQueue.size()];
            eventQueue.toArray(eventArray);
            Arrays.sort(eventArray);
            
            return eventArray;
        }
        else {
            return null;
        }
    }
    
    /**
     * Scale Event times in the queue by the specified factor.
     * 
     * @param s the scaling factor
     */
    public void scaleEventTimes(double s) {
        BigDecimal scaler = new BigDecimal(s);
        
        if (state == STOPPED) {
            Event[] events = new Event[eventQueue.size()];
            eventQueue.toArray(events);
            
            for (int i = 0; i < events.length; i++) {
                BigDecimal initialDelay = new BigDecimal(events[i].getDelayValue());
                scaledDelay = initialDelay.multiply(scaler);
                roundedDelay = scaledDelay.setScale(0, RoundingMode.HALF_UP);
                events[i].setDelayValue(roundedDelay.longValue());
            }
            
            scaledDelay = null;
            roundedDelay = null;
            
            // The total recorded time has now changed
            recordTime = Collections.max(eventQueue).getDelayValue();
        }
    }
    
    /**
     * Removes a set of recorded events based on the recording id.
     * 
     * @param id
     */
    public void removeRecording(int id) {
        if (state == STOPPED) {
            for (Event e : eventQueue) {
                if (e.getRecordingID() == id) {
                    eventQueue.remove(e);
                }
            }
        }
    }
    
    /**
     * Saves Junction data to XML.
     */
    private void saveJunctions() {
        // Check for Junctions to save
        if (junctionList.size() > 0) {
            
            for (Junction j : junctionList) {
                if (j.isSavable()) {
                    Element junctionElement = writeDoc.createElement("junction");
                    
                    Element order = writeDoc.createElement("order");
                    order.appendChild(writeDoc.createTextNode(Integer.toString(junctionList.indexOf(j))));
                    junctionElement.appendChild(order);
                    
                    Element label = writeDoc.createElement("label");
                    label.appendChild(writeDoc.createTextNode(j.getLabel()));
                    junctionElement.appendChild(label);
                    
                    Element x = writeDoc.createElement("centerX");
                    x.appendChild(writeDoc.createTextNode(Float.toString(j.getCenterX())));
                    junctionElement.appendChild(x);
                    
                    Element y = writeDoc.createElement("centerY");
                    y.appendChild(writeDoc.createTextNode(Float.toString(j.getCenterY())));
                    junctionElement.appendChild(y);
                    
                    Element width = writeDoc.createElement("width");
                    width.appendChild(writeDoc.createTextNode(Float.toString(j.getWidth())));
                    junctionElement.appendChild(width);
                    
                    Element height = writeDoc.createElement("height");
                    height.appendChild(writeDoc.createTextNode(Float.toString(j.getHeight())));
                    junctionElement.appendChild(height);
                    
                    Element angle = writeDoc.createElement("angle");
                    angle.appendChild(writeDoc.createTextNode(Float.toString(j.getAngle())));
                    junctionElement.appendChild(angle);
                    
                    Element toggle = writeDoc.createElement("toggle");
                    toggle.appendChild(writeDoc.createTextNode(Boolean.toString(j.getToggle())));
                    junctionElement.appendChild(toggle);
                    
                    writeRoot.appendChild(junctionElement);
                }
            }
        }
    }
    
    /**
     * Saves Event data to XML.
     */
    private void saveEvents() {
        // Use getEvents() since scrolling through the queue removes Events
        Event[] events = getEvents();
        
        // The event array could be null
        if (events != null) {
            int eventCount = events.length;
            
            Element queueElement = writeDoc.createElement("eventQueue");
            
            for (int i = 0; i < eventCount; i++) {
                Element event = writeDoc.createElement("event");
                
                Element type = writeDoc.createElement("type");
                type.appendChild(writeDoc.createTextNode(Integer.toString(events[i].getType())));
                event.appendChild(type);
                
                Element id = writeDoc.createElement("id");
                id.appendChild(writeDoc.createTextNode(Integer.toString(events[i].getID())));
                event.appendChild(id);
                
                Element x = writeDoc.createElement("x");
                x.appendChild(writeDoc.createTextNode(Float.toString(events[i].getX())));
                event.appendChild(x);
                
                Element y = writeDoc.createElement("y");
                y.appendChild(writeDoc.createTextNode(Float.toString(events[i].getY())));
                event.appendChild(y);
                
                Element delay = writeDoc.createElement("delay");
                delay.appendChild(writeDoc.createTextNode(Long.toString(events[i].getDelayValue())));
                event.appendChild(delay);
                
                queueElement.appendChild(event);
            }
            
            writeRoot.appendChild(queueElement);
        }
    }
    
    /**
     * Saves Junction, Relay, and Event data to XML.
     * 
     * @param output the file to save to
     */
    public void saveXML(OutputStream output) {
        saveJunctions();
        saveEvents();
        
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            
            // Each tag on its own line
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            
            DOMSource source = new DOMSource(writeDoc);
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        }
        catch (Exception e) {
            // Do nothing
        }
        finally {
            try {
                if (output != null) {
                    output.close();
                }
            }
            catch (IOException io) {
                // Do nothing
            }
        }
    }
    
    /**
     * Loads Junction data from XML.
     */
    private void loadJunctions() {
        if (readDoc != null) {
            // The root node must be <junctionbox>
            if (readRoot.getNodeName().equals("junctionbox")) {
                NodeList junctionNodes = readDoc.getElementsByTagName("junction");
                
                for (int i = 0; i < junctionNodes.getLength(); i++) {
                    NodeList valueNodes = junctionNodes.item(i).getChildNodes();
                    
                    String label = "";
                    int order = 0;
                    float centerX = 0;
                    float centerY = 0;
                    float width = 0;
                    float height = 0;
                    float angle = 0;
                    boolean toggle = false;
                    
                    for (int j = 0; j < valueNodes.getLength(); j++) {
                        Node value = valueNodes.item(j);
                        
                        if (value.getNodeName().equals("order")) {
                            order = Integer.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("label")) {
                            label = value.getTextContent();
                        }
                        else if (value.getNodeName().equals("centerX")) {
                            centerX = Float.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("centerY")) {
                            centerY = Float.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("width")) {
                            width = Float.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("height")) {
                            height = Float.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("angle")) {
                            angle = Float.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("toggle")) {
                            toggle = Boolean.valueOf(value.getTextContent());
                        }
                    }
                    
                    for (Junction junction : junctionList) {
                        if (label.equals(junction.getLabel()) && junction.isSavable()) {
                            junction.setCenter(centerX, centerY);
                            junction.setWidth(width);
                            junction.setHeight(height);
                            junction.setAngle(angle);
                            junction.setToggle(toggle);
                            
                            orderJunction(order, junction);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Loads Event data from XML.
     */
    private void loadEvents() {
        if (readDoc != null) {
            // The root node must be <junctionbox>
            if (readRoot.getNodeName().equals("junctionbox")) {
                NodeList eventQueueNodes = readDoc.getElementsByTagName("eventQueue");
                
                for (int i = 0; i < eventQueueNodes.getLength(); i++) {
                    NodeList eventNodes = eventQueueNodes.item(i).getChildNodes();
                    
                    for (int j = 0; j < eventNodes.getLength(); j++) {
                        if (eventNodes.item(j).getNodeName().equals("event")) {
                            NodeList valueNodes = eventNodes.item(j).getChildNodes();
                            
                            int type = 0;
                            int id = 0;
                            float x = 0;
                            float y = 0;
                            long delay = 0;
                            
                            for (int k = 0; k < valueNodes.getLength(); k++) {
                                Node value = valueNodes.item(k);
                                if (value.getNodeName().equals("type")) {
                                    type = Integer.valueOf(value.getTextContent());
                                }
                                else if (value.getNodeName().equals("id")) {
                                    id = Integer.valueOf(value.getTextContent());
                                }
                                else if (value.getNodeName().equals("x")) {
                                    x = Float.valueOf(value.getTextContent());
                                }
                                else if (value.getNodeName().equals("y")) {
                                    y = Float.valueOf(value.getTextContent());
                                }
                                else if (value.getNodeName().equals("delay")) {
                                    delay = Long.valueOf(value.getTextContent());
                                }
                            }
                            
                            if (type == 0) {
                                queueAddEvent(id, x, y, delay);
                            }
                            else if (type == 1) {
                                queueUpdateEvent(id, x, y, delay);
                            }
                            else if (type == 2) {
                                queueRemoveEvent(id, delay);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Loads Junction, Relay, and Event data from XML.
     * 
     * @param input the file to load from
     */
    public void loadXML(InputStream input) {
        // Read the file before trying to load
        if (input != null) {
            InputStreamReader reader = new InputStreamReader(input);
            
            try {
                // Is the file readable?
                if (reader.ready()) {
                    readDoc = builder.parse(input);
                    readRoot = readDoc.getDocumentElement();
                }
            }
            catch (Exception e) {
                // Do nothing
            }
            finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                }
                catch (IOException io) {
                    // Do nothing
                }
            }
        }
        
        loadJunctions();
        loadEvents();
    }
}
