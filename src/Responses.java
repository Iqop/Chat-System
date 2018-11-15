import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

class Responses {

    static private final Charset charset = Charset.forName("UTF8");
    static private final CharsetEncoder encoder = charset.newEncoder();

    static void acceptedNickResponse(SelectionKey key, String oldNick, String newNick, Selector selector) {
        //TODO SUCCESS, pode utilizar esse nick
        sendMessageToClient(key, "OK");
        if (((ClientState) key.attachment()).getState().compareTo("inside") == 0) {
//            diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), "NEWNICK " + oldNick + " " + newNick, selector, false);
            diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), oldNick + " mudou de nome " + newNick, selector, false);
        }
    }

    static void sendErrorResponse(SelectionKey key) {
        //TODO ERROR, nome já escolhido
        sendMessageToClient(key, "ERROR");
    }

    static void joinedRoomResponse(SelectionKey key, String nickname, Selector selector) {
        sendMessageToClient(key, "OK");
        diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), "JOINED " + nickname, selector, false);
    }


    static void leaveRoomResponseToClient(SelectionKey key) {
        sendMessageToClient(key, "OK");
    }

    static void leaveRoomResponseToOthers(SelectionKey key, String whoLeft, Selector selector) {
        diffuseToChatRoom(key, ((ClientState) key.attachment()).getRoom(), "LEFT " + whoLeft, selector, false);
    }

    static void byeResponse(SelectionKey key) {
        sendMessageToClient(key, "BYE");
    }

    static void diffuseToChatRoom(SelectionKey k, String room, String message, Selector selector, boolean sendToOwner) {
        for (SelectionKey key : selector.keys()) {
            if (!key.isAcceptable() && (sendToOwner || !key.equals(k))) {
                if (key.attachment() != null) {
                    if (((ClientState) key.attachment()).getRoom().compareTo(room) == 0 && ((ClientState) key.attachment()).getState().compareTo("inside") == 0) {
                        sendMessageToClient(key, message);
                    }
                }
            }
        }
    }

    static void sendPrivateMessageToClient(SelectionKey senderKey, SelectionKey receiverKey, String message) {
        String sender = ((ClientState) senderKey.attachment()).getNick();
        sendMessageToClient(receiverKey, "PRIVATE " + sender + " " + message);
        sendMessageToClient(senderKey, "OK");
    }

    private static void sendMessageToClient(SelectionKey key, String message) {
        SocketChannel sc = (SocketChannel) key.channel();
//        Socket s = sc.socket();
        ByteBuffer buffer = ByteBuffer.allocate(16384);
        buffer.clear();
        try {
            buffer.put(encoder.encode(CharBuffer.wrap(message + "\n")));
            buffer.flip();
            try {
                sc.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.rewind();
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }
}

