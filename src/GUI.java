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
    private JPanel main_panel;
    private JPanel title_panel;
    private JLabel app_name;
    private JPanel input_panel;
    private JLabel input_label;
    private JTextField input_field;


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

        chat = new JTextArea();
        chat.setEditable(false);
        chat.setSize(450,550);

        //JScrollPane scroll = new JScrollPane(chat, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        app_name = new JLabel("Anonymous Chat App");
        app_name.setFont(new Font("",Font.BOLD,24));

        main_panel = new JPanel();
        main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS));

        title_panel = new JPanel();
        title_panel.add(app_name);
        main_panel.add(title_panel);

        input_panel = new JPanel();
        input_label = new JLabel("Enter a message:");
        input_label.setFont(new Font("", Font.PLAIN,24));

        input_field = new JTextField(40);
        input_field.setFont(new Font("",Font.PLAIN,18));

        input_panel.add(input_label);
        input_panel.add(input_field);
        main_panel.add(input_panel);

        main_panel.add(chat);

        frame.setContentPane(main_panel);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == about){
            JOptionPane abt = new JOptionPane();
            JOptionPane.showMessageDialog(null, "Name: Sarper\nSurname: Özer\nSchool Number: 20220702142");
        }
    }

    public void addText (String s){
        chat.append(s);
    }
}
