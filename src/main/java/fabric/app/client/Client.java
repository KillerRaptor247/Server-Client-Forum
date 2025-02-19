/*
 * Author:     Dante Hart
 * Assignment: Program 2
 * Class:      CSI 4321
 *
 */
package fabric.app.client;

import fabric.serialization.*;
import fabric.serialization.Error;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import static java.lang.System.exit;

/**
 * Class for handle the client protocol
 */
public class Client {
    // constants for testing BOUT arg length and KNOWP arg length

    /**
     * Argument length for Bout
     */
    final static protected int BOUT_ARG_LENGTH = 7;

    /**
     * Argument length for knowp
     */
    final static protected int KNOWP_ARG_LENGTH = 5;

    /**
     * Boolean variable flag to check whether we are sending a bout or a knowp message
     */
    static private boolean sendBoutMode = false;


    /**
     * Reads in a Message and does proper validation
     *
     * @param msgInput the MessageInput to decode the message
     *
     */
    private static Message readMessage(MessageInput msgInput){
        Message msg = null;
        try{
            msg = Message.decode(msgInput);
            // Referencing the specification first handle any error message before checking for a server message
            if(Objects.equals(msg.getOperation(), Error.MSG_OP)){
                System.err.println("Error: " + msg);
                exit(8);
            }
        } catch(IOException e){
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(6);
        }catch(ValidationException e){
            System.err.println("Invalid Message: " + e.getMessage() + " from " + e.getCause() + " with token " + e.getBadToken());
            exit(7);
        }
        return msg;
    }

    /**
     * verifies the command line parameters passed in for a client
      */
    private static void commandLineValidation(String[] args){
        // first validate arguments passed into Client
        // The client should have either 5 or 7 command line parameters
        // 5 command line parameters for KNOWP
        // 7 command line parameters for BOUT
        if(args.length != BOUT_ARG_LENGTH && args.length != KNOWP_ARG_LENGTH){
            System.err.println("Validation failed: Invalid number of arguments given. Usage: <server> <port> <userid> <password> <request>");
            exit(1);
        }

        // validate a knowp if we have KNOWP arguments
        if(args.length == KNOWP_ARG_LENGTH){
            if(!Objects.equals(args[4], Knowp.MSG_OP)){
                System.err.println("Validation Failed: Invalid Message Operation Given for KNOWP arguments");
                exit(11);
            }
        }
        else{
            // validate Bout message operation
            if(!Objects.equals(args[4], Bout.MSG_OP)){
                System.err.println("Validation Failed: Invalid Message Operation Given for BOUT arguments");
                exit(12);
            }
            // set the sendBoutMode flag that we are sending a bout
            sendBoutMode = true;
        }
    }

    /**
     * Validate if a userID is validate by creating an ID with the user Message
     * @param userID the userID
     * @return the ID Message if the ID is valid
     */
    private static ID validateUser(String userID){
        ID myIDMsg = null;
        try {
            myIDMsg = new ID(userID);
        } catch (ValidationException e) {
            System.err.println("Invalid Message: " + e.getMessage() + " from " + e.getCause() + " with token " + e.getBadToken());
            exit(6);
        }
        return myIDMsg;
    }

    /**
     * Validates a bout message with the given category and imagepath given in from the command line
     * @param category the image category
     * @param imagePath the path of the image
     * @return the bout message if valid
     */
    private static Bout validateBout(String category, String imagePath){
        Bout myBoutMsg = null;
        try{
            myBoutMsg = new Bout(category, Files.readAllBytes(Path.of(imagePath)));
        } catch (ValidationException e) {
            System.err.println("Validation Failed: Invalid Arguments for Bout: " + e.getMessage() + " with " + e.getBadToken() + " from " + e.getCause());
            exit(13);
        } catch (IOException e) {
            System.err.println("Validation Failed: Invalid Image from " + e.getMessage());
            exit(14);
        }
        return myBoutMsg;
    }

    /**
     * Attempt to create a socket with the given port number and server name
     * @param portNumber port number given from command line
     * @param serverName server name given from command line
     * @return the socket if it could be created
     */
    private static Socket connectSocket(String portNumber, String serverName){
        // initialize server port variable for try catch compiler error
        int serverPort = 0;
        // Try to parse out the server port
        try{
            serverPort = Integer.parseInt(portNumber);
        }catch(NumberFormatException e){
            System.err.println("Unable to communicate: " + e.getMessage());
            exit(2);
        }

        // try to create a socket that is connected to the server on the specified port
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(serverName, serverPort);
        }catch(Exception e){
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(3);
        }
        return clientSocket;
    }

    /**
     * Setup a MessageInput with the given Socket
     * @param socket this MessageInput's Socket
     * @return a valid MessageInput with the Socket if it could be created
     */
    private static MessageInput setupMsgInput(Socket socket){
        MessageInput msgInput = null;
        try{
            InputStream socketInput = socket.getInputStream();
            msgInput = new MessageInput(socketInput);

        }catch(Exception e){
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(4);
        }
        return msgInput;
    }

    /**
     * Setup a MessageOutput with the given Socket
     * @param socket this MessageOutput's Socket
     * @return a valid MessageOutput with the Socket if it could be created
     */
    private static MessageOutput setupMsgOutput(Socket socket){
        MessageOutput msgOutput = null;
        try{
            socket.getOutputStream();
            msgOutput = new MessageOutput(socket.getOutputStream());
        }catch(Exception e){
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(5);
        }
        return msgOutput;
    }

    /**
     * Try to Receive a fabric message from the server
     * @param msgInput the MessageInput to Read the Message
     */
    private static void receiveFabric(MessageInput msgInput){
        boolean fabReceived = false;

        while(!fabReceived) {
            // now that input and output streams are created, try to receive a message from the server
            Message msg = readMessage(msgInput);
            // we expect the first message to be a fabric message
            // if we receive it send our ID message
            if (msg instanceof Fabric) {
                // set the fabric received flag
                fabReceived = true;
            }
            // otherwise error print and CONTINUE
            else {
                System.err.println("Unexpected message: " + msg);
            }
        }
    }

    /**
     *
     * @param msgOut the MessageOutput to write the ID message after a Fabric Message is received
     * @param idMsg the ID message to immediately write after a successful Fabric recieved
     */
    private static void sendID(MessageOutput msgOut, ID idMsg){
        try{
            idMsg.encode(msgOut);
        }
        catch (IOException e) {
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(7);
        }
    }

    /**
     * Try to Receive a challenge message from the server
     * @param msgIn the MessageInput to Read the Message
     * @return the Challenge message read in from the server if a valid message
     */
    private static Challenge receiveChallenge(MessageInput msgIn) {
        // Keep trying to receive challenge until error
        boolean clngReceived = false;
        // Define minimum scope for message
        Message msg = null;
        while (!clngReceived) {
            msg = readMessage(msgIn);
            if (msg instanceof Challenge) {
                clngReceived = true;
            }
            // otherwise error print and CONTINUE
            else {
                System.err.println("Unexpected message: " + msg);
            }
        }
        return (Challenge) msg;
    }

    /**
     * Attempts to write a credentials message to the OutputStream with the password given from the command line
     * @param clngMsg the Challenge message received earlier from the server
     * @param msgOut the MessageOutput to write to the outputstream
     * @param hashPass the password given from the command line
     */
    private static void sendCred(Challenge clngMsg, MessageOutput msgOut, String hashPass){
        try{
            // Create the Credentials String from computing the md5 hash
            String credString = HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("MD5").digest((clngMsg.getNonce() + hashPass).getBytes(Message.CHAR_ENC)));
            // Then create credentials message to send to server
            Credentials myCredMsg = new Credentials(credString);
            myCredMsg.encode(msgOut);
        }catch(ValidationException e){
            System.err.println("Invalid Message: " + e.getMessage() + " from " + e.getCause() + " with token " + e.getBadToken());
            exit(8);
        } catch (IOException e) {
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(9);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Validation Error: " + e.getMessage() + " from " + e.getCause());
            exit(10);
        }
    }

    /**
     * Sends a knowp message to the given MessageOutput's Output Stream
     * @param msgOut the MessageOutput
     */
    private static void sendKnowp(MessageOutput msgOut){
        try{
            Knowp myKnowP = new Knowp();
            myKnowP.encode(msgOut);
        }catch(IOException e){
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(9);
        }
    }

    /**
     * Sends a previously validated Bout Message to the given MessageOutput's OutputStream
     * @param boutMsg the Bout message to send
     * @param msgOut the MessageOutput to send the Bout Message to
     */
    private static void sendBout(MessageOutput msgOut, Bout boutMsg){
        try{
            boutMsg.encode(msgOut);
        } catch (IOException e) {
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause());
            exit(9);
        }
    }

    /**
     * After completing protocol, try to successfully close the connection between the server by reading Acks
     * @param msgIn the MessageInput to read in Messages
     */
    private static void receiveAcks(MessageInput msgIn){
        boolean ackWait = true;
        boolean finalWait = true;
        while(finalWait){
            // After sending all message attempt to close connection by going into Final Wait
            // Ensure no error messages were returned
            Message msg = readMessage(msgIn);
            if (msg instanceof Ack){
                // if this is the first ack we received go into final wait
                if(ackWait){
                    ackWait = false;
                }
                // if we are already in final wait close the connection
                else{
                    finalWait = false;
                }
            }
            else{
                System.err.println("Unexpected message: " + msg);
            }

        }
    }

    /**
     * Client Executable
     * @param args parameters passed in
     */
    // Have callable main function
    public static void main(String args[]){
        commandLineValidation(args);
        // get the name of the server
        String serverName = args[0];
        String serverPort = args[1];
        // get the user ID
        String userID = args[2];
        // validate userID
        String hashPass = args[3];

        // validate username
        // by trying to create an ID message
        ID myIDMsg = validateUser(userID);

        // validate for a BOUT argument with the given category and filePath
        // define minimum scope for Bout Message
        Bout myBoutMsg = null;
        if(sendBoutMode){
            String category = args[5];
            String imagePath = args[6];
            myBoutMsg = validateBout(category, imagePath);
        }

        // Create and connect the socket
        Socket socket = connectSocket(serverPort,serverName);

        // setupMsgInOut()
        // Create an InputStream for receiving messages and give it to the MessageInput
        MessageInput msgInput = setupMsgInput(socket);

        // Create an OutputStream for writing messages to the server and give it to the MessageOutput
        MessageOutput msgOutput = setupMsgOutput(socket);

        // only send an ID after we have a valid FABRIC message

        // Keep trying to receive fabric until error
        receiveFabric(msgInput);
        // After we've received a fabric send an ID message
        sendID(msgOutput, myIDMsg);

        // Once The Client has sent an ID message, we want a CLNG message containing the challenge
        Challenge serverChallenge = receiveChallenge(msgInput);

        // Once the challenge has been received, try to verify and send a credentials
        sendCred(serverChallenge, msgOutput, hashPass);

        // Based on sendBoutMode flag being true or false, send a knowp message or a bout message
        if(sendBoutMode){
            sendBout(msgOutput, myBoutMsg);
        }
        else{
            sendKnowp(msgOutput);
        }

        // Once The bout/knowp message has been sent, attempt to close the connection
        receiveAcks(msgInput);

        // Close the connection once valid response from server acquired
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Unable to communicate: " + e.getMessage() + " from " + e.getCause() + " When attempting to close socket");
            exit(14);
        }
    }
}
