/*
 * Author:     Dante Hart
 * Assignment: Program 5
 * Class:      CSI 4321
 *
 */
package stitch.app.client;

import stitch.serialization.CodeException;
import stitch.serialization.Query;
import stitch.serialization.Response;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

import static java.lang.System.exit;

/**
 * Class for handling the stitch protocol
 */
public class Client {

    // timeout for stitch messages in milliseconds
    private static final int MESSAGE_TIMEOUT = 4000;

    private static final int MAX_POSTS = 65535;

    /**
     * Executable Protocol for Stitch client
     *
     * @param args arguments provided to the executable
     */
    public static void main(String[] args) {
        // First verify command line parameters
        if (args.length != 3) {
            System.err.println("Invalid number of arguments! Expected: Server IP/Name, Server Port, Requested Posts");
            exit(1);
        }
        String serverName = args[0];

        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println("Could not parse server port: " + e.getMessage());
            exit(2);
        }

        int requestMaxPost = 0;
        try {
            requestMaxPost = Integer.parseInt(args[2]);
        } catch (Exception e) {
            System.err.println("Could not parse requested posts: " + e.getMessage());
            exit(3);
        }

        if(requestMaxPost > MAX_POSTS){
            System.err.println("Requested amount of posts is too large!");
            exit(15);
        }

        // Get Server Address
        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName(serverName);
        } catch (UnknownHostException e) {
            System.err.println("Could not get server InetAddress: " + e.getMessage());
            exit(4);
        }

        // try to create UDP datagram socket
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(MESSAGE_TIMEOUT);
        } catch (SocketException e) {
            System.err.println("Could not create Datagram Socket:" + e.getMessage());
            exit(5);
        }
        Random rand = new Random();
        // Randomly generate the QueryID within the bounds of a query message
        long queryID = rand.nextLong(Query.MAX_QID);

        // Create the Query Message
        Query sendQuery = new Query(queryID, requestMaxPost);

        // Create a Datagram Packet and encode the query
        DatagramPacket packagedQuery = null;
        try {
            packagedQuery = new DatagramPacket(sendQuery.encode(), sendQuery.encode().length, serverAddress, serverPort);
        } catch (Exception e) {
            System.err.println("Could not create Datagram Packet:" + e.getMessage());
            socket.close();
            exit(6);
        }


        boolean correctResponse = false;

        int retransmit = 0;

        boolean received = false;

        // while a correctResponse hasn't been recieved
        DatagramPacket packagedResponse = null;
        while (!correctResponse) {

            while(retransmit < 2 && !received){
                // Try to Send the Query
                try {
                    socket.send(packagedQuery);
                } catch (IOException e) {
                    System.err.println("Could not send Query packet: " + e.getMessage());
                    socket.close();
                    exit(7);
                }

                // create a byte buffer for reading the largest possible expected value
                byte[] readBuffer = new byte[Response.MAX_RESPONSE_HEADER_BYTE_LEN];
                try {
                    packagedResponse = new DatagramPacket(readBuffer, readBuffer.length);
                } catch (Exception e) {
                    System.err.println("Could not create receiving response packet: " + e.getMessage());
                    socket.close();
                    exit(8);
                }
                // try to receive a response
                try {
                    socket.receive(packagedResponse);
                    received = true;
                    retransmit = 0;
                } catch (SocketTimeoutException e) {
                    if (retransmit == 1) {
                        System.err.println("Packet Retransmitted and still timed out: " + e.getMessage());
                        exit(12);
                    } else {
                        retransmit++;
                    }
                } catch (Exception e) {
                    System.err.println("Could not receive packet from socket: " + e.getMessage());
                    socket.close();
                    exit(9);
                }
            }
            // update flag for next packet
            received = false;

            byte[] recieveBuffer = Arrays.copyOfRange(packagedResponse.getData(), 0,packagedResponse.getLength());

            // Attempt to decode the response
            Response receivedResponse = null;
            try {
                receivedResponse = new Response(recieveBuffer);
            } catch (CodeException e) {
                // Print out error based on Code Exception
                System.err.println(e.getErrorCode().getErrorMessage());
                socket.close();
                exit(10);
            }
            // If response is valid
            // Check for non-zero error
            if (receivedResponse.getErrorCode().getErrorCodeValue() != 0) {
                System.err.println("Non-Zero Error Code detected in response: " + receivedResponse.getErrorCode().getErrorMessage());
                socket.close();
                exit(11);
            }

            // verify if response has matching queryID
            if (receivedResponse.getQueryID() == sendQuery.getQueryID()) {
                // print the response to the terminal
                System.out.println(receivedResponse);
                // set the boolean flag to true to leave read loop
                correctResponse = true;
            }
            // otherwise ignore packet

        }

        socket.close();
    }

}

