import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Peer{
    private static int PEER_PORT = 1234;
    private final static String PEER_IP;
    private static final int BUFFER_SIZE = 1024;
    private static final String BROADCAST_IP = "192.168.147.208";
    private static GUI gui;

    static {
        try {
            PEER_IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramSocket socket = new DatagramSocket(PEER_PORT);
        socket.setBroadcast(true);

        InetAddress broadcastIP = InetAddress.getByName(BROADCAST_IP);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        Thread input_thread = new Thread(()->{
            while(true){
                DatagramPacket input_packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(input_packet);
                    String received = new String(input_packet.getData(), 0, input_packet.getLength());
                    gui.addText(received);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        System.out.println(PEER_IP + " " + PEER_PORT);

        Thread output_thread = new Thread(()->{
            while(true){
                String message = null;
                try {
                    message = userInput.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                byte[] sendData = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, PEER_PORT);
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        gui = new GUI();

        input_thread.start();
        output_thread.start();
    }
}
