import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GatewayPeer extends Peer{
    private ServerSocket serverSocket;
    private final int  TCP_PORT = 12345;
    private final String IP = "10.0.2.4";
    public GatewayPeer() throws Exception {
        super();
        serverSocket= new ServerSocket(TCP_PORT);

        Thread tcpInputThread = new Thread(()->{
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    System.out.println(in.readLine());
                    System.out.println(out);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread tcpOutputThread = new Thread(()->{
            Socket socket = null;
            try {
                socket = new Socket(IP, TCP_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


                while (true) {
                    if(getGui().getInput_message() != null) {
                        out.println(getGui().getInput_message());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

        tcpInputThread.run();
        tcpOutputThread.run();

    }
}
