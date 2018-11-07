import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Responses {
  public static void acceptedNickResponse(SelectionKey key,String oldNick,String newNick){
    //TODO SUCCESS, pode utilizar esse nick
    SocketChannel sc = (SocketChannel) key.channel();
    Socket s = sc.socket();
    ByteBuffer buffer = ByteBuffer.allocate(1000000);
    buffer.clear();
    buffer.put("OK\n".getBytes());
    buffer.flip();
    try {
      sc.write(buffer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static void rejectedNickResponse(SelectionKey key){
    //TODO ERROR, nome já escolhido
    
  }
}
