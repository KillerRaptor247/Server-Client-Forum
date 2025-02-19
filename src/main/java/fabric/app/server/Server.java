/*
 * Author:     Dante Hart
 * Assignment: Program 3
 * Class:      CSI 4321
 *
 */
package fabric.app.server;
import stitch.app.server.*;

import fabric.serialization.*;
import lybrary.Y;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static java.lang.System.exit;

/**
 * Class for managing the server protocol,
 */
public class Server {

    // constant for validation of command line parameters
    private static final int MAX_ARGS = 3;

    // Create Logger if cant read any information from a logger file
    private static FileHandler logHandler;
    private static final Logger serverLog = Logger.getLogger("");

    // Define pattern and matcher for validating password file entries
    private static Pattern passPattern;
    private static Matcher passMatcher;

    // Have a Set of known userIDs that map to their passwords
    private static final HashMap<String, String> knownUsers = new HashMap<>();

    // Have a server static instance to a Set so keep track of knowp message since last server start
    private static final HashMap<String, Integer> userKnowpCount = new HashMap<>();

    // have static context of single secure random generator to ensure numbers are random for each ID
    private static final SecureRandom random = new SecureRandom();

    // have static context of webpage so that all threads may post to it
    private static Y htmlPage;


    /**
     * Validates a single line entry from a password file
     * @param passLine The string to validate
     * @return true if valid password entry and false otherwise
     */
    private static boolean validatePassEntry(String passLine){
        passPattern = Pattern.compile("^[\\dA-Za-z]+:[\\da-zA-Z]*\\r\\n\\z");
        passMatcher = passPattern.matcher(passLine);
        return passMatcher.matches();
    }

    /**
     * Parses the given parameter into a user/pass pair and adds it to the servers list of known users
     * @param passLine the password entry line read in from the password file
     */
    private static void addEntry(String passLine){
        // split password entry into user and password segments
        String[] userEntry = passLine.split(":");
        // use userID as the key to minimize password exposure
        serverLog.log(Level.INFO, "Adding " + userEntry[0] + " to known users");
        // add to known users and initialize knowp count
        knownUsers.put(userEntry[0], userEntry[1].substring(0, userEntry[1].indexOf(Message.CR_LF)));
        userKnowpCount.put(userEntry[0], 0);
    }

    /**
     * Validates the parameters passed into the server before starting up and listening
     * @param args the parameters passed in to the server
     */
    private static void validateArguments(String[] args){
        try {
            // Disable Default Console Handler
            Handler[] handlers = serverLog.getHandlers();
            for(Handler handler : handlers){
                serverLog.removeHandler(handler);
            }
            logHandler = new FileHandler("server.log", true);
            logHandler.setFormatter(new SimpleFormatter());
            serverLog.addHandler(logHandler);
        } catch (IOException e) {
            serverLog.log(Level.SEVERE, "Unable to Start: " + e.getMessage() + " from " + e.getCause());
            serverLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
            serverLog.log(Level.FINE, "Exit Code: 10");
            exit(10);
        }
        serverLog.setLevel(Level.FINE);
        // before attempting to start up server, validate command line
        serverLog.log(Level.FINE, "Attempting to Startup Server");
        serverLog.log(Level.INFO, "Parameters Given: " + Arrays.toString(args));

        if(args.length != MAX_ARGS){
            serverLog.log(Level.SEVERE, "Unable to Start: Incorrect number of parameters given!");
            serverLog.log(Level.INFO, "Format: <Server Port> <Number of Threads> <Password File>");
            serverLog.log(Level.FINE, "Exit Code: 1");
            exit(1);
        }

        // Try to parse arguments for server port and number of threads in the thread pool
        serverLog.log(Level.FINE, "Attempting to Parse Parameters.");
        try{
            int servPort = Integer.parseInt(args[0]);
            int numThread = Integer.parseInt(args[1]);
            if(numThread < 1 || servPort < 1){
                serverLog.log(Level.SEVERE, "Unable to start: Invalid Server Port or Number of Threads Given!");
                serverLog.log(Level.INFO, "Server Port: " + servPort);
                serverLog.log(Level.INFO, "Number of Threads: " + numThread);
                serverLog.log(Level.FINE, "Exit Code: 2");
                exit(2);
            }
        }catch(NumberFormatException e){
            serverLog.log(Level.SEVERE, "Unable to start: " + e.getMessage() + " from" + e.getCause());
            serverLog.log(Level.FINE, "Resulting Stack Trace: " + Arrays.toString(e.getStackTrace()));
            serverLog.log(Level.FINE, "Exit Code: 2");
            exit(2);
        }

    }

    private static void readPassFile(String fileName){

        FileInputStream passInput = null;
        File passFile = null;
        try {
            passFile = new File(fileName);
            passInput = new FileInputStream(passFile);
        } catch (Exception e) {
            serverLog.log(Level.SEVERE, "Unable to start: " + e.getMessage() + " from " + e.getCause());
            serverLog.log(Level.FINE, "Resulting Stack Trace: " + Arrays.toString(e.getStackTrace()));
            serverLog.log(Level.FINE, "Exit Code: 3");
            exit(3);
        }
        // validate and add entries to map
        serverLog.log(Level.INFO, "Password File: " + passFile.getName());
        serverLog.log(Level.FINE, "Attempting to validate Password File Entries");
        try {
            // Try to read in one password line at a time
            byte currByte;
            ByteArrayOutputStream currBuffer = new ByteArrayOutputStream();

            // read in one byte at a time we can't assume anything about the input
            // until we've reach EOS or the end of a password entry
            serverLog.log(Level.FINE,"Reading in Entries from File...");
            while ((currByte = (byte) passInput.read()) != -1) {
                // store the byte into the ByteArrayOutputStream
                currBuffer.write(currByte);

                // Once we received an endline verify the correct format of the password entry
                if (currByte == '\n') {
                    String passLine = currBuffer.toString(Message.CHAR_ENC);
                    // If the current password entry is invalid
                    if(!validatePassEntry(passLine)){
                        // log error and terminate
                        serverLog.log(Level.SEVERE, "Unable to start: Invalid entry format detected inside password file");
                        serverLog.log(Level.FINE, "Exit Code: 4");
                        exit(4);
                    }
                    else{
                        // If the current password is valid add it to the table
                        serverLog.log(Level.FINE, passLine + " is valid. Adding to mapping table");
                        // reset buffer and add entry to table/map
                        addEntry(passLine);
                        currBuffer.reset();
                    }
                }
            }
            // after of EOS is reached closed the file input stream
            passInput.close();
        } catch (IOException e) {
            serverLog.log(Level.SEVERE, "Unable to start: " + e.getMessage() + " from" + e.getCause());
            serverLog.log(Level.FINE, "Resulting Stack Trace: " + Arrays.toString(e.getStackTrace()));
            serverLog.log(Level.FINE, "Exit Code: 5");
            exit(5);
        }
    }

    private static ServerSocket createServerSocket(int servPort){
        ServerSocket servSocket = null;
        try{
            servSocket = new ServerSocket(servPort);
        } catch (IOException e) {
            serverLog.log(Level.SEVERE, "Unable to start: " + e.getMessage() + " from" + e.getCause());
            serverLog.log(Level.FINE, "Resulting Stack Trace: " + Arrays.toString(e.getStackTrace()));
            serverLog.log(Level.FINE, "Exit Code: 6");
            exit(6);
        }

        return servSocket;

    }

    /**
     * Main Executable of a Server for the Fabric Protocol
     * @param args The parameters passed in for the server to create
     */
    public static void main(String[] args){
        // validate command line parameters
        validateArguments(args);

        int serverPort = Integer.parseInt(args[0]);
        int numThreads = Integer.parseInt((args[1]));
        String passFilePath = args[2];

        // try to create Html File
        try {
            htmlPage = new Y(System.getProperty("user.dir") + "/y.html");
        } catch (Exception e) {
            serverLog.log(Level.SEVERE, "Unable to Start: " + e.getMessage() + " from " + e.getCause());
            serverLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
            exit(-1);
        }

        // try to open and read password file
        serverLog.log(Level.FINE, "Attempting to Open Password File");
        readPassFile(passFilePath);

        // try to open and read password file
        serverLog.log(Level.FINE, "Attempting to Open ServerSocket for listening on port " + serverPort);
        // Once password file is read in, attempt to open server socket for listening on server port
        ServerSocket servSocket = createServerSocket(serverPort);

        // try to open and read password file
        serverLog.log(Level.INFO, "Fabric Server Created Successfully. Creating Thread pool of " + numThreads + " Threads.");
        serverLog.log(Level.INFO, "Attempting to create Stitch Server");

        try {
            stitch.app.server.Server stitchServ = new stitch.app.server.Server(serverPort);
            serverLog.log(Level.INFO, "Stitch Server Created Successfully. Running Now");
            stitchServ.start();
        } catch (SocketException e) {
            serverLog.log(Level.SEVERE, "Stitch Server could not be start up from " + e.getMessage() + ". Terminating");
        }

        // Create number of threads based on parameters given
        for(int i = 0; i < numThreads; i++){
            ServerThread servThread = new ServerThread(servSocket, serverLog, knownUsers, userKnowpCount, random, htmlPage);
            servThread.start();
            serverLog.log(Level.INFO, "Created and started ServerThread: " + servThread.getName());
        }




    }

}
