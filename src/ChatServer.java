import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatServer {
    // A pre-allocated buffer for the received data
    static private final ByteBuffer buffer = ByteBuffer.allocate(16384);

    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();
    static private LinkedList<String> userNames = new LinkedList<>();
    static private LinkedList<String> chatRooms = new LinkedList<>();
    
    static public void main(String args[]) throws Exception {
        // Parse port from command line
        int port = Integer.parseInt(args[0]);

        try {
            // Instead of creating a ServerSocket, create a ServerSocketChannel
            ServerSocketChannel ssc = ServerSocketChannel.open();

            // Set it to non-blocking, so we can use select
            ssc.configureBlocking(false);

            // Get the Socket connected to this channel, and bind it to the
            // listening port
            ServerSocket ss = ssc.socket();
            InetSocketAddress isa = new InetSocketAddress(port);
            ss.bind(isa);

            // Create a new Selector for selecting
            Selector selector = Selector.open();

            // Register the ServerSocketChannel, so we can listen for incoming
            // connections
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Listening on port " + port);

            while (true) {
                // See if we've had any activity -- either an incoming connection,
                // or incoming data on an existing connection
                int num = selector.select();

                // If we don't have any activity, loop around and wait again
                if (num == 0) {
                    continue;
                }

                // Get the keys corresponding to the activity that has been
                // detected, and process them one by one
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    // Get a key representing one of bits of I/O activity
                    SelectionKey key = it.next();

                    // What kind of activity is it?
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) ==
                            SelectionKey.OP_ACCEPT) {

                        // It's an incoming connection.  Register this socket with
                        // the Selector so we can listen for input on it
                        Socket s = ss.accept();
                        System.out.println("Got connection from " + s);

                        // Make sure to make it non-blocking, so we can use a selector
                        // on it.
                        SocketChannel sc = s.getChannel();
                        sc.configureBlocking(false);

                        // Register it with the selector, for reading
                        sc.register(selector, SelectionKey.OP_READ);

                    } else if ((key.readyOps() & SelectionKey.OP_READ) ==
                            SelectionKey.OP_READ) {

                        SocketChannel sc = null;

                        try {

                            // It's incoming data on a connection -- process it
                            sc = (SocketChannel) key.channel();
                            boolean ok = processInput(key);

                            // If the connection is dead, remove it from the selector
                            // and close it
                            if (!ok) {
                                key.cancel();

                                Socket s = null;
                                try {
                                    s = sc.socket();
                                    System.out.println("Closing connection to " + s);
                                    s.close();
                                } catch (IOException ie) {
                                    System.err.println("Error closing socket " + s + ": " + ie);
                                }
                            }

                        } catch (IOException ie) {

                            // On exception, remove this channel from the selector
                            key.cancel();

                            try {
                                sc.close();
                            } catch (IOException ie2) {
                                System.out.println(ie2);
                            }

                            System.out.println("Closed " + sc);
                        }
                    }
                }

                // We remove the selected keys, because we've dealt with them.
                keys.clear();
            }
        } catch (IOException ie) {
            System.err.println(ie);
        }
    }


    // Just read the message from the socket and send it to stdout
    static private boolean processInput(SelectionKey key) throws IOException {
      
      buffer.clear();
      SocketChannel sc = (SocketChannel)(key.channel());
      sc.read(buffer);
      buffer.flip();
  
      if (buffer.limit() == 0) return false;
      
      
      String message = decoder.decode(buffer).toString();
      String commandPatternStr = "^\\/(\\w+)";
      Pattern commandPattern = Pattern.compile(commandPatternStr);
      Matcher matcher = commandPattern.matcher(message);
      
      if (matcher.find()){
        String command = matcher.group(1);
        System.out.println("command "+command);
        switch (command) {
          case "nick":
            String newNick = message.split(" ")[1];
            
            if (!searchNick(newNick)) {
              String oldNick = ((ClientState) key.attachment()).getNick();
              if (key.attachment() == null) {
                key.attach(new ClientState(newNick));
              } else {
                removeNick(oldNick);
                addNick(newNick);
                ((ClientState) key.attachment()).setNick(newNick);
              }
              Responses.acceptedNickResponse(key,oldNick,newNick);
          }else{
              Responses.rejectedNickResponse(key);
            }
            break;
          case "join":
  
            break;
          case "leave":
  
            break;
          case "bye":
  
            break;
        }
      }else{
        String unformatedCodePattern = "^\\/\\/+(.*)";
        Pattern unfPattern = Pattern.compile(unformatedCodePattern);
        Matcher matcherUnf = unfPattern.matcher(message);
        
        if (matcherUnf.find()){
          message = new String("/"+matcherUnf.group(1));
        }
        
        //TODO difundir a mensagem para todos os clientes
      }
      
        
      return true;
    }
    
    
    public static boolean searchNick(String nick){
      for(String names : userNames){
        if (names.compareTo(nick)==0){
          return true;
        }
      }
      return false;
    }
    
    public static void removeNick(String nick){
      int id=-1;
      for(int i=0;i<userNames.size();i++){
        if (userNames.get(i).compareTo(nick)==0){
          id=i;
          i=userNames.size();
        }
      }
      userNames.remove(id);
    }
    
    public static void addNick(String nick){
      userNames.add(nick);
    }
}
