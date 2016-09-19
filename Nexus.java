package junctionbox;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Nexus class implements the NDEF (Nexus Data Exchange Format) specification for managing connections and sharing
 * messages between nodes on a network. Nodes are represented in a Nexus by one or more {@link junctionbox.Relay} objects.
 * 
 * @author Lawrence Fyfe
 * @version 0.99
 * @see <a href="http://innovis.cpsc.ucalgary.ca/Research/NDEFSpecification">The Nexus Data Exchange Format Specification</a>
 */
public class Nexus implements OSCListener {
    /**
     * The IP address of the target.
     */
    private String defaultAddress;
    
    /**
     * The port number of the target
     */
    private int defaultPort;
    
    /**
     * The list of Relays stored in this Junction.
     */
    private CopyOnWriteArrayList<Relay> relayList;
    
    /**
     * The IP address for incoming OSC messages.
     */
    private String listenAddress;
    
    /**
     * The port for incoming OSC messages.
     */
    private int listenPort;
    
    /**
     * The OSCPortIn listener for incoming OSC messages.
     */
    private OSCPortIn oscIn;
    
    /**
     * NDEF connection request string.
     */
    private static final String NDEF_CONNECTION_REQUEST = "/ndef/connection/request";
    
    /**
     * NDEF connection accept string.
     */
    private static final String NDEF_CONNECTION_ACCEPT = "/ndef/connection/accept";
    
    /**
     * NDEF connection label.
     */
    private static final String NDEF_CONNECTION_LABEL = "/ndef/connection/label";
    
    /**
     * NDEF connection mark response.
     */
    private static final String NDEF_CONNECTION_MARK = "/ndef/connection/mark";
    
    /**
     * NDEF connection ping.
     */
    private static final String NDEF_CONNECTION_PING = "/ndef/connection/ping";
    
    /**
     * NDEF connection echo response.
     */
    private static final String NDEF_CONNECTION_ECHO = "/ndef/connection/echo";
    
    /**
     * NDEF message request string.
     */
    private static final String NDEF_MESSAGE_REQUEST = "/ndef/message/request";
    
    /**
     * NDEF message reply string.
     */
    private static final String NDEF_MESSAGE_REPLY = "/ndef/message/reply";
    
    /**
     * NDEF message count.
     */
    private static final String NDEF_MESSAGE_COUNT = "/ndef/message/count";
    
    /**
     * NDEF message tally response.
     */
    private static final String NDEF_MESSAGE_TALLY = "/ndef/message/tally";
    
    /**
     * NDEF message add string.
     */
    private static final String NDEF_MESSAGE_ADD = "/ndef/message/add";
    
    /**
     * NDEF message remove string.
     */
    private static final String NDEF_MESSAGE_REMOVE = "/ndef/message/remove";
    
    /**
     * NDEF message replace string.
     */
    private static final String NDEF_MESSAGE_REPLACE = "/ndef/message/replace";
    
    /**
     * List of rejected NDEF messages as Strings.
     */
    private ArrayList<String> rejectedList;
    
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
     * A Nexus handles connection and message management with remote (or local) network nodes.
     */
    public Nexus() {
        relayList = new CopyOnWriteArrayList<Relay>();
        rejectedList = new ArrayList<String>();
        
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
     * A Nexus handles connection and message management with remote (or local) network nodes. Using this constructor sets a default
     * target address and port. Additional nodes can still be added to the Nexus manually.
     * 
     * @param address the default IP address
     * @param port the default port number
     */
    public Nexus(String address, int port) {
        defaultAddress = address;
        defaultPort = port;
        
        relayList = new CopyOnWriteArrayList<Relay>();
        rejectedList = new ArrayList<String>();
        
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
     * Returns a local IP address from the local machine.
     * 
     * @return a String representing the IP address or an empty String
     */
    public String getLocalAddress() {
        String address = "";
        
        try {
            Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
            
            while (interfaceList.hasMoreElements()) {
                Enumeration<InetAddress> addressList = interfaceList.nextElement().getInetAddresses();
                
                while (addressList.hasMoreElements()) {
                    InetAddress ia = addressList.nextElement();
                    
                    if (!ia.isLoopbackAddress()) {
                        address = ia.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        
        return address;
    }
    
    /**
     * Listen for incoming OSC messages on the given port. The IP address used will be whatever address is returned by
     * the {@link #getLocalAddress()} method. If this is not the desired listening address, try
     * {@link #startListening(String address, int port)} with the desired address instead.
     * 
     * @param port the port number
     */
    public void startListening(int port) {
        startListening(getLocalAddress(), port);
    }
    
    /**
     * Listen for incoming OSC messages on the given socket.
     * 
     * @param address the IP address
     * @param port the port number
     */
    public void startListening(String address, int port) {
        listenAddress = address;
        listenPort = port;
        
        try {
            oscIn = new OSCPortIn(listenPort);
            oscIn.addListener(NDEF_CONNECTION_ACCEPT, this);
            oscIn.addListener(NDEF_CONNECTION_LABEL, this);
            oscIn.addListener(NDEF_CONNECTION_MARK, this);
            oscIn.addListener(NDEF_CONNECTION_PING, this);
            oscIn.addListener(NDEF_CONNECTION_ECHO, this);
            oscIn.addListener(NDEF_MESSAGE_REPLY, this);
            oscIn.addListener(NDEF_MESSAGE_COUNT, this);
            oscIn.addListener(NDEF_MESSAGE_TALLY, this);
            oscIn.addListener(NDEF_MESSAGE_ADD, this);
            oscIn.addListener(NDEF_MESSAGE_REMOVE, this);
            oscIn.addListener(NDEF_MESSAGE_REPLACE, this);
            oscIn.startListening();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Stop listening for incoming OSC messages.
     */
    public void stopListening() {
        if (oscIn != null && oscIn.isListening()) {
            oscIn.stopListening();
            oscIn.close();
        }
    }
    
    /**
     * Returns the current IP address used to listen for incoming OSC messages.
     * 
     * @return the IP address as a String
     */
    public String getListeningAddress() {
        return listenAddress;
    }
    
    /**
     * Returns the current port number used to listen for incoming OSC messages.
     * 
     * @return an int representing the listening port
     */
    public int getListeningPort() {
        return listenPort;
    }
    
    /**
     * Sends an NDEF connection request to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     */
    public void sendConnectionRequest(String ip, int port) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_CONNECTION_REQUEST);
        
        // Address of requester (this Dispatcher instance)
        r.addString(NDEF_CONNECTION_REQUEST, listenAddress);
        r.addInteger(NDEF_CONNECTION_REQUEST, listenPort);
        
        r.send(NDEF_CONNECTION_REQUEST);
    }
    
    /**
     * Sends an NDEF connection request with a label to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     * @param label the label
     */
    public void sendConnectionRequest(String ip, int port, String label) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_CONNECTION_REQUEST);
        
        // Address of requester (this Dispatcher instance)
        r.addString(NDEF_CONNECTION_REQUEST, listenAddress);
        r.addInteger(NDEF_CONNECTION_REQUEST, listenPort);
        r.addString(NDEF_CONNECTION_REQUEST, label);
        
        r.send(NDEF_CONNECTION_REQUEST);
    }
    
    /**
     * Sends an NDEF connection request to the IP address and port of the default target node.
     */
    public void sendConnectionRequest() {
        sendConnectionRequest(defaultAddress, defaultPort);
    }
    
    /**
     * Sends an NDEF connection request with a label to the IP address and port of the default target node.
     * 
     * @param label the label
     */
    public void sendConnectionRequest(String label) {
        sendConnectionRequest(defaultAddress, defaultPort, label);
    }
    
    /**
     * Determines whether the specified combination of IP address and port number is connected.
     * 
     * @param ip the IP address to check
     * @param port the port number to check
     * @return true if connected, false otherwise
     */
    public boolean isConnected(String ip, int port) {
        for (Relay r : relayList) {
            if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determines whether the specified Relay is connected.
     * 
     * @param r the Relay to check
     * @return true if connected, false otherwise
     */
    public boolean isConnected(Relay r) {
        if (relayList.contains(r)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Sends an NDEF connection label command to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     */
    public void sendConnectionLabel(String ip, int port, String label) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_CONNECTION_LABEL);
        
        r.addString(NDEF_CONNECTION_LABEL, listenAddress);
        r.addInteger(NDEF_CONNECTION_LABEL, listenPort);
        r.addString(label);
        
        r.send(NDEF_CONNECTION_LABEL);
    }
    
    /**
     * Sends an NDEF connection label command to the default target.
     */
    public void sendConnectionLabel(String label) {
        sendConnectionLabel(defaultAddress, defaultPort, label);
    }
    
    /**
     * Sends an NDEF connection ping to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     */
    public void sendConnectionPing(String ip, int port) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_CONNECTION_PING);
        
        r.addString(NDEF_CONNECTION_PING, listenAddress);
        r.addInteger(NDEF_CONNECTION_PING, listenPort);
        
        r.send(NDEF_CONNECTION_PING);
        
        // Look for local Relay and set remote echo status until echo message received
        for (Relay local : relayList) {
            if (local.getIPAddress().equals(ip) && local.getPort() == port) {
                local.setRemoteEcho(false);
            }
        }
    }
    
    /**
     * Sends an NDEF connection ping to the default target.
     */
    public void sendConnectionPing() {
        sendConnectionPing(defaultAddress, defaultPort);
    }
    
    /**
     * Sends an NDEF connection echo to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     */
    public void sendConnectionEcho(String ip, int port) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_CONNECTION_ECHO);
        
        r.addString(NDEF_CONNECTION_ECHO, listenAddress);
        r.addInteger(NDEF_CONNECTION_ECHO, listenPort);
        
        r.send(NDEF_CONNECTION_ECHO);
    }
    
    /**
     * Sends an NDEF connection echo to the default target.
     */
    public void sendConnectionEcho() {
        sendConnectionEcho(defaultAddress, defaultPort);
    }
    
    /**
     * Sends an NDEF message request to the specified IP address and port.
     * 
     * @param ip the target IP address
     * @param port the target port
     */
    public void sendMessageRequest(String ip, int port) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_MESSAGE_REQUEST);
        
        // Socket of requester (this Dispatcher instance)
        r.addString(NDEF_MESSAGE_REQUEST, listenAddress);
        r.addInteger(NDEF_MESSAGE_REQUEST, listenPort);
        
        r.send(NDEF_MESSAGE_REQUEST);
    }
    
    /**
     * Sends an NDEF message request to the IP address and port of the default target node.
     */
    public void sendMessageRequest() {
        sendMessageRequest(defaultAddress, defaultPort);
    }
    
    /**
     * Sends an NDEF message count request to the specified IP address and port.
     * 
     * @param ip the IP address
     * @param port the port number
     */
    public void sendMessageCount(String ip, int port) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_MESSAGE_COUNT);
        
        r.addString(NDEF_MESSAGE_COUNT, listenAddress);
        r.addInteger(NDEF_MESSAGE_COUNT, listenPort);
        
        r.send(NDEF_MESSAGE_COUNT);
    }
    
    /**
     * Sends an NDEF message count request to the default target.
     */
    public void sendMessageCount() {
        sendMessageCount(defaultAddress, defaultPort);
    }
    
    /**
     * Sends an add message request to the specified target.
     * 
     * @param ip the target IP address
     * @param port the target port
     * @param message the message to add
     */
    public void sendMessageAdd(String ip, int port, String message) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_MESSAGE_ADD);
        
        // Socket of requester (this Dispatcher instance)
        r.addString(NDEF_MESSAGE_ADD, listenAddress);
        r.addInteger(NDEF_MESSAGE_ADD, listenPort);
        r.addString(message);
        
        r.send(NDEF_MESSAGE_ADD);
    }
    
    /**
     * Sends an add message request to the default target.
     * 
     * @param m the message to add
     */
    public void sendMessageAdd(String m) {
        sendMessageAdd(defaultAddress, defaultPort, m);
    }
    
    /**
     * Sends a remove message request to the specified target.
     * 
     * @param ip the target IP address
     * @param port the target port
     * @param message the message to remove
     */
    public void sendMessageRemove(String ip, int port, String message) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_MESSAGE_REMOVE);
        
        // Socket of requester (this Dispatcher instance)
        r.addString(NDEF_MESSAGE_REMOVE, listenAddress);
        r.addInteger(NDEF_MESSAGE_REMOVE, listenPort);
        r.addString(message);
        
        r.send(NDEF_MESSAGE_REMOVE);
    }
    
    /**
     * Sends a remove message request to the default target.
     * 
     * @param m the message to remove
     */
    public void sendMessageRemove(String m) {
        sendMessageRemove(defaultAddress, defaultPort, m);
    }
    
    /**
     * Sends a replace message request to the specified target.
     * 
     * @param ip the target IP address
     * @param port the target port
     * @param oldMessage the message to replace
     * @param newMessage the new message
     */
    public void sendMessageReplace(String ip, int port, String oldMessage, String newMessage) {
        Relay r = new Relay(ip, port);
        r.addMessage(NDEF_MESSAGE_REPLACE);
        
        // Socket of requester (this Dispatcher instance)
        r.addString(NDEF_MESSAGE_REPLACE, listenAddress);
        r.addInteger(NDEF_MESSAGE_REPLACE, listenPort);
        r.addString(oldMessage);
        r.addString(newMessage);
        
        r.send(NDEF_MESSAGE_REPLACE);
    }
    
    /**
     * Sends a replace message request to the default target.
     * 
     * @param oldM the message to replace
     * @param newM the new message
     */
    public void sendMessageReplace(String oldM, String newM) {
        sendMessageReplace(defaultAddress, defaultPort, oldM, newM);
    }
    
    /**
     * Accepts certain OSC messages. Messages currently accepted are Nexus Data Exchange Format (NDEF).
     * 
     * @param time the time associated with the message
     * @param message the OSC message
     */
    public void acceptMessage(Date time, OSCMessage message) {
        String address = message.getAddress();
        Object[] args = message.getArguments();
        boolean rejected = false;
        String ip = "";
        int port = 0;
        
        // No address = no message!
        if (address != null && address.length() > 0) {
            // There must be at least two arguments, IP address and port with optional message String
            if (args.length >= 2) {
                // Beware the casting!
                try {
                    // This should be the target socket
                    ip = (String)args[0];
                    port = (Integer)args[1];
                }
                catch (ClassCastException e) {
                    rejected = true;
                }
                
                if (!rejected) {
                    // The NDEF connection is accepted
                    if (address.equals(NDEF_CONNECTION_ACCEPT)) {
                        boolean found = false;
                        String label = "";
                        
                        // Look for optional label String
                        if (args.length == 3) {
                            try {
                                label = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                        }
                        
                        for (Relay r : relayList) {
                            if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                found = true;
                                r.setLabel(label);
                                break;
                            }
                        }
                        
                        // Only create a new Relay if the socket does not match
                        if (!found) {
                            Relay r = new Relay(ip, port);
                            r.setLabel(label);
                            relayList.add(r);
                        }
                    }
                    else if (address.equals(NDEF_CONNECTION_LABEL)) {
                        // The label is always the third argument
                        if (args.length == 3) {
                            String label = "";
                            
                            try {
                                label = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        r.setLabel(label);
                                        break;
                                    }
                                }
                            }
                        }
                        else {
                            rejected = true;
                        }
                    }
                    else if (address.equals(NDEF_CONNECTION_MARK)) {
                        // Look for optional third label argument
                        if (args.length == 3) {
                            String label = "";
                            
                            try {
                                label = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        // Set the remote label rather than the local label
                                        r.setRemoteLabel(label);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    else if (address.equals(NDEF_CONNECTION_PING)) {
                        for (Relay r : relayList) {
                            if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                // Send echo back to sender
                                sendConnectionEcho(ip, port);
                            }
                        }
                    }
                    else if (address.equals(NDEF_CONNECTION_ECHO)){
                        for (Relay r : relayList) {
                            if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                // Mark Relay as still connected?
                            }
                        }
                    }
                    else if (address.equals(NDEF_MESSAGE_REPLY)) {
                        // Message replies should have 3 arguments
                        if (args.length == 3) {
                            String m = "";
                            try {
                                // The message should be the 3rd argument
                                m = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        // Only add new messages
                                        if (!r.containsMessage(m)) {
                                            r.addMessage(m);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // No message String included?
                            rejected = true;
                        }
                    }
                    /*else if (address.equals(NDEF_MESSAGE_COUNT)) {
                        for (Relay r : relayList) {
                            if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                // Send count of messages in local Relay???
                            }
                        }
                    }*/
                    else if (address.equals(NDEF_MESSAGE_TALLY)) {
                        if (args.length == 3) {
                            int tally = 0;
                            try {
                                // The message tally is the third argument
                                tally = (int)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        r.setRemoteMessageCount(tally);
                                    }
                                }
                            }
                        }
                    }
                    else if (address.equals(NDEF_MESSAGE_ADD)) {
                        // Add message to local list
                        if (args.length == 3) {
                            String m = "";
                            try {
                                // The message should be the 3rd argument
                                m = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        // Do not add duplicate messages
                                        if (!r.containsMessage(m)) {
                                            r.addMessage(m);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // No message String included?
                            rejected = true;
                        }
                    }
                    else if (address.equals(NDEF_MESSAGE_REMOVE)) {
                        // Remove message from local list
                        if (args.length == 3) {
                            String m = "";
                            try {
                                // The message should be the 3rd argument
                                m = (String)args[2];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        // Remove the message if it's there
                                        if (r.containsMessage(m)) {
                                            r.removeMessage(m);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // No message String included?
                            rejected = true;
                        }
                    }
                    else if (address.equals(NDEF_MESSAGE_REPLACE)) {
                        // Replace message in local list
                        if (args.length == 4) {
                            // m2 replaces m1
                            String m1 = "";
                            String m2 = "";
                            try {
                                // The messages should be the 3rd and 4th arguments
                                m1 = (String)args[2];
                                m2 = (String)args[3];
                            }
                            catch (ClassCastException e) {
                                rejected = true;
                            }
                            
                            if (!rejected) {
                                for (Relay r : relayList) {
                                    if (r.getIPAddress().equals(ip) && r.getPort() == port) {
                                        if (r.containsMessage(m1)) {
                                            r.replaceMessage(m1, m2);
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            // No message String included?
                            rejected = true;
                        }
                    }
                }
            }
            else {
                rejected = true;
                // Not enough arguments
            }
        }
        
        if (rejected) {
            // First add OSC address pattern
            StringBuffer buffer = new StringBuffer(address);
            
            // Append arguments to address pattern as Strings
            for (int i = 0; i < args.length; i++) {
                buffer.append(" " + args[i].toString());
            }
            
            // Add message to reject list
            rejectedList.add(buffer.toString());
        }
    }
    
    /**
     * Returns the specified Relay.
     * 
     * @param r the index of the Relay
     * @return the specified Relay
     */
    public Relay getRelay(int r) {
        return relayList.get(r);
    }
    
    /**
     * Returns the NDEF message mapping Relays associated with this Dispatcher as an array.
     * 
     * @return an array of Relays
     */
    public Relay[] getRelays() {
        return relayList.toArray(new Relay[0]);
    }
    
    /**
     * Returns the number of Relays contained in this Dispatcher.
     * 
     * @return the number of Relays
     */
    public int getRelayCount() {
        return relayList.size();
    }
    
    /**
     * Returns any rejected NDEF messages.
     * 
     * @return an array of NDEF message Strings
     */
    public String[] getRejectedMessages() {
        return rejectedList.toArray(new String[0]);
    }
    
    /**
     * Removes all rejected NDEF messages.
     */
    public void clearRejectedMessages() {
        rejectedList.clear();
    }
    
    /**
     * Saves Relay data to XML.
     */
    private void saveRelays() {
        // Check for Relays to save
        if (relayList.size() > 0) {
            for (Relay r : relayList) {
                Element relayElement = writeDoc.createElement("relay");
                
                Element label = writeDoc.createElement("label");
                label.appendChild(writeDoc.createTextNode(r.getLabel()));
                relayElement.appendChild(label);
                
                Element ip = writeDoc.createElement("ipAddress");
                ip.appendChild(writeDoc.createTextNode(r.getIPAddress()));
                relayElement.appendChild(ip);
                
                Element port = writeDoc.createElement("port");
                port.appendChild(writeDoc.createTextNode(Integer.toString(r.getPort())));
                relayElement.appendChild(port);
                
                // Save message list
                String[] messageList = r.getMessages();
                
                if (messageList.length > 0) {
                    for (int i = 0; i < messageList.length; i++) {
                        Element message = writeDoc.createElement("message");
                        message.appendChild(writeDoc.createTextNode(messageList[i]));
                        relayElement.appendChild(message);
                    }
                }
                
                writeRoot.appendChild(relayElement);
            }
        }
    }
    
    /**
     * Saves Junction, Relay, and Event data to XML.
     * 
     * @param output the file to save to
     */
    public void saveXML(OutputStream output) {
        saveRelays();
        
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
     * Loads Relay data from XML.
     */
    private void loadRelays() {
        if (readDoc != null) {
            // The root node must be <junctionbox>
            if (readRoot.getNodeName().equals("junctionbox")) {
                NodeList relayNodes = readDoc.getElementsByTagName("relay");
                
                for (int i = 0; i < relayNodes.getLength(); i++) {
                    NodeList valueNodes = relayNodes.item(i).getChildNodes();
                    
                    String label = "";
                    String ip = "";
                    int port = 0;
                    ArrayList<String> messages = new ArrayList<String>();
                    
                    for (int j = 0; j < valueNodes.getLength(); j++) {
                        Node value = valueNodes.item(j);
                        
                        if (value.getNodeName().equals("label")) {
                            label = value.getTextContent();
                        }
                        else if (value.getNodeName().equals("ipAddress")) {
                            ip = value.getTextContent();
                        }
                        else if (value.getNodeName().equals("port")) {
                            port = Integer.valueOf(value.getTextContent());
                        }
                        else if (value.getNodeName().equals("message")) {
                            messages.add(value.getTextContent());
                        }
                    }
                    
                    // Create Relay
                    Relay r = new Relay(ip, port);
                    r.setLabel(label);
                    
                    for (int k = 0; k < messages.size(); k++){
                        r.addMessage(messages.get(k));
                    }
                    relayList.add(r);
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
        
        loadRelays();
    }
}
