import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.Base64;

/*
    Protocol Struture <id fragmentedBit messageType nickname message>
    Every packet 128 byte
 */

public class Peer{
    private static final int chunkSize = 128;
    private String peerID;
    private int lastSendPacketNumber = 0;
    private int PEER_PORT = 1234;
    private InetAddress broadcastIP = getBroadcastIP();
    private static final int BUFFER_SIZE = 1024;
    private GUI gui;
    private PrivateKey private_key;
    private PublicKey public_key;
    private String nickname;
    private HashMap<String, PublicKey> activeUsers = new HashMap<>();

    public Peer() throws Exception {

        getBroadcastIP();

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramSocket socket = new DatagramSocket(PEER_PORT);
        socket.setBroadcast(true);

        Thread input_thread = new Thread(()->{
            while(true){
                DatagramPacket input_packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(input_packet);
                    String received = new String(input_packet.getData(), 0, input_packet.getLength());
                    System.out.println(received);
                    if (received.substring(6,9).contentEquals("MSG"))
                        gui.addText(received.substring(4));
                    /*else if (received.substring(6,9).contentEquals("NCK")) {
                        String[] arr = received.split(" ", 3);
                        String n = arr[1];
                        String pkString = arr[2];

                        byte[] keyBytes = Base64.getDecoder().decode(pkString);
                        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyBytes);
                        PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(pkSpec);

                        gui.addNewUser(n);

                        activeUsers.put(n,pk);
                    }*/
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        Thread output_thread = new Thread(()->{
            while(true){
                String body = gui.getInput_message();
                String encryptedMessage;
                String prefix;
                String msg;
                ArrayList<String> fragmentedMessages = new ArrayList<>();

                if (body != null) {
                    body = body.substring(4);
                    String messageType = gui.getInput_message().substring(0,3);
                    prefix = getPeerID() + lastSendPacketNumber + " F " + messageType + " " + getNickname() + " ";
                    msg = prefix + body;

                    System.out.println(msg.getBytes().length + " ");

                    if(msg.getBytes().length > chunkSize){
                        System.out.println("entered if");
                        int maxMessageSize = chunkSize - prefix.getBytes().length; // Maximum message size without prefix

                        int numberOfFragments = (body.getBytes().length + maxMessageSize - 1) / maxMessageSize;  // How many fragments there will be

                        System.out.println(body.getBytes().length + " " + maxMessageSize);
                        System.out.println(numberOfFragments);

                        for(int i = 0; i < numberOfFragments; i++){
                            System.out.println("entered for");
                            int start = i * maxMessageSize;
                            int end = Math.min(start + maxMessageSize, body.getBytes().length);

                            byte[] part = Arrays.copyOfRange(body.getBytes(), start, end);
                            fragmentedMessages.add(new String(part));
                        }
                    }

                    if(!fragmentedMessages.isEmpty()){
                        for(int i = 0; i < fragmentedMessages.size(); i++){
                            String m;
                            if(i != fragmentedMessages.size()-1)
                                m = getPeerID() + lastSendPacketNumber + " T " + messageType + " " + getNickname() + " " + fragmentedMessages.get(i);  // if not last packet fragmented bit T
                            else
                                m = getPeerID() + lastSendPacketNumber + " F " + messageType + " " + getNickname() + " " + fragmentedMessages.get(i);


                            /*try {
                                encryptedMessage = encrpytMessage(m);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }*/

                            byte[] sendData = m.getBytes();
                            DatagramPacket sendPacket = null;
                            sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, PEER_PORT);
                            try {
                                socket.send(sendPacket);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            lastSendPacketNumber++;
                        }
                    }
                    else {
                        System.out.println("Single packet");
                        /*try {
                            encryptedMessage = encrpytMessage(msg);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                        byte[] sendData = msg.getBytes();
                        DatagramPacket sendPacket = null;
                        sendPacket = new DatagramPacket(sendData, sendData.length, broadcastIP, PEER_PORT);
                        try {
                            socket.send(sendPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    gui.setInput_message(null);
                    lastSendPacketNumber++;
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
    public int getLastSendPacketNumber() {
        return lastSendPacketNumber;
    }

    public void setLastSendPacketNumber(int lastSendPacketNumber) {
        this.lastSendPacketNumber = lastSendPacketNumber;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
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

    public String encrpytMessage(String msg) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, this.getPublic_key());

        byte[] plainBytes = msg.getBytes();
        System.out.println(msg);

        byte[] encryptedBytes = cipher.doFinal(plainBytes);
        String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);

        return encryptedMessage;
    }
}
