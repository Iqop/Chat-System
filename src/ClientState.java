import java.net.Socket;
import java.nio.channels.SelectionKey;

public class ClientState {
    String room;
    String nick;
    String state;
    SelectionKey key;

    public ClientState(String nick, SelectionKey key) {
        room = "";
        this.nick = new String(nick);
        state = "outside";
        this.key = key;
    }

    public ClientState() {
        room = "";
        this.nick = "";
        state = "init";
        this.key = null;
    }


    public void setNick(String nick) {
        this.nick = new String(nick);
    }

    public String getNick() {
        return this.nick;
    }

    public void setRoom(String roomName) {
        this.room = new String(roomName);
    }

    public String getRoom() {
        return this.room;
    }

    public void setState(String state) {
        this.state = new String(state);
    }

    public String getState() {
        return this.state;
    }


    public void setSelectionKey(SelectionKey key) {
        this.key = key;
    }

    public SelectionKey getKey() {
        return this.key;
    }

}
