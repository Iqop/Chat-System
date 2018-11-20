import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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
  static private Map<String, String> chatRooms = new HashMap<>();
  
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
              boolean ok = processInput(key, selector);
              
              // If the connection is dead, remove it from the selector
              // and close it
              if (!ok) {
                closeConnection(key, selector);
              }
              
            } catch (IOException ie) {
              closeConnection(key, selector);
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
  static private boolean processInput(SelectionKey key, Selector selector) throws IOException {
    buffer.clear();
    SocketChannel sc = (SocketChannel) (key.channel());
    sc.read(buffer);
    buffer.flip();
    
    if (buffer.limit() == 0) return false;
    
    
    for (String message : decoder.decode(buffer).toString().split("\n")) {
      
      System.out.println("mensagem que entrou: " + message);
      String commandPatternStr = "^\\s*/(\\w+)";
      Pattern commandPattern = Pattern.compile(commandPatternStr);
      Matcher matcher = commandPattern.matcher(message);
      
      if (matcher.find()) {
        
        boolean ok = false;
        String command = matcher.group(1);
        String nickName = (key.attachment() != null) ? (((ClientState) key.attachment()).getNick()) : ("");
        System.out.println("command " + command);
        switch (command) {
          case "nick":
            if (message.split("nick(\\s)+").length > 1) {
              String newNick = message.split("nick(\\s)+")[1].replaceAll("\r", "".replaceAll("\n", ""));
              System.out.println(newNick);
              if (!searchNick(newNick)) {
                ok = true;
                if (key.attachment() == null) {
                  System.out.println("Added new NickName");
//                            Socket aux = ((SocketChannel) key.channel()).socket();
                  key.attach(new ClientState(newNick));
                  addNick(newNick);
                } else {
                  removeNick(nickName);
                  addNick(newNick);
                  ((ClientState) key.attachment()).setNick(newNick);
                }
                if (((ClientState) key.attachment()).getRoom().compareTo("") != 0) {
                  leaveRoom(nickName);
                  joinRoom(((ClientState) key.attachment()).getRoom(), nickName);
                }
                
                Responses.acceptedNickResponse(key, nickName, newNick, selector);
                if (((ClientState) key.attachment()).getState().equals("init")) {
                  ((ClientState) key.attachment()).setState("outside");
                }
              } else {
                System.out.println("Negated new nick name: already exists");
              }
            }
            
            if (!ok) {
              Responses.sendErrorResponse(key);
            }
            
            break;
          
          
          case "join":
            ok = false;
            
            if (key.attachment() != null) {
              if (message.split("join(\\s)+").length > 1) {
                ok = true;
                String room = message.split("join(\\s)+")[1];
                room = room.replaceAll("\r", "").replaceAll("\n", "");
                System.out.println(room);
                if (((ClientState) key.attachment()).getRoom().compareTo("") == 0) {
                  //no room
                  
                  joinRoom(room, nickName);
                  ((ClientState) key.attachment()).setRoom(room);
                  ((ClientState) key.attachment()).setState("inside");
                  
                  Responses.joinedRoomResponse(key, nickName, selector);
                } else {
                  leaveRoom(nickName);
                  
                  //Responses.leaveRoomResponseToClient(key);
                  Responses.leaveRoomResponseToOthers(key, nickName, selector);
                  
                  joinRoom(room, nickName);
                  ((ClientState) key.attachment()).setRoom(room);
                  ((ClientState) key.attachment()).setState("inside");
                  
                  Responses.joinedRoomResponse(key, nickName, selector);
                }
              }
            }
            
            if (!ok) {
              Responses.sendErrorResponse(key);
            }
            
            break;
          
          
          case "leave":
            
            if (key.attachment()!=null && ((ClientState)key.attachment()).getState().compareTo("inside")==0) {
              leaveRoom(nickName);
              //TODO send leave message
              Responses.leaveRoomResponseToClient(key);
              Responses.leaveRoomResponseToOthers(key, nickName, selector);
              ((ClientState) key.attachment()).setRoom("");
              ((ClientState) key.attachment()).setState("outside");
              
            }else Responses.sendErrorResponse(key);
            break;
          
          
          case "bye":
            Responses.byeResponse(key);
            if (key.attachment() != null) {
              if (((ClientState) key.attachment()).getState().compareTo("inside") == 0) {
                Responses.leaveRoomResponseToOthers(key, nickName, selector);
                removeNick(((ClientState) key.attachment()).getNick());
              }
            }
            closeConnection(key, selector);
            break;
          
          
          case "priv":
            String[] aux = message.split("\\s+");
            
            ok = false;
            
            if (aux.length >= 3) {
              if (userNames.contains(aux[1])) {
                SelectionKey receiverKey = getReceiverKey(aux[1], key, selector);
                if (receiverKey != null && key.attachment() != null) {
                  
                  StringBuilder newMessage = new StringBuilder();
                  for (int i = 2; i < aux.length; i++)
                    newMessage.append(aux[i]).append(" ");
                  message = newMessage.toString();
                  
                  if (message.charAt(0) == '/') {
                    message = message.replaceFirst("/", "");
                  }
                  
                  message = message.replace("\n", "");
                  
                  Responses.sendPrivateMessageToClient(key, receiverKey, message);
                  ok = true;
                }
              }
            }
            if (!ok) {
              Responses.sendErrorResponse(key);
            }
            
            break;
          
          
          default:
            Responses.sendErrorResponse(key);
            break;
          
        }
      } else {
        if (key.attachment() == null) {
          Responses.sendErrorResponse(key);
        } else if (!(((ClientState) key.attachment()).getState().equals("inside"))) {
          Responses.sendErrorResponse(key);
        } else {
          String unformattedCodePattern = "^//+(.*)";
          Pattern unfPattern = Pattern.compile(unformattedCodePattern);
          Matcher matcherUnf = unfPattern.matcher(message);
          
          if (matcherUnf.find()) {
            message = matcherUnf.group(0).replaceFirst("/", "");
          }
          
          message = message.replace("\n", "");
          
          
          if (((ClientState) key.attachment()).getState().compareTo("inside") == 0) {
            Responses.diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), "MESSAGE " + ((ClientState) key.attachment()).getNick() + " " + message, selector, true);
//                      Responses.diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), ((ClientState) key.attachment()).getNick() + ": " + message, selector, true);
          }
        }
      }
    }
    return true;
  }
  
  private static void closeConnection(SelectionKey key, Selector selector) {
    
    SocketChannel sc = (SocketChannel) key.channel();
    Socket s = null;
    try {
      if (key.attachment() != null) {
        if (searchNick(((ClientState) key.attachment()).getNick())) {
          Responses.leaveRoomResponseToOthers(key, ((ClientState) key.attachment()).getNick(), selector);
        }
        removeNick(((ClientState) key.attachment()).getNick());
        leaveRoom(((ClientState) key.attachment()).getNick());
      }
      s = sc.socket();
      System.out.println("Closing connection to " + s);
      s.close();
      sc.close();
      
    } catch (IOException ie) {
      System.err.println("Error closing socket " + s + ": " + ie);
    }
    key.cancel();
  }
  
  
  private static boolean searchNick(String nick) {
    for (String names : userNames) {
      if (names.compareTo(nick) == 0) {
        return true;
      }
    }
    return false;
  }
  
  private static void removeNick(String nick) {
    int id = -1;
    for (int i = 0; i < userNames.size(); i++) {
      if (userNames.get(i).compareTo(nick) == 0) {
        id = i;
        i = userNames.size();
      }
    }
    if (id != -1)
      userNames.remove(id);
  }
  
  private static void addNick(String nick) {
    userNames.add(nick);
  }
  
  
  private static void joinRoom(String roomName, String nickName) {
    chatRooms.put(nickName, roomName);
  }
  
  private static void leaveRoom(String nickName) {
    chatRooms.remove(nickName);
  }
  
  public static LinkedList<String> getRooms() {
    LinkedList<String> aux = new LinkedList<>();
    Map<String, String> aux2 = chatRooms;
    
    for (String key : aux2.keySet())
      if (!aux.contains(aux2.get(key)))
        aux.addFirst(aux2.get(key));
    
    return aux;
  }
  
  private static SelectionKey getReceiverKey(String nick, SelectionKey currentUserKey, Selector selector) {
    for (SelectionKey key : selector.keys()) {
      if (!key.isAcceptable() && !key.equals(currentUserKey) && key.attachment() != null) {
        if (((ClientState) key.attachment()).getNick().equals(nick)) {
          return key;
        }
      }
    }
    return null;
  }
}
