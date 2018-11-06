import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer {
    // A pre-allocated buffer for the received data
    static private final ByteBuffer buffer = ByteBuffer.allocate(16384);

    // Decoder for incoming text -- assume UTF-8
    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetDecoder decoder = charset.newDecoder();
    static private LinkedList<String> usersNickNames = new LinkedList<>();
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
                            boolean ok = processInput(sc);

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
    static private boolean processInput(SocketChannel sc) throws IOException {

        boolean finished = false;
        String message = "";

        while (!finished) {
            // Read the message to the buffer
            buffer.clear();
            sc.read(buffer);
            buffer.flip();

            // If no data, close the connection
            if (buffer.limit() == 0) {
//                System.out.println("No DATA");
                return false;
            }


            // Decode and print the message to stdout
//            System.out.println("Nome: " + Charset.defaultCharset().name());

            message += decoder.decode(buffer).toString();


            if (message.charAt(message.length() - 1) == '\n')
                finished = true;
        }
        System.out.print("Recebido no servidor: " + message);


        String[] aux = message.split(" ");


        // Será aqui?
        switch (aux[0]) {
            case "/nick":
                if (addNickName(aux[1])) {
                    buffer.clear();
                    String outgoingMessage = "Nickname set to " + aux[1] + "\n";
                    buffer.put(outgoingMessage.getBytes());
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        sc.write(buffer);
                    }
                    buffer.rewind();

                } else {
                    // return "error"
                }
                break;
            case "/join":
                joinRoom(aux[1], "");
                break;
            case "/leave":
                leaveRoom(aux[1], "");
                break;
            case "/bye":
                byeCommand(aux[1]);
                break;
            default:
                // erro
                break;
        }

        return true;
    }

    public static boolean checkNickNameAvailable(String nickName) {
        return !chatRooms.containsKey(nickName);
    }

    public static boolean addNickName(String nick) {
        if (checkNickNameAvailable(nick)) {
            chatRooms.put(nick, "");
            return true;
        }
        return false;
    }

    public static boolean joinRoom(String roomName, String nickName) {
        if (getUsersNickNames().contains(nickName)) {
            if (!chatRooms.get(nickName).equals("")) {
                return false;           // já tem sala atribuída
            }
        }
        chatRooms.put(nickName, roomName);
        return true;
    }

    private static void leaveRoom(String roomName, String nickName) {
        if (getUsersNickNames().contains(nickName)) {
            if (!chatRooms.get(nickName).equals("")) {
                chatRooms.replace(nickName, "");
            }
        }
    }

    public static void byeCommand(String nickname) {
        chatRooms.keySet().remove(nickname);
    }

    public static LinkedList<String> getRooms() {
        LinkedList<String> aux = new LinkedList<>();
        Map<String, String> aux2 = chatRooms;

        for (String key : aux2.keySet())
            if (!aux.contains(aux2.get(key)))
                aux.addFirst(aux2.get(key));

        return aux;
    }

    public static LinkedList<String> getUsersNickNames() {
        Map<String, String> aux2 = chatRooms;
        LinkedList<String> aux = new LinkedList<>();

        for (String key : aux2.keySet())
            if (!aux.contains(key))
                aux.addFirst(key);

        return aux;
    }
}