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
import java.lang.*; // Include for method to compareTo method

/* ClientAdmin sends input to server and receives text from server
 * ClientAdmin informs server that it wants to switch a mode
 */
public class JokeClientAdmin {
  public static int PORT;   // port number for communication with server which is 5050 for a main server

  public static void main(String args[]) {
    String serverName;    // to store IP address (which computer/device that server will be connected to )
    PORT = 5050;          // use this port 5050 for AdminClient

    if (args.length < 1) { serverName = "localhost"; } // if ClientAdmin doesn't provide specific IP address, use the localhost (127.0.0.1)
    else { serverName = args[0]; }  // if not, use that IP address to be connected

    System.out.println("Siriporn Phakamad's Admin Client, 1.8.\n");                 // print Joke Client Admin and java 1.8
    System.out.println("Server: " + serverName + ", Port: " + PORT);         // print servername and port number
    System.out.println("You are in the Joke Admin Client...");
    BufferedReader inPut = new BufferedReader(new InputStreamReader(System.in));   // buffer for input from adminclient

    try {
      String AdminInput;        // key that admin puts will be in here
      String switchMode = "";   // to compare the enter button

      /* Ask user to press ENTER to change between joke mode and proverb mode
       * Joke mode is the default mode when adminclient connects to server
       * If user enters "quit", the program terminates
       */
      do {
        System.out.print("Press -ENTER- to switch between Joke mode and Proverb mode, (quit) to exit: "); // ask uses if they want to switch a mode
        System.out.flush();             // save input from adminClient
        AdminInput = inPut.readLine();  // receive hostname from client that user enters

        /* If user press ENTER button, connection AdminClient to server
         * and let server know that Adminclient wants to change a mode
         */
         if (AdminInput.compareTo(switchMode) == 0) {    // compare input of AdminClient if it's "enter" button
           getMode(AdminInput, serverName);             // if yes, connection start between server and AdminClient for a mode
         }
      }

      /* If user input equal to "quit", exit the program  */
      while (AdminInput.compareTo("quit") != 0);  // if user typed "quit", disconnect between server and AdminClient
      System.out.println("Program is Terminated. See you later!");          // print bye bye to let AdminClient know they quit
    }

    /* Catch with the excapetion that appear when run the program */
    catch (IOException exce) {
      exce.printStackTrace();
    }
  }

  /* It will turn turn from hexdeximal to string */
  static String toText(byte ip[]) {
    StringBuffer result = new StringBuffer();
    for (int i = 0; i < ip.length; i++) {
      if (i > 0) result.append(".");
      result.append(0xff & ip[i]);
    }
    return result.toString();
  }

  /* Start connecting to server. After that it will send request to server
   * and receive text from server for switching mode Joke and Proverb mode
   */
  static void getMode(String AdminInput, String serverName) {
    Socket sock;
    BufferedReader fromServer;  // to receive data from server
    PrintStream toServer;       // to send out request to server
    String textFromServer;      // to store text from server

    try {
      sock = new Socket(serverName, PORT);   // Begin connecting to server with port number

      /* Create Input/Output streams for the socket */
      fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      toServer = new PrintStream(sock.getOutputStream());

      /* Send Admin key to server so server can switch the mode
       * Admin will receive whatever mode that change to after press enter
       */
      toServer.println(AdminInput);
      toServer.flush();

      /* read text (mode) two to three lines from server
       * print text (jokemode or provebmode) from server after there's no text left
       */
      for (int i = 1; i <= 3; i++) {
        textFromServer = fromServer.readLine();   // read line(s) from server
        if (textFromServer != null) System.out.println(textFromServer);
      }

      sock.close();  // close connection but server will be still on
    }
    catch(IOException exc) {
      System.out.println("Socket error.");
      exc.printStackTrace();
    }
  }
}
