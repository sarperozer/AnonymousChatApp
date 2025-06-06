import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.security.KeyFactory;
import java.util.Base64;

public class Peer{
    private String peerID;
    private int PEER_PORT = 1234;
    private InetAddress broadcastIP = getBroadcastIP();
    private static final int BUFFER_SIZE = 1024;
    private GUI gui;
    private PrivateKey private_key;
    private PublicKey public_key;
    private String nickname;
    private HashMap<String, PublicKey> activeUsers = new HashMap<>();

    public Peer() throws IOException {

        getBroadcastIP();
        System.out.println(broadcastIP);

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramSocket socket = new DatagramSocket(PEER_PORT);
        socket.setBroadcast(true);

        Thread input_thread = new Thread(()->{
            while(true){
                DatagramPacket input_packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(input_packet);
                    String received = new String(input_packet.getData(), 0, input_packet.getLength());
                    if (received.substring(0,3).contentEquals("MSG"))
                        gui.addText(received.substring(4));
                    else if (received.substring(0,3).contentEquals("NCK")) {
                        String[] arr = received.split(" ", 3);
                        String n = arr[1];
                        String pkString = arr[2];


                        byte[] keyBytes = Base64.getDecoder().decode(pkString);
                        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyBytes);
                        PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(pkSpec);

                        gui.addNewUser(n);

                        activeUsers.put(n,pk);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (InvalidKeySpecException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        Thread output_thread = new Thread(()->{
            while(true){
                String message = gui.getInput_message();

                if (message != null) {
                    byte[] sendData = message.getBytes();
                    DatagramPacket sendPacket = null;
                    sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, PEER_PORT);
                    try {
                        socket.send(sendPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    gui.setInput_message(null);
                }
                try {
                    Thread.sleep(10); // Not working if deleted, thread runs too fast
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        gui = new GUI(this);

        input_thread.start();
        output_thread.start();
    }

    public PublicKey getPublic_key() {
        return public_key;
    }

    public PrivateKey getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(PrivateKey private_key) {
        this.private_key = private_key;
    }

    public void setPublic_key(PublicKey public_key) {
        this.public_key = public_key;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public InetAddress getBroadcastIP() throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

        for (NetworkInterface n_interface : interfaces) {

            if (!n_interface.isUp() || n_interface.isLoopback() || n_interface.isVirtual()) {
                continue;
            }

            String displayName = n_interface.getDisplayName().toLowerCase();

            if (displayName.contains("virtual") || displayName.contains("wsl") || displayName.contains("docker") ||   // Getting rid of virtual interfaces
                    displayName.contains("virtualbox"))
                continue;

            for (InterfaceAddress interfaceAddress : n_interface.getInterfaceAddresses()) {
                InetAddress broadcast = interfaceAddress.getBroadcast();

                if(broadcast != null){
                    return broadcast;
                }
            }
        }
        return null;
    }
}
