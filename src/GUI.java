import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class GUI implements ActionListener {

    private JFrame frame;
    private JMenuBar menu_bar;
    private JMenu file_menu;
    private JMenu help_menu;
    private JMenuItem generate_keys;
    private JMenuItem connect;
    private JMenuItem disconnect;
    private JMenuItem about;
    private JMenuItem exit;
    private JTextArea chat;
    private JPanel top_panel;
    private JPanel main_panel;
    private JPanel title_panel;
    private JLabel app_name;
    private JPanel input_panel;
    private JLabel input_label;
    private JTextField input_field;
    private String input_message;
    private JSplitPane bottom_panel;
    private JTextArea active_users;
    private JLabel active_users_label;
    private JPanel active_users_panel;
    private Peer peer;

    GUI(Peer peer){
        this.peer = peer;
        frame = new JFrame();
        frame.setSize(800,800);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        menu_bar = new JMenuBar();

        file_menu = new JMenu("File");
        help_menu = new JMenu("Help");

        generate_keys = new JMenuItem("Generate Keys");
        connect = new JMenuItem("Connect");
        about = new JMenuItem("About");
        exit = new JMenuItem("Exit");

        about.addActionListener(this);
        generate_keys.addActionListener(this);
        connect.addActionListener(this);
        exit.addActionListener(this);


        file_menu.add(generate_keys);
        file_menu.add(connect);
        file_menu.add(exit);
        help_menu.add(about);

        menu_bar.add(file_menu);
        menu_bar.add(help_menu);

        frame.setJMenuBar(menu_bar);

        main_panel = new JPanel();
        main_panel.setLayout(new BorderLayout());
        
        app_name = new JLabel("Anonymous Chat App");
        app_name.setFont(new Font("",Font.BOLD,24));

        title_panel = new JPanel();
        title_panel.add(app_name);

        input_panel = new JPanel();
        input_label = new JLabel("Enter a message:");
        input_label.setFont(new Font("", Font.PLAIN,24));

        input_field = new JTextField(20);
        input_field.setFont(new Font("",Font.PLAIN,16));
        input_field.addActionListener(e -> {
            if (peer.getPrivate_key() != null && peer.getPublic_key() != null && peer.getNickname() != null)
                input_message = "MSG " + input_field.getText();
            else
                missingNicknameOrKeys();
            input_field.setText("");
        });

        input_panel.add(input_label);
        input_panel.add(input_field);

        top_panel = new JPanel();
        top_panel.setLayout(new BoxLayout(top_panel, BoxLayout.Y_AXIS));
        top_panel.add(title_panel);
        top_panel.add(input_panel);

        chat = new JTextArea();
        chat.setEditable(false);
        chat.setFont(new Font("", Font.PLAIN, 18));

        JScrollPane chat_scroll = new JScrollPane(chat);
        chat_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        active_users = new JTextArea();
        active_users.setEditable(false);
        active_users.setFont(new Font("", Font.PLAIN, 18));

        active_users_label = new JLabel("Active users");

        JScrollPane active_users_scroll = new JScrollPane(active_users);
        active_users_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        active_users_panel = new JPanel(new BorderLayout());
        active_users_panel.add(active_users_label, BorderLayout.NORTH);
        active_users_panel.add(active_users_scroll, BorderLayout.CENTER);

        bottom_panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chat_scroll, active_users_panel);
        bottom_panel.setResizeWeight(0.6);

        main_panel.add(top_panel, BorderLayout.NORTH);
        main_panel.add(bottom_panel, BorderLayout.CENTER);

        frame.setContentPane(main_panel);
        frame.setVisible(true);
    }

    public String getInput_message() {
        return input_message;
    }

    public void setInput_message(String input_message) {
        this.input_message = input_message;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == about){
            JOptionPane.showMessageDialog(null, "Name: Sarper Özer\nSchool Number: 20220702142\nYour nickname: " + peer.getNickname());
        }
        if(e.getSource() == generate_keys && peer.getPublic_key() == null && peer.getPrivate_key() == null){
            try {
                KeyPairGenerator k = KeyPairGenerator.getInstance("RSA");
                k.initialize(2048);
                KeyPair p = k.generateKeyPair();

                peer.setPublic_key(p.getPublic());
                peer.setPrivate_key(p.getPrivate());

                JOptionPane.showMessageDialog(null, "Keys generated");

            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
        else if (e.getSource() == generate_keys && peer.getPublic_key() != null && peer.getPrivate_key() != null){
            JOptionPane.showMessageDialog(null, "Keys already generated");
        }
        if(e.getSource() == connect && peer.getPublic_key() != null){
            peer.setNickname(JOptionPane.showInputDialog("Enter your nickname:"));
            byte[] pk = peer.getPublic_key().getEncoded();
            String pkString = Base64.getEncoder().encodeToString(pk);
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

            Random rnd = new Random();
            char first = chars.charAt(rnd.nextInt(chars.length()));
            char second = chars.charAt(rnd.nextInt(chars.length()));

            peer.setPeerID(new StringBuilder().append(first).append(second).toString());
            byte[] pKBytes = peer.getPublic_key().getEncoded();
            input_message = "NCK " + Base64.getEncoder().encodeToString(pKBytes);

            Peer.getActiveUsers().put(peer.getNickname(), peer.getPublic_key());
            addNewUser(peer.getNickname());
        }
        else if(e.getSource() == connect && peer.getPublic_key() == null){
            JOptionPane.showMessageDialog(null, "Please first create keys!");
        }
        if(e.getSource() == exit){
            System.exit(0);
        }
    }

    public void addText (String s){
        if (!chat.getText().contains(s))
            chat.append(s + "\n");
    }

    public void missingNicknameOrKeys(){
        JOptionPane.showMessageDialog(null, "Keys or nickname missing!\nPlease create keys and enter your nickname.");
    }

    public void addNewUser(String nickname){
        if(!active_users.getText().contains(nickname))
            active_users.append(nickname + "\n");
    }
}
