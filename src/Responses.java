import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Responses {
    public static void acceptedNickResponse(SelectionKey key, String oldNick, String newNick) {
        //TODO SUCCESS, pode utilizar esse nick
        sendMessageToClient(key,"OK");
    }

    public static void rejectedNickResponse(SelectionKey key) {
        //TODO ERROR, nome já escolhido
        sendMessageToClient(key,"ERROR");
    }

    public static void leaveRoomResponse(SelectionKey key) {

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

