import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Responses {
    public static void acceptedNickResponse(SelectionKey key, String oldNick, String newNick, Selector selector) {
        //TODO SUCCESS, pode utilizar esse nick
        sendMessageToClient(key,"OK");
        if (((ClientState)key.attachment()).getState().compareTo("inside")==0){
          difuseToChatRoom(key,((ClientState)key.attachment()).getRoom(),"NEWNICK "+oldNick+" "+newNick,selector,false);
        }
    }

    public static void rejectedNickResponse(SelectionKey key) {
        //TODO ERROR, nome já escolhido
        sendMessageToClient(key,"ERROR");
    }

    public static void leaveRoomResponse(SelectionKey key) {

    }
    
    
    public static void difuseToChatRoom(SelectionKey k,String room,String message,Selector selector,boolean sendToOwner){
      ByteBuffer buffer = ByteBuffer.allocate(16384);
      buffer.clear();
      buffer.put((message+"\n").getBytes());
      buffer.flip();
      try {
        for(SelectionKey key : selector.keys()){
          if(!key.isAcceptable() && (sendToOwner || !key.equals(k))) {
            if (((ClientState)key.attachment()).getRoom().compareTo(room)==0 && ((ClientState)key.attachment()).getState().compareTo("inside")==0) {
              ((SocketChannel) key.channel()).write(buffer);
              buffer.rewind();
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
  
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

