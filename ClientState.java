public class ClientState {
    private String room;
    private String nick;
    private String state;


    public ClientState(String nick) {
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

    void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return this.state;
    }

}

