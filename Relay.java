package junctionbox;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

/**
 * The Relay class sends OSC messages to the specified target. Messages are stored and referred to by their address pattern with
 * the ability to add integers, floats, longs, strings and blobs to the specified address pattern. Type tags are automatically
 * created when the message is sent.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 * @see <a href="http://opensoundcontrol.org/spec-1_0">The Open Sound Control 1.0 Specification</a>
 */
public class Relay {
    /**
     * The OSC port for sending out messages.
     */
    private OSCPortOut out;
    
    /**
     * The IP address that messages will be sent to.
     */
    private String ipAddress;
    
    /**
     * The port that message will be sent to.
     */
    private int port;
    
    /**
     * The map that contains OSCMessage objects where the key is the message address pattern String.
     */
    private ConcurrentHashMap<String,OSCMessage> messageMap;
    
    /**
     * The label given to this Relay.
     */
    private String label = "";
    
    /**
     * The label sent by a remote node.
     */
    private String remoteLabel = "";
    
    /**
     * The message count sent by a remote note.
     */
    private int remoteMessageCount = 0;
    
    /**
     * The echo status of the remote node.
     */
    private boolean remoteEcho = false;
    
    /**
     * A Sender is a thread for sending messages.
     */
    private class Sender extends Thread {
        OSCMessage message;
        
        Sender(OSCMessage m) {
            message = m;
        }
        
        public void run() {
            try {
                out.send(message);
            }
            catch(IOException e) {
                
            }
        }
    }
    
    /**
     * Constructs a new Relay object with the specified target address and port. The IP address of the target must be an IPv4 address represented in
     * dot-decimal notation. For example, "127.0.0.1" is a valid target string for sending messages to the loopback network interface.
     * 
     * @param ip the IP address of the target
     * @param p the port number of the target
     */
    public Relay(String ip, int p) {
        ipAddress = ip;
        port = p;
        messageMap = new ConcurrentHashMap<String,OSCMessage>();
        
        try {
            out = new OSCPortOut(InetAddress.getByName(ip), port);
        }
        catch (Exception e) {
            // Do nothing
        }
    }
    
    /**
     * Sets the IP address and port number.
     * 
     * @param ip the IP address of the target
     * @param p the port number of the target
     */
    public void setSocket(String ip, int p) {
        ipAddress = ip;
        port = p;
        
        try {
            out = new OSCPortOut(InetAddress.getByName(ip), port);
        }
        catch (Exception e) {
            // Do nothing
        }
    }
    
    /**
     * Returns the IP address associated with this Relay.
     * 
     * @return the IP address
     */
    public String getIPAddress() {
        return ipAddress;
    }
    
    /**
     * Returns the port number associated with this Relay.
     * 
     * @return the port number
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Sets the label for this Relay.
     * 
     * @param l the label String
     */
    public void setLabel(String l) {
        label = l;
    }
    
    /**
     * Gets the label for this Relay.
     * 
     * @return the label String or an empty String if no label is set
     */
    public String getLabel() {
        return label;
    }
    
    public void setRemoteLabel(String l) {
        remoteLabel = l;
    }
    
    public String getRemoteLabel() {
        return remoteLabel;
    }
    
    public void setRemoteMessageCount(int c) {
        remoteMessageCount = c;
    }
    
    public int getRemoteMessageCount() {
        return remoteMessageCount;
    }
    
    public void setRemoteEcho(boolean e) {
        remoteEcho = e;
    }
    
    public boolean getRemoteEcho() {
        return remoteEcho;
    }
    
    /**
     * Returns all messages associated with this Relay as an array of Strings.
     * 
     * @return a String array containing messages
     */
    public String[] getMessages() {
        return messageMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Returns the number of messages associated with this Relay.
     * 
     * @return the number of messages
     */
    public int getMessageCount() {
        return messageMap.size();
    }
    
    /**
     * Adds a message to this Relay. A message will be added only if it does not currently exist in this Relay.
     * 
     * @param a the message string to add
     */
    public void addMessage(String a) {
        if (!messageMap.containsKey(a)) {
            messageMap.put(a, new OSCMessage(a));
        }
    }
    
    /**
     * Adds an integer argument to all OSC messages contained in this Relay.
     * 
     * @param i the integer to add
     */
    public void addInteger(int i) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(i);
        }
    }
    
    /**
     * Adds an integer argument to the specified OSC message.
     * 
     * @param message the message that will receive the integer
     * @param i the integer to add
     */
    public void addInteger(String message, int i) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(i);
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(i);
        }
    }
    
    /**
     * Adds a long argument to all OSC messages contained in this Relay.
     * 
     * @param l the long to add
     */
    public void addLong(long l) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(l);
        }
    }
    
    /**
     * Adds a long argument to the specified OSC message.
     * 
     * @param message
     * @param l the long to add
     */
    public void addLong(String message, long l) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(l);
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(l);
        }
    }
    
    /**
     * Adds a float argument to all messages contained in this Relay.
     * 
     * @param f the float to add
     */
    public void addFloat(float f) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(f);
        }
    }
    
    /**
     * Adds a float argument to the specified OSC message.
     * 
     * @param message the message that will receive the float argument
     * @param f the float to add
     */
    public void addFloat(String message, float f) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(f);
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(f);
        }
    }
    
    /**
     * Adds a float argument mapped to the provided value range.
     * 
     * @param f the float to add
     * @param min the minimum value for the range
     * @param max the maximum value for the range
     */
    public void addFloat(float f, float min, float max) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(map(f, min, max));
        }
    }
    
    /**
     * Adds a float argument to the specified OSC message that is mapped to the provided value range.
     * 
     * @param message the message that will receive the mapped float argument
     * @param f the float to add
     * @param min the minimum value for the range
     * @param max the maximum value for the range
     */
    public void addFloat(String message, float f, float min, float max) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(map(f, min, max));
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(map(f, min, max));
        }
    }
    
    /**
     * Adds a String argument to all messages contained in this Relay.
     * 
     * @param s the string to add
     */
    public void addString(String s) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(s);
        }
    }
    
    /**
     * Adds a String argument to the specified OSC message.
     * 
     * @param message the message that will receive the string argument
     * @param s the string to add
     */
    public void addString(String message, String s) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(s);
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(s);
        }
    }
    
    /**
     * Adds a blob argument to all messages contained in this Relay.
     * 
     * @param b the blob to add
     */
    public void addBlob(byte[] b) {
        for (OSCMessage m : messageMap.values()) {
            m.addArgument(b);
        }
    }
    
    /**
     * Adds a blob argument to the specified OSC message.
     * 
     * @param message the message that will receive the blob argument
     * @param b the blob to add
     */
    public void addBlob(String message, byte[] b) {
        if (messageMap.containsKey(message)) {
            messageMap.get(message).addArgument(b);
        }
        else {
            messageMap.put(message, new OSCMessage(message));
            messageMap.get(message).addArgument(b);
        }
    }
    
    /**
     * Determines whether the specified message is contained in this Relay.
     * 
     * @param m the specified message
     * @return true if the message is contained, false otherwise
     */
    public boolean containsMessage(String m) {
        if (messageMap.containsKey(m)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Resets the specified message without removing it. Resetting involves clearing the arguments for the message while leaving it
     * available for adding new arguments.
     * 
     * @param a the message to reset
     */
    public void resetMessage(String a) {
        messageMap.replace(a, new OSCMessage(a));
    }
    
    /**
     * Replaces the specified message with a new message.
     * 
     * @param a1 the message to be replaced
     * @param a2 the new message
     */
    public void replaceMessage(String a1, String a2) {
        if (messageMap.containsKey(a1)){
            OSCMessage m = messageMap.get(a1);
            m.setAddress(a2);
            messageMap.put(a2, m);
            messageMap.remove(a1);
        }
    }
    
    /**
     * Removes the specified message.
     * 
     * @param a the message to remove
     */
    public void removeMessage(String a) {
        messageMap.remove(a);
    }
    
    /**
     * Remove all message associated with this Relay.
     */
    public void clearMessages() {
        messageMap.clear();
    }
    
    /**
     * Sends all messages associated with this Relay.
     */
    public void send() {
        for (OSCMessage m : messageMap.values()) {
            new Sender(m).start();
        }
    }
    
    /**
     * Sends the specified message.
     * 
     * @param message the message to send
     */
    public void send(String message) {
        if (messageMap.containsKey(message)) {
            new Sender(messageMap.get(message)).start();
        }
    }
    
    /**
     * Sends all messages specified in the array provided as an argument
     * 
     * @param m an array of messages to send
     */
    public void send(String[] m) {
        for (String s : m) {
            if (messageMap.containsKey(s)) {
                new Sender(messageMap.get(s)).start();
            }
        }
    }
    
    /**
     * Maps the value provided given the specified range.
     * 
     * @param n the value to map
     * @param m1 the first range value
     * @param m2 the second range value
     * @return the mapped value
     */
    private float map(float n, float m1, float m2) {
        return (n*(m2-m1)) + m1;
    }
}
