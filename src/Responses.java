import java.nio.channels.SelectionKey;

public class Responses {
  public static void acceptedNickResponse(SelectionKey key,String oldNick,String newNick){
    //TODO SUCCESS, pode utilizar esse nick
  }
  public static void rejectedNickResponse(SelectionKey key){
    //TODO ERROR, nome já escolhido
    
  }
}
