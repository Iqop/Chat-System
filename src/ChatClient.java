import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.TimeUnit;


public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui

    private String server;
    private int port;
    private BufferedReader in;
    private DataOutputStream out;
    private Socket clientSocket;

    final Charset charset = Charset.forName("UTF8");
    final CharsetDecoder decoder = charset.newDecoder();

    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message);
    }


    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                    chatBox.setText("");
                }
            }
        });
        // --- Fim da inicialização da interface gráfica

        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui

        this.server = server;
        this.port = port;
        this.clientSocket = new Socket(this.server, this.port);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        this.out = new DataOutputStream(clientSocket.getOutputStream());

    }


    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
        // PREENCHER AQUI com código que envia a mensagem ao servidor
        message += "\n";
        byte[] aux = message.getBytes("UTF8");
        out.write(aux, 0, aux.length);            //            out.writeBytes(message + "\n");
        out.flush();

//        System.out.println("Mensagem da caixa de texto: " + (message+"\n").toString());
    }


    // Método principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
        while (true) {

            String messageFromServer = in.readLine();

            if (messageFromServer == null) {
                if (!serverAvailabilityCheck()) {
                    printMessage("Server's down\n".toUpperCase());
                    closeWindow(3);
                    return;
                } else {
                    break;
                }
            }


            printMessage(messageFromServer + "\n");

            if (messageFromServer.equals("BYE")) {
                closeWindow(1);
            }
        }
//        System.out.println("From server: " + messageFromServer);
    }


    private void closeWindow(int timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }


    private boolean serverAvailabilityCheck() {
        try (Socket ignored = new Socket(this.server, this.port)) {
            return true;
        } catch (IOException ex) {
            /* ignore */
        }
        return false;
    }


    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}
