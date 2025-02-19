/*
 * Author:     Dante Hart
 * Assignment: Program 3
 * Class:      CSI 4321
 *
 */
package fabric.app.server;

import fabric.serialization.*;
import fabric.serialization.Error;
import lybrary.Y;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 * A Thread Class for handle client connections in the server
 */
public class ServerThread extends Thread{

    // The threads logger to log. All thread should reference the same log
    private static Logger threadLog;

    // This server thread's serverSocket. All threads should reference the same serverSocket
    private final ServerSocket serverSocket;

    // The server thread's client Socket to listen on, each Thread has its own client connection
    /**
     * The server thread's client Socket to listen on, each thread has its own client connection
     */
    private Socket clientSocket = null;

    // constant for client timeout in milliseconds
    private static int CLIENT_TIMEOUT = 40000;

    // static context for user/password pairs. All threads should reference the same server hashmap
    private static HashMap<String, String> knownUsers;

    // static context for knowp user count. All threads should reference the same server hashmap
    private static HashMap<String, Integer> userKnowpCount;

    // static context for random number generator. All threads should reference the same generator to ensure unique values
    private static SecureRandom threadRndGen;

    private static Y threadPage;

    // Give all threads access to server static context variable so that all threads may reference the same objects

    /**
     * Overloaded Constructor for a Server Thread
     * @param servSocket The server's socket
     * @param serverLog The Server's log file
     * @param servUsers The mapping of known Users
     * @param serverKnowpCount The mapping of the knowp count for users
     * @param servRand The secure random generator for ID's
     * @param servPage The html page to post to when finished
     */
    public ServerThread(ServerSocket servSocket,Logger serverLog, HashMap<String, String> servUsers, HashMap<String, Integer> serverKnowpCount, SecureRandom servRand, Y servPage){
        this.serverSocket = servSocket;
        threadLog = serverLog;
        knownUsers = servUsers;
        userKnowpCount = serverKnowpCount;
        threadRndGen = servRand;
        threadPage = servPage;
    }

    /**
     * Reads in a Message and does proper validation
     *
     * @param msgInput the MessageInput to decode the message
     *
     */
    private Message readMessage(MessageInput msgInput) throws IOException {
        Message msg = null;
        try{
            msg = Message.decode(msgInput);

            // Referencing the specification first handle any error message before checking for a server message
            if(Objects.equals(msg.getOperation(), Error.MSG_OP)){
                threadLog.log(Level.WARNING,"Received Error: " + msg + " from thread " + this.getId()+ ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
                clientSocket.close();
            }
        } catch(IOException e){
            threadLog.log(Level.SEVERE, "Unable to communicate: " + e.getMessage() + " from " + e.getCause() + " in Thread " + this.getId()+ ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
            clientSocket.close();
        }catch(ValidationException e){
            threadLog.log(Level.SEVERE, "Invalid message: " + e.getMessage() + " from " + e.getCause() + " with token " + e.getBadToken() + " in Thread " + this.getId() + ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
            clientSocket.close();
        }
        return msg;
    }

    /**
     * Setup a MessageInput with the given client Socket
     * @param clientSocket this MessageInput's Client Socket
     * @return a valid MessageInput with the Socket if it could be created
     */
    private MessageInput setupMsgInput(Socket clientSocket) throws IOException {
        MessageInput msgInput = null;
        try{
            InputStream socketInput = clientSocket.getInputStream();
            msgInput = new MessageInput(socketInput);

        }catch(Exception e){
            threadLog.log(Level.SEVERE, "Unable to communicate: " + e.getMessage() + " from " + e.getCause() + " in Thread " + this.getId() + ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
            clientSocket.close();
        }
        return msgInput;
    }

    /**
     * Setup a MessageOutput with the given client Socket
     * @param clientSocket this MessageOutput's client Socket
     * @return a valid MessageOutput with the Socket if it could be created
     */
    private MessageOutput setupMsgOutput(Socket clientSocket){
        MessageOutput msgOutput = null;
        try{
            clientSocket.getOutputStream();
            msgOutput = new MessageOutput(clientSocket.getOutputStream());
        }catch(IOException e){
            threadLog.log(Level.SEVERE, "Unable to communicate: " + e.getMessage() + " from " + e.getCause() + " in Thread " + this.getId()+ ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
        }
        return msgOutput;
    }

    /**
     * Sends a knowp message to the given MessageOutput's Output Stream
     * @param msgOut the MessageOutput
     */
    private void sendFabric(MessageOutput msgOut) throws IOException {
        Fabric myFabric = new Fabric();
        myFabric.encode(msgOut);
    }

    /**
     * Try to Receive an ID message from the client
     * @param msgInput the MessageInput to Read the Message
     */
    private String receiveID(MessageInput msgInput) throws IOException {
        String receivedID = null;
        threadLog.log(Level.INFO, "Thread " + this.getId() + ": Attempting to read in expected ID message");
        // try to receive message from client
        Message msg = readMessage(msgInput);
        // Referencing the specification first handle any error message before checking for a server message
        if(msg instanceof Error){
            threadLog.log(Level.WARNING,"Received Error: " + msg + " from thread " + this.getId()+ ". Terminating client" + clientSocket.getRemoteSocketAddress() + " connection.");
            clientSocket.close();
        }
        // we expect the first message to be an ID message
        else if (msg instanceof ID) {
            ID myID = (ID) msg;
            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Received ID message " + myID);
            receivedID = myID.getID();
        }
        // otherwise error print and CONTINUE
        else {
            threadLog.log(Level.WARNING, "Unexpected message: " + msg + " in Thread " + this.getId());
            clientSocket.close();
        }

        return receivedID;
    }

    /**
     * Tries to send a challenge message to the client
     * @param msgOutput the threads output stream connection to the client
     * @param clng the challenge message to send
     */
    private void sendClng(MessageOutput msgOutput, Challenge clng) throws IOException {
        clng.encode(msgOutput);
    }

    /**
     * Try to Receive a Credentials message from the client
     * @param msgInput the MessageInput to Read the Message
     */
    private String receiveCred(MessageInput msgInput) throws IOException {
        String receivedCred = null;
        threadLog.log(Level.INFO, "Thread " + this.getId() + ": Attempting to read in expected Credentials message");
        // try to receive message from client
        Message msg = readMessage(msgInput);
        // we expect the first message to be an ID message
        if (msg instanceof Credentials) {
            Credentials myCred = (Credentials) msg;
            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Received Credentials message " + myCred);
            receivedCred = myCred.getHash();
        }
        // otherwise error print and CONTINUE
        else {
            threadLog.log(Level.WARNING, "Unexpected message: " + msg + " in Thread " + this.getId());
            clientSocket.close();
        }

        return receivedCred;
    }

    /**
     * Send an error message to the client
     * @param msgOut the message output that holds the outputstream
     * @param errMsg the error message to send
     * @throws IOException If I/O Error when sending
     */
    private void sendError(MessageOutput msgOut, Error errMsg) throws IOException {
        errMsg.encode(msgOut);
    }

    /**
     * Sends an Ack message to the client
     * @param msgOut the message output that holds the outputstream
     * @throws IOException If I/O Error when sending
     */
    private void sendAck(MessageOutput msgOut) throws IOException {
        Ack servAck = new Ack();
        servAck.encode(msgOut);
    }

    @Override
    public void run() {
        while(true) {
            try {
                threadLog.log(Level.INFO, "Thread " + this.getId() + ": Listening for Client Connection...");
                clientSocket = serverSocket.accept();
                // after client accepted set the timeout
                clientSocket.setSoTimeout(CLIENT_TIMEOUT);

                // If client socket could be connected proceed with protocol, otherwise let the connection terminate
                threadLog.log(Level.INFO, "Thread " + this.getId() + ": Successfully able to connect to client on " + clientSocket.getRemoteSocketAddress());

                // Initialize MessageInput and MessageOutput
                threadLog.log(Level.FINE, "Thread " + this.getId() + ": Setting Up Message Input.");
                MessageInput threadInput = setupMsgInput(clientSocket);
                threadLog.log(Level.FINE, "Thread " + this.getId() + ": Setting Up Message Output.");
                MessageOutput threadOutput = setupMsgOutput(clientSocket);

                threadLog.log(Level.FINE, "Thread " + this.getId() + ": Sending Fabric Message to " + clientSocket.getRemoteSocketAddress());
                // After the message inputs and outputs have been set up send a fabric message to the client
                sendFabric(threadOutput);

                // Read in an ID message, anything else terminate client connection
                String userID = receiveID(threadInput);
                // If userID was successfully read in,
                // Verify if we know user
                // then try to send a CLNG message
                if(userID != null && knownUsers.containsKey(userID)){
                    threadLog.log(Level.INFO,"Thread " + this.getId() + ": Received User " + userID + " and user exists!");
                    // create a Challenge Message
                    // ensure the random number is never negative
                    int clngValue = threadRndGen.nextInt(0, Integer.MAX_VALUE);
                    // define minimum scope for server challenge
                    threadLog.log(Level.FINE, "Thread " + this.getId() + ": Generate challenge value and creating challenge message");
                    Challenge servClng = null;
                    try{
                        servClng = new Challenge(String.valueOf(clngValue));
                    } catch (ValidationException e) {
                        threadLog.log(Level.SEVERE, "Validation failed: " + e.getMessage() + " from " + e.getCause() + e.getBadToken());
                        threadLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
                        exit(7);
                    }
                    threadLog.log(Level.FINE, "Thread " + this.getId() + ": Sending Challenge to " + clientSocket.getRemoteSocketAddress());
                    // try to send out challenge
                    sendClng(threadOutput, servClng);
                    String expectedValue = null;
                    try {
                        expectedValue = HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("MD5").digest((servClng.getNonce() + knownUsers.get(userID)).getBytes(Message.CHAR_ENC)));
                    } catch (NoSuchAlgorithmException e) {
                        threadLog.log(Level.SEVERE, "Validation failed: " + e.getMessage() + " from " + e.getCause());
                        threadLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
                        exit(7);
                    }

                    String userCred = receiveCred(threadInput);

                    // If the credentials are not equal to one another send an error message
                    if(!Objects.equals(userCred, expectedValue)){
                        // Try to create and send error message to the client
                        Error credError = null;
                        try {
                            credError = new Error(500, "Unable to authenticate");
                        } catch (ValidationException e) {
                            threadLog.log(Level.SEVERE, "Validation failed: " + e.getMessage() + " from " + e.getCause());
                            threadLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
                            exit(8);
                        }
                        // Send the error to the client
                        threadLog.log(Level.WARNING, "Error Detected: " + credError);
                        threadLog.log(Level.FINE, "Sending Error Message to Client.");
                        sendError(threadOutput, credError);
                        clientSocket.close();
                    }
                    else{
                        // Otherwise A valid Credentials has been received send an ack
                        threadLog.log(Level.INFO, "Valid Credentials Received from: " + userID + " in Thread " + this.getId());
                        threadLog.log(Level.FINE, "Responding with ACK message in thread " + this.getId());
                        sendAck(threadOutput);
                        // Try to receive a bout or knowp
                        Message protocolMessage = readMessage(threadInput);
                        // if the message is a bout, send ack and go with bout protocol
                        if(protocolMessage instanceof Bout){
                            // A valid Bout has been received send ack
                            threadLog.log(Level.INFO, "Valid Bout Received from: " + userID + " in Thread " + this.getId());
                            threadLog.log(Level.FINE, "Responding with ACK message in thread " + this.getId());
                            sendAck(threadOutput);
                            Bout boutMsg = (Bout) protocolMessage;
                            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Posting Bout Message from " + userID);
                            threadLog.log(Level.FINE, "Thread " + this.getId() + ": Bout Message " + boutMsg);
                            threadPage.updateWithImage(userID + ": LtsRL #" + boutMsg.getCategory(), boutMsg.getImage());
                            // Update legitimate message list for stitch server
                            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Adding Bout Message to Stitch list");
                            stitch.app.server.Server.addLegitMessage(boutMsg);
                        }
                        else if(protocolMessage instanceof Knowp){
                            // A valid knowp has been received send ack
                            threadLog.log(Level.INFO, "Valid Knowp Received from: " + userID + " in Thread " + this.getId());
                            threadLog.log(Level.FINE, "Responding with ACK message in Thread " + this.getId());
                            sendAck(threadOutput);
                            // Increment the users Knowp Count
                            userKnowpCount.replace(userID, userKnowpCount.get(userID), userKnowpCount.get(userID) + 1);
                            threadLog.log(Level.INFO, "Posting Knowp to Webpage in Thread " + this.getId());
                            String knowpString = userID + ": Knowp " + userKnowpCount.get(userID);
                            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Posting Knowp Message from " + userID);
                            threadLog.log(Level.FINE, "Thread " + this.getId() + ": Knowp Message " + knowpString);
                            threadPage.update(knowpString);
                            // Update legitimate message list for stitch server
                            threadLog.log(Level.INFO, "Thread " + this.getId() + ": Adding Knowp Message to Stitch list");
                            stitch.app.server.Server.addLegitMessage(protocolMessage);
                        }
                        else{
                            threadLog.log(Level.WARNING, "Unexpected message: " + protocolMessage + " in Thread " + this.getId());
                            clientSocket.close();
                        }
                    }
                }
                else{
                    // Try to create and send error message to the client
                    Error userError = null;
                    try {
                        userError = new Error(500, "No such user " + userID);
                    } catch (ValidationException e) {
                        threadLog.log(Level.SEVERE, "Validation failed: " + e.getMessage() + " from " + e.getCause());
                        threadLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
                        exit(8);
                    }
                    // Send the error to the client
                    threadLog.log(Level.WARNING, "Error Detected: " + userError);
                    threadLog.log(Level.FINE, "Sending Error Message to Client.");
                    sendError(threadOutput, userError);
                    clientSocket.close();
                }

            } catch (IOException e) {
                threadLog.log(Level.SEVERE, "Unable to communicate: " + e.getMessage() + " from " + e.getCause() + " in Thread " + this.getId());
            }
        }
    }
}
