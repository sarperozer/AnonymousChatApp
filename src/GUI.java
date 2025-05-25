import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI implements ActionListener {

    private JFrame frame;
    private JMenuBar menu_bar;
    private JMenu file_menu;
    private JMenu help_menu;
    private JMenuItem generate_keys;
    private JMenuItem connect;
    private JMenuItem disconnect;
    private JMenuItem about;
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
    JPanel active_users_panel;

    GUI(){
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
        disconnect = new JMenuItem("Disconnect");
        about = new JMenuItem("About");

        about.addActionListener(this);
        generate_keys.addActionListener(this);
        connect.addActionListener(this);
        disconnect.addActionListener(this);


        file_menu.add(generate_keys);
        file_menu.add(connect);
        file_menu.add(disconnect);
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

        input_field = new JTextField(40);
        input_field.setFont(new Font("",Font.PLAIN,18));
        input_field.addActionListener(e -> {
            input_message = input_field.getText();
            addText(input_message);
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

        active_users = new JTextArea("Sarper\nMehmet\nZeynep");
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
            JOptionPane abt = new JOptionPane();
            JOptionPane.showMessageDialog(null, "Name: Sarper Özer\nSchool Number: 20220702142");
        }
    }

    public void addText (String s){
        chat.append(s + "\n");
    }

}
