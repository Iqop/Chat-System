import java.nio.channels.SelectionKey;

public class ClientState {
  String sala;
  String nick;
  String state;
  
  public ClientState(String nick){
    sala="";
    this.nick = new String(nick);
    state="outside";
  }
  
  public ClientState(){
    sala="";
    this.nick = "";
    state="init";
  }
  
  
  public void setNick(String nick){
    this.nick = new String(nick);
  }
  
  public String getNick(){
    return this.nick;
  }
  
  public void setRoom(String roomName){
    this.sala = new String(roomName);
  }
  
  public String getRoom(){
    return this.sala;
  }
  
  public void setState(String state){
    this.state = new String(state);
  }
  
  public String getState(){
    return this.state;
  }
  
}
