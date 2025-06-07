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
    private InetAddress broadcastIP;
    private static final int BUFFER_SIZE = 1024;
    private GUI gui;
    private PrivateKey private_key;
    private PublicKey public_key;
    private String nickname;
    private HashMap<String, PublicKey> activeUsers = new HashMap<>();
    private HashMap<String, Integer> lastReceived = new HashMap<>();
    private HashMap<String, String> fragmentedUserKeys = new HashMap<>();
    private HashMap<String, String> fragmentedMessages = new HashMap<>();

    public Peer() throws Exception {
        broadcastIP = getBroadcastIP();

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramSocket socket = new DatagramSocket(PEER_PORT);
        socket.setBroadcast(true);

        Thread input_thread = new Thread(()->{
            while(true){
                DatagramPacket input_packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(input_packet);
                    String received = new String(input_packet.getData(), 0, input_packet.getLength());
                    String[] parts = received.split(" ");
                    String userID = parts[0];
                    String fragmented = parts[1];
                    String messageType = parts[2];
                    String nick = parts[3];
                    String message = "";

                    for(int i = 4; i < parts.length; i++){
                         message += parts[i] + " ";
                    }

                    System.out.println(received);

                    if (messageType.contentEquals("MSG") && fragmented.contentEquals("F")) { // Not fragmented message
                        if (!fragmentedMessages.containsKey(nick)) {  // Single packet message
                            gui.addText(nick + ": " + message);
                        }
                        else {                                        // Last packet of a fragmented message
                            String oldMessage = fragmentedMessages.get(nick);
                            String newMessage = oldMessage + message;

                            gui.addText(nick + ": " + newMessage);
                            fragmentedMessages.clear();               // Clearing map for security
                        }
                    }
                    else if (messageType.contentEquals("MSG") && fragmented.contentEquals("T")){ // Fragmented message
                        if (!fragmentedMessages.containsKey(nick)) {  // First fragmented packet
                            fragmentedMessages.put(nick, message);
                        }
                        else {                                        // Fragmented messages in between
                            String oldMessage = fragmentedMessages.get(nick);
                            String newMessage = oldMessage + message;
                            fragmentedMessages.replace(nick, newMessage);
                        }

                    }
                    else if (messageType.contentEquals("NCK") && !lastReceived.keySet().contains(nick)) { // First NCK packet
                        fragmentedUserKeys.put(nick,message.substring(0,message.length()-1));
                        lastReceived.put(nick, 0);
                    }
                    else if (messageType.contentEquals("NCK") && fragmented.contentEquals("T")) {  // NCK packets in between first and last
                        String tempKey = fragmentedUserKeys.get(nick);
                        String newKey = tempKey + message.substring(0,message.length()-1);
                        fragmentedUserKeys.replace(nick,newKey);

                        int last = lastReceived.get(nick);
                        last++;
                        lastReceived.replace(nick, last);
                    }
                    else if (messageType.contentEquals("NCK") && fragmented.contentEquals("F")) { // Last NCK packet
                        String tempKey = fragmentedUserKeys.get(nick);
                        String newKey = tempKey + message.substring(0,message.length()-1);
                        fragmentedUserKeys.replace(nick,newKey);

                        int last = lastReceived.get(nick);
                        last++;
                        lastReceived.replace(nick, last);

                        byte[] keyBytes = Base64.getDecoder().decode(fragmentedUserKeys.get(nick));
                        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyBytes);
                        PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(pkSpec);

                        activeUsers.put(nick, pk);
                        gui.addNewUser(nick);

                        fragmentedUserKeys.clear(); // Clearing map for security
                    }
                } catch (Exception e) {
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
                                m = getPeerID() + lastSendPacketNumber + " T " + messageType + " " + getNickname() + " " + fragmentedMessages.get(i);  // if not last packet, fragmented bit T
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

    public GUI getGui() {
        return gui;
    }
}
