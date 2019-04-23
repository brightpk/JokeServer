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
 h. AdminWorker.class
 i. ClientWorker.class

5. Notes:
My program is not tracking the conversation between Client and Server.
However, it returns all jokes and proverbs randomly eventually.
If Admin swtiches to Joke mode, server sends a joke to client or if it changes to proverb, then client receives a proverb (one at a time)
I have set up the UUID but haven't finished tracking :(

----------------------------------------------------------*/

import java.io.*;   // Include Input and Output libraries
import java.net.*;  // Include networking libraries
import java.util.*; // In this case it's for random() method to send out a random joke and a random proverb
import java.lang.*;  // Include method to compare the objects

/* Server connects to JokeClient so they both can communicate
 * both client and server have the same portnumber
 * and that's how they speak to each other
 * JokeServer's job is giving a joke or a proverb to client one at a time randomly
 * In order to send a joke or a proverb, server needs to know what mode Admin swtiches to
 * Anyhow, the default mode is a joke mode in this program
 */
public class JokeServer {

  public static void main(String a[]) throws IOException {
    int maxQueue = 6;   // Number of queue that can be handled for connections (maximun is 6)
    int PORT = 4545;    // Port number for talk to client is 4545
    Socket sock;        // to be able to send the signal between server and user

    /* Copied from the instruction from joke-threat.html
     * Professor Clark says it's okay to do so for this part
     */
    Admin AL = new Admin();
    Thread t = new Thread(AL);
    t.start();  // begin the thread and wait for admin input

    ServerSocket servsock = new ServerSocket(PORT, maxQueue);  // listen to connection from client

    System.out.println("Siriporn Phakamad's Joker Server starting up, at port " + PORT + ".\n");

    /* wait for client to send the his input and username
     * start connection and keep on going till there's no more request from client
     */
    while (true) {
      sock = servsock.accept();         // wait to communicate with coming up client
      new ClientWorker(sock).start(); // employee for client begins to work which is sending a joke or a proveb
    }
  }
}

/* Server receives client's name and the task for a joke / a proverb from Jokeclient and
 * send a joke or a proverb that it's up to Admin if he/she's changed the mode
 */
class ClientWorker extends Thread {   // Class that server will be using for client
  Socket clientSock;                    // local socket that creates connection between client and server
  String[] jokes = new String[4];       // make an array called jokes that have 4 jokes
  String[] proverbs = new String[4];    // create an array to store all 4 proverbs

  String stateJokes;     // tracking state of Jokes if client has seen or not
  String stateProverbs;  // tracking state of Proverbs if client has seen or not

  /* Constructor */
  ClientWorker (Socket s) { clientSock = s; }

  /* Receive input/output from socket */
  public void run() {
    PrintStream toClient = null;        // to send result to client
    BufferedReader fromClient = null;   // Read whatever client has sent to server

    /* This part of the code prints out a joke or a proveb that client requests
     * It depends on Admin if he/she wants to swtich to another mode (JOKE mode and PROVERB mode)
     */
    try {
      fromClient = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
      toClient = new PrintStream(clientSock.getOutputStream());

      try {
        String clientName = null;   // to store client's username
        String clientInput = null;  // to store client's input
        String enter = "";          // empty means when user on Jokeclient only presses enter
        clientName = fromClient.readLine();     // read the username from jokeClient
        stateJokes = fromClient.readLine();     // read the state of joke sending by client
        stateProverbs = fromClient.readLine();  // read the state of proverb sending by client
        clientInput = fromClient.readLine();    // read what client has enter

        System.out.println("User: " + clientName);  // print clientName to server to see who connects to server

        /* "1111" means client has seen all four jokes or Proverbs
         * if client has seen all four which is "1111", then clean up by switching all to "0000"
         * so server can send any joke or proverb to start with
         */
        if (Admin.MODE == "JOKE" && stateJokes.equals("1111")) {  // check if it's in Joke mode and client has seen all four jokes
          stateJokes = "0000"; // then change all One to zero meaning that we reset the state
        } else if (Admin.MODE == "PROVERB" && stateProverbs.equals("1111")) { // check if it's in proverb mode and client has seen all four proverb
          stateProverbs = "0000"; // then change all One to zero meaning that we reset the state
        }

        /* This block exceutes 2 functions either get a random joke or a random proverb
         * It also will print out on server console which joke/proverb number we're sending to client
         * If the input is "quit", then the server will print out username and saying that he left the program
         */
        String sentJoke;    // a joke that we send to client
        String sentProv;    // a proverb that we send to client
        if (clientInput.compareTo(enter) == 0) {  // if client just pressed enter then get a joke or a proverb
          System.out.println("Current mode: " + Admin.MODE);  // print mode to let server know which mode Admin picks or if it's a defaut mode
          if (Admin.MODE == "JOKE") {             // if in JOKE mode
            sentJoke = randomJokes(clientName);   // get a random joke
            toClient.println(sentJoke);           // send a joke to client
            toClient.println(stateJokes);         // send a state of joke to client
            toClient.println(stateProverbs);      // also send a state of proverb to client
            int JokeNum = 0;
            if (sentJoke.startsWith("JA")) { JokeNum = 1; }
            else if (sentJoke.startsWith("JB")) { JokeNum = 2; }
            else if (sentJoke.startsWith("JC")) { JokeNum = 3; }
            else if (sentJoke.startsWith("JD")) { JokeNum = 4; }
            System.out.println("Sending Joke #" + JokeNum + "...\n");
          } else if (Admin.MODE == "PROVERB") {     // if in JOKE mode
            sentProv = randomProverbs(clientName);  // get a random proverb
            toClient.println(sentProv);             // send a proverb to client
            toClient.println(stateJokes);           // also send a state of joke to client
            toClient.println(stateProverbs);        // send a state of proverb to client
            int ProvNum = 0;
            if (sentProv.startsWith("PA")) { ProvNum = 1; }
            else if (sentProv.startsWith("PB")) { ProvNum = 2; }
            else if (sentProv.startsWith("PC")) { ProvNum = 3; }
            else if (sentProv.startsWith("PD")) { ProvNum = 4; }
            System.out.println("Sending Proverb #" + ProvNum + "...\n");
          }
        } else if (clientInput.compareTo("quit") == 0) {  // if input is "quit"
          System.out.println("Left the program!\n");      // print out the he is no longer in the program
        }

        /* Print on server side to let the server know that
         * server has sent all four jokes or all four proverbs
         */
        if (Admin.MODE == "JOKE" && stateJokes.equals("1111")) {
          System.out.println("JOKE CYCLE COMPLETED!\n");
        } else if (Admin.MODE == "PROVERB" && stateProverbs.equals("1111")) {
          System.out.println("PROVERB CYCLE COMPLETED!\n");
        }
      }

      /* catch whatever the exception that occur when start asking for a joke/proverb */
      catch (IOException err) {
        System.out.println("Server read error");
        err.printStackTrace();
      }

      clientSock.close();   // close connection, but server's still on
    }

    /* stop at the exception and print it out  */
    catch (IOException IOerr) {
      System.out.println(IOerr);
    }
  }

  /* Get a random joke from this method
   * This function gives out the joke that client asks for
   * It randomly passes a joke that client hasn't seen to to jokeclient
   */
  String randomJokes(String clientName) {
    String randJoke;  // to put the random joke
    int JokNum = 0;                // to store the randome number
    int len = jokes.length;       // to store what size of array in this case it's 4
    Random rand = new Random();   // to generate the random jokes in array
    char[] charStateJokes = stateJokes.toCharArray(); // tempArrchar for updating state

    /* The list of all four jokes in array, and each index represents differnt joke */
    jokes[0] = "JA " + clientName + ": " + "Q: What do you call a frozen dog? A: A pupsicle!";
    jokes[1] = "JB " + clientName + ": " + "Q: How does a computer get drunk? A: It takes screenshots!";
    jokes[2] = "JC " + clientName + ": " + "Q: What do scientists use to freshen their breath? A: Experi-mints!";
    jokes[3] = "JD " + clientName + ": " + "Q: Why did the computer show up at work late? A: It had a hard drive!";

    /* get a random joke from all 4 jokes in this array and print out only one */
    for (int i = 0; i < len; i++) {
      JokNum = rand.nextInt(len);
    }

    /* check if state of joke (by char) is equal to 1
     * which means this joke already be seen so call the function again to get a new joke
     */
    if (charStateJokes[JokNum] == '1') {
      return randomJokes(clientName);
    }

    randJoke = jokes[JokNum];   // a randome joke

    /* Basically update the state of joke by changing from 0 to 1 in the tmp state char array
     * then update back on the state of joke and put it as string
     */
    if (randJoke.equals(jokes[0]) && charStateJokes[0] == '0') {
      charStateJokes[0] = '1';
      stateJokes = String.valueOf(charStateJokes);
    } else if (randJoke.equals(jokes[1]) && charStateJokes[1] == '0') {
      charStateJokes[1] = '1';
      stateJokes = String.valueOf(charStateJokes);
    } else if (randJoke.equals(jokes[2]) && charStateJokes[2] == '0') {
      charStateJokes[2] = '1';
      stateJokes = String.valueOf(charStateJokes);
    } else if (randJoke.equals(jokes[3]) && charStateJokes[3] == '0') {
      charStateJokes[3] = '1';
      stateJokes = String.valueOf(charStateJokes);
    }

    return randJoke;
  }

    /* Get random a proverb from this method
     * This function is doing the same thing as Jokes function
     * but instead, this is for proverbs
     */
    String randomProverbs(String clientName) {
      String randProv;              // to put the random proverb in here
      int ProvNum = 0;              // to store the randome number
      int len = proverbs.length;    // to store the length of this array which is 4
      Random rand = new Random();   // give the random proverb
      char[] charStateProverbs = stateProverbs.toCharArray(); // tempArrchar for updating state

      /* The list of all four proverbs in array and show each index has differnt proverb */
      proverbs[0] = "PA " + clientName + ": " + "Another day, another dollar";
      proverbs[1] = "PB " + clientName + ": " + "There's no place like home.";
      proverbs[2] = "PC " + clientName + ": " + "Be the first in the field and the last to the couch.";
      proverbs[3] = "PD " + clientName + ": " + "It’s not whether you get knocked down, It’s whether you get up.";

      /* get a random proverb for .random() */
      for (int i = 0; i < len; i++) {
        ProvNum = rand.nextInt(len);
      }

      /* check if state of proverb (by char) is equal to 1
       * which means this proverb already be seen so call the function again to get a new proverb
       */
      if (charStateProverbs[ProvNum] == '1') {
        return randomProverbs(clientName);
      }

      randProv = proverbs[ProvNum]; // a random proverb

      /* Basically update the state of proverb by changing from 0 to 1 in the tmp state char array
       * then update back on the state of proverb and put it as string
       */
      if (randProv.equals(proverbs[0]) && charStateProverbs[0] == '0') {
        charStateProverbs[0] = '1';
        stateProverbs = String.valueOf(charStateProverbs);
      } else if (randProv.equals(proverbs[1]) && charStateProverbs[1] == '0') {
        charStateProverbs[1] = '1';
        stateProverbs = String.valueOf(charStateProverbs);
      } else if (randProv.equals(proverbs[2]) && charStateProverbs[2] == '0') {
        charStateProverbs[2] = '1';
        stateProverbs = String.valueOf(charStateProverbs);
      } else if (randProv.equals(proverbs[3]) && charStateProverbs[3] == '0') {
        charStateProverbs[3] = '1';
        stateProverbs = String.valueOf(charStateProverbs);
      }

      return randProv; // a random proverb that client will get if it's in proverb mode
    }

  /* turn input in buffer to string */
  static String toText(byte ip[]) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < ip.length; i++) {
      if (i > 0) result.append(".");
      result.append(0xff & ip[i]);
    }
    return result.toString();
  }
}


/* Copied from the instruction from joke-threat.html
 * Professor Clark says it's okay to do so for this part
 * It implements the run method
 */
class Admin implements Runnable {
  public static boolean AdminMode = true;
  public static String MODE = "JOKE";

  /* This is for communicating between Admin and server
   * They're connected when Admin has entered input and press enter to send the input to server
   */
  public void run() {
      System.out.println("...Client Admin thread is on...");

      int maxQueue = 6; // Number of queue that can be handled for connections (maximun is 6)
      int PORT = 5050;  // port number for communication between clientAdmin and server is 5050 on the a primary server
      Socket sock;

      try {
        ServerSocket servsock = new ServerSocket(PORT, maxQueue); // get to connection from clientAdmin

        /* wait for request from clientAdmin, start connection and keep on going till there's no more request */
        while (AdminMode) {
          sock = servsock.accept();       // wait for coming up clientAdmin connection
          new AdminWorker(sock).start(); // AdminWorker starts the request
        }
      }
      catch (IOException IOerr) {System.out.println(IOerr);}
    }
  }

  /* This class is for Admin. It will switch the mode (Joke/Proverb)
   * Everytime Admin presses enter, mode is changed and all connection's mode will change to
   * All clients will be on the same mode. Switching mode depends on Admin because Admin is a mode controller
   */
  class AdminWorker extends Thread {
    Socket AdminSock;   // to connect between Admin and server with port number of 5050

    /* Constructor */
    AdminWorker (Socket s) { AdminSock = s; }

    /* Under AdminWorker, it runs the program for switching mode controlling by Admin
     * It prints out what mode users are toggling to.
     * This means ALL connections mode are changed.
     */
    public void run() {
      PrintStream toAdmin = null;     // Send output stream to Admin
      BufferedReader fromAdmin = null;   // Read input from Admin

      /* Build To/from Admin streams for connection
       * In this run program, it basically switch the mode when the key from Admin has entered
       */
      try {
        fromAdmin = new BufferedReader(new InputStreamReader(AdminSock.getInputStream()));
        toAdmin = new PrintStream(AdminSock.getOutputStream());

        try {
          String AdminInput;
          AdminInput = fromAdmin.readLine();              // receive key from Admin
          System.out.println("Entering mode...");     // print what mode is entering to on server side
          printMode(switchMode(AdminInput), toAdmin);  // print a joke or a proverb mode to admin know that it has switched to differnet mode

        }

        /* print an exception that be caught during running the program */
        catch (IOException x) {
          System.out.println("Server read error");
          x.printStackTrace();
        }

        /* shut down the connection between admin and server
         * however, server's still
         */
        AdminSock.close();
      }

      /* Let the server know that it catches some IO execption */
      catch (IOException ioe) {
        System.out.println(ioe);
      }
    }

    /* Print a joke and a proverb one at a time to clientAdmin
     */
    static void printMode(String AdminInput, PrintStream toAdmin) {
      try {
        if (AdminInput == "Joke") {
          toAdmin.println("--> JOKE mode <--\n");
        } else {
          toAdmin.println("--> PROVERB mode <--\n");
        }
      }

      /* Handle the exception that has found when start running the program */
      catch(Exception ex) {
        toAdmin.println("Failed in attempt to look up for mode ");
      }

    }

    /* Switch a joke mode or a proverb mode everytime connect to ClientAdmin and everytime ClientAdmin wants to swtich
     * In this case is everytime ClietnAdmin press enter which require to toggle between JOKE and PROVERB mode
     * Default mode is a joke mode
     * It takes one parameter which is the key that Admin has entered
     */
    static String switchMode(String AdminInput) {
      String mode = null; // to store the outcome (mode)

      try {
      while (true) {   // while the function has been called
        if (Admin.MODE == "JOKE") {   // if mode from Admin is JOKE mode
          Admin.MODE = "PROVERB";     // then switch the mode to proverb
          mode = "Proverb";           // store the result in mode variable
          System.out.println("Changing to PROVERB mode" + "\n");  // let server know that the mode is changed to proverb
          break;

        } else if (Admin.MODE == "PROVERB") { // do opposite which means if the mode is proverb
            Admin.MODE = "JOKE";              // then change to Joke mode
            mode = "Joke";                    // put the result in variable mode to return it later
            System.out.println("Changing to JOKE mode" + "\n");  // inform server that Admin changed mode by print it on server side
            break;
        }
      }
    }

    /* Deal with the exception */
    catch(Exception ex) {
      System.out.println("Failed in attempt to switch mode ");
    }

    return mode;

    }
  }
