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
  
}
