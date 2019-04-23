/*--------------------------------------------------------

1. Name / Date:
Siriporn Phakamad / 23 September 2018

2. Java version used, if not the official version for the class:
build 1.8.0_181

3. Precise command-line compilation examples / instructions:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 192.168.1.64

5. List of files needed for running the program.
 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java
 e. JokeServer.class
 f. JokeClient.class
 g. JokeClientAdmin.class
 h. AdminEmployee.class
 i. ClientEmployee.class

5. Notes:
My program is not tracking the conversation between Client and Server.
However, it returns all jokes and proverbs randomly eventually.
If Admin swtiches to Joke mode, server sends a joke to client or if it changes to proverb, then client receives a proverb (one at a time)
I have set up the UUID but haven't finished tracking :(

----------------------------------------------------------*/

import java.io.*;   // Include Input and Output libraries
import java.net.*;  // Include networking libraries
import java.lang.*; // Include this for compareTo method
import java.util.UUID;

/* Client sends input to server and receives text from server
 * Client sends his/her name to server so the server knows who he or she is
 * Client then ask for a joke or a proverb from server by pressing enter button to connect to server
 */
public class JokeClient {
  public static int PORT;         // port number for client to talk to server which is 4545 for the primary server
  public static String stateJokes;
  public static String stateProverbs;

  public static void main(String args[]) {
    String serverName;    // to store IP address
    PORT = 4545;          // use this port 5050 for AdminClient

    if (args.length < 1) { serverName = "localhost"; }  // if client doesn't provide specific IP address, use the localhost
    else { serverName = args[0]; }  // if not, use that IP address to be connected and connect to that IP address

    System.out.println("Siriporn Phakamad's Joke Client, 1.8.\n");                // print Joke Client and java 1.8
    System.out.println("Server: " + serverName + ", Port: " + PORT);        // print servername and port number
    System.out.println("You are in the Joke Client...");                          // inform client that they're in JokeClient
    BufferedReader inPut = new BufferedReader(new InputStreamReader(System.in));  // to store client's input and send it later to server

    try {
      String clientName;  // name of user will be in here
      String clientInput; // whatever client puts will be store here
      String enter = "";  // use for compare if user presses only enter
      String clientID;    // to store the unique number for each client

      /* Ask client to put their name. 1 character or more is allowed
       * otherwise it will keep asking client to enter the name
       */
      do {
        System.out.print("Enter your name: ");  // ask the name of the user
        System.out.flush();                     // make sure that data is saved
        clientName = inPut.readLine();          // store name of user to name variable

        /* if there's at least 1 char */
        if (clientName.length() != 0) {
          break;
        }

      } while (true);

      /* State from server for jokes and proverbs */
      stateJokes = "0000";      // state of jokes
      stateProverbs = "0000";   // state of proverbs

      /* Ask user to press ENTER with no other command to get a joke or a proverb
       * It'll keep asking to press ENTER if user enter something else
       * rather than ENTER button or "quit" to exit this program
       */
      do {
        System.out.print("Press -ENTER- to hear a joke or a proverb, (quit) to exit: ");
        System.out.flush();              // save client's input before close the socket
        clientInput = inPut.readLine();  // store input what client has entered

        /* If user press ENTER button, connection starts
         * and get a joke or a proverb from server
         */
         if (clientInput.compareTo(enter) == 0) {         // see if user pressed only ENTER button
           getJokeProverb(clientName, serverName, clientInput); // if yes, then it will connect to the server and receive a joke or a proverb
         }
      }

      /* If user enter "quit" instead of press ENTER, then get out the program */
      while (clientInput.compareTo("quit") != 0);  // if key equals to "quit" then quit
      System.out.println("Program is terminated. See you later!");
      try {
        getJokeProverb(clientName, serverName, clientInput); // still call the function because I want server to print out who left the program
      } catch (NullPointerException n) {}

    }

    /* Deal with the exception and it will catch any exception that appear when user enters the key */
    catch (IOException error) {
      error.printStackTrace();
    }
  }

  static String toText(byte ip[]) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < ip.length; i++) {
      if (i > 0) result.append(".");
      result.append(0xff & ip[i]);
    }
    return result.toString();
  }

  /* Get a joke or a proverb from server by sending clientname and the key that use has entered
   * After client connects to server, it will send back a joke or a proverb which depends on MODE
   */
  static void getJokeProverb(String clientName, String serverName, String clientInput) {
    Socket sock;
    BufferedReader fromServer;    // put data that user receive from server in the buffer and read it
    PrintStream toServer;         // to store name and request for a joke/proverb
    String textFromServer;        // read and store the text from server

    try {
      sock = new Socket(serverName, PORT);   // start communicating between client and server with port number 4545

      /* Create Input/Output streams for the socket
       * Send out the signal and communicate with server
       * Send out the input and name to server so server can give back a joke or a proverb
       */
      fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      toServer = new PrintStream(sock.getOutputStream());

      /* Send clientName and input to server */
      toServer.println(clientName);     // send username to server
      toServer.println(stateJokes);
      toServer.println(stateProverbs);
      toServer.println(clientInput);    // send request for a joke/proverb
      toServer.flush();                 // save those output

      /* Get a joke ot proverb from server */
      textFromServer = fromServer.readLine();  // read jokes and proverbs from server
      if (textFromServer != null) {            // if there's some text
        System.out.println(textFromServer);    // print the text on client side
        System.out.println();
      }

      stateJokes = fromServer.readLine();      // read the state of jokes
      /* print joke cycyle completed when client's seen all four jokes */
      if (stateJokes.equals("1111") && textFromServer.startsWith("J")) {
        System.out.println("----------------------------- JOKE CYCLE COMPLETED ----------------------------- ");
        System.out.println();
      }

      stateProverbs = fromServer.readLine();   // read the satte of proverbs
      /* print proverb cycyle completed when client's seen all four proverb */
      if (stateProverbs.equals("1111") && textFromServer.startsWith("P")) {
        System.out.println("----------------------------- PROVERB CYCLE COMPLETED ----------------------------- ");
        System.out.println();
      }

      // System.out.println("StateJokes: " + stateJokes);
      // System.out.println("StateProverbs: " + stateProverbs);

      sock.close();  // close connection
    }
    catch(IOException IOerror) {
      System.out.println("Socket error.");
      IOerror.printStackTrace();
    }
  }
}
