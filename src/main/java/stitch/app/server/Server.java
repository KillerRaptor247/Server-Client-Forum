/*
 * Author:     Dante Hart
 * Assignment: Program 6
 * Class:      CSI 4321
 *
 */
package stitch.app.server;

import fabric.serialization.Message;
import stitch.serialization.CodeException;
import stitch.serialization.ErrorCode;
import stitch.serialization.Query;
import stitch.serialization.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.*;

import static java.lang.System.exit;

/**
 * Class for managing the stitch server protocol,
 */
public class Server extends Thread{

    // Max UDP payload in bytes
    private static final int MAX_UDP_PAYLOAD = 65500;

    // stitch logger
    private static final Logger stitchLog = Logger.getLogger("");

    // Create Logger if cant read any information from a logger file
    private static FileHandler stitchLogHandler;

    // shared port with fabric
    private int sharedPort;

    // Datagram Socket for Server
    private DatagramSocket stitchSocket;

    // Keep track of all legitimate BOUT and KNOWP messages
    private static List<String> legitMessageList;

    // Server Constructor

    /**
     * Constructor for a Stitch Server
     * @param fabricPort the port of the fabric server
     * @throws SocketException if the socket cannot be constructed
     */
    public Server(int fabricPort) throws SocketException {
        try {
            // Disable Default Console Handler
            Handler[] handlers = stitchLog.getHandlers();
            for(Handler handler : handlers){
                if(handler instanceof  ConsoleHandler){
                    stitchLog.removeHandler(handler);
                }
            }

            // attach file handler
            stitchLogHandler = new FileHandler("stitch.log", true);
            stitchLogHandler.setFormatter(new SimpleFormatter());
            stitchLog.addHandler(stitchLogHandler);
        } catch (IOException e) {
            stitchLog.log(Level.SEVERE, "Unable to Start: " + e.getMessage() + " from " + e.getCause());
            stitchLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
            stitchLog.log(Level.FINE, "Exit Code: 66");
            exit(66);
        }
        // set logger level
        stitchLog.setLevel(Level.FINE);
        stitchLog.log(Level.FINE, "Attempting to Startup Server");
        // Create the Socket Instance on the shared port
        sharedPort = fabricPort;
        stitchLog.log(Level.INFO, "Port Received from fabric server: " + sharedPort);
        stitchSocket = new DatagramSocket(sharedPort);
        stitchLog.log(Level.INFO, "UDP Datagram Socket Created!");

        // create array list
        legitMessageList = new ArrayList<>(0);
    }

    // add legitimate message to list

    /**
     * Adds a message to the stitch servere legit message list
     * @param m the Message to add
     */
    public static void addLegitMessage(Message m){
        stitchLog.log(Level.INFO, "Adding " + m + " to message list!");
        legitMessageList.add(m.toString());
    }

    /**
     */
    @Override
    public void run() {
        while(true){
            byte[] readBuffer = new byte[Response.MAX_RESPONSE_HEADER_BYTE_LEN];
            // Create stitch packet
            DatagramPacket recStitchPacket = new DatagramPacket(readBuffer, readBuffer.length);

            // Try to receive stitch packet
            try {
                stitchSocket.receive(recStitchPacket);
                stitchLog.log(Level.INFO, "Stitch: Received Packet.");

                byte[] recieveBuffer = Arrays.copyOfRange(recStitchPacket.getData(), 0, recStitchPacket.getLength());

                stitchLog.log(Level.INFO, Arrays.toString(recieveBuffer));

                // Response based on result of recieving packet
                Response stitchResponse = null;
                // Try to parse Query Packet
                try{
                    Query stitchQuery = new Query(recieveBuffer);

                    // if subset of list is requested
                    if(stitchQuery.getRequestedPosts() < legitMessageList.size()){
                       stitchResponse = new Response(stitchQuery.getQueryID(), ErrorCode.NOERROR, legitMessageList.subList(0, stitchQuery.getRequestedPosts()));
                    }
                    // if the requested is greater than the size just pass the entire list
                    else{
                        stitchResponse = new Response(stitchQuery.getQueryID(), ErrorCode.NOERROR, legitMessageList);
                    }
                    // remove the newest posts while the response message is too large
                    while(stitchResponse.encode().length > MAX_UDP_PAYLOAD){
                        legitMessageList.remove(legitMessageList.size() - 1);
                        stitchResponse.setPosts(legitMessageList);
                    }

                } catch (CodeException e) {
                    List<String> noPosts = new ArrayList<>(0);
                    switch(e.getErrorCode()){
                        case BADVERSION -> stitchResponse = new Response(0, ErrorCode.BADVERSION, noPosts);
                        case UNEXPECTEDPACKETTYPE -> stitchResponse = new Response(0, ErrorCode.UNEXPECTEDPACKETTYPE, noPosts);
                        case NETWORKERROR -> stitchResponse = new Response(0, ErrorCode.NETWORKERROR, noPosts);
                        case UNEXPECTEDERRORCODE -> stitchResponse = new Response(0, ErrorCode.UNEXPECTEDERRORCODE, noPosts);
                        case PACKETTOOSHORT -> stitchResponse = new Response(0, ErrorCode.PACKETTOOSHORT, noPosts);
                        case PACKETTOOLONG -> stitchResponse = new Response(0, ErrorCode.PACKETTOOLONG, noPosts);
                        default -> stitchResponse = new Response(0, ErrorCode.VALIDATIONERROR, noPosts);
                    }
                    stitchLog.log(Level.WARNING,"Stitch: Unexpected Error: " + e.getMessage() + " from " + e.getCause());
                    stitchLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
                }
                // send the response to the client
                stitchLog.log(Level.INFO, "Stitch: Sending Response to " + recStitchPacket.getAddress().getHostName() + ": " + stitchResponse);
                InetAddress clientAddress = InetAddress.getByName(recStitchPacket.getAddress().getHostName());
                DatagramPacket sendStitchPacket = new DatagramPacket(stitchResponse.encode(), stitchResponse.encode().length, clientAddress, recStitchPacket.getPort());
                stitchSocket.send(sendStitchPacket);

            } catch (IOException e) {
                stitchLog.log(Level.WARNING, "Stitch: " + e.getMessage() + " from " + e.getCause());
                stitchLog.log(Level.FINE, Arrays.toString(e.getStackTrace()));
            }
        }
    }


}
