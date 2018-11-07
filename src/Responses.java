import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class Responses {
    public static void acceptedNickResponse(SelectionKey key, String oldNick, String newNick) {
        //TODO SUCCESS, pode utilizar esse nick
        sendMessageToClient(key, "OK");
    }

    public static void rejectedNickResponse(SelectionKey key) {
        //TODO ERROR, nome já escolhido
        sendMessageToClient(key, "ERROR");
    }

    public static void joinedRoomResponse(SelectionKey key, String nickname) {
        sendMessageToClient(key, "JOINED " + nickname);
    }

    public static void leaveRoomResponseToClient(SelectionKey key) {
        sendMessageToClient(key, "OK");
    }

    public static void leaveRoomResponseToOthers(LinkedList<SelectionKey> l, String whoLeft) {
        while (l.peekFirst() != null) {
            sendMessageToClient(l.pop(), "LEFT " + whoLeft);
        }
    }

    public static void byeResponse(SelectionKey key) {
        sendMessageToClient(key, "BYE");
    }

    public static void sendMessageToClient(SelectionKey key, String message) {
        SocketChannel sc = (SocketChannel) key.channel();
//        Socket s = sc.socket();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        buffer.clear();
        buffer.put((message + "\n").getBytes());
        buffer.flip();
        try {
            sc.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

