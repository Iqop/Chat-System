import java.nio.channels.SelectionKey;

public class ClientState {
    String room;
    String nick;
    String state;


    public ClientState(String nick, SelectionKey key) {
        room = "";
        this.nick = nick;
        state = "outside";
    }

    public ClientState() {
        room = "";
        this.nick = "";
        state = "init";
    }


    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public void setRoom(String roomName) {
        this.room = roomName;
    }

    public String getRoom() {
        return this.room;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

}
