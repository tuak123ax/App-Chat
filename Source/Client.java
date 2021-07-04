import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client extends JFrame implements Runnable
{
    private JButton btnFile=new JButton("File");
    private JButton btnSend;
    private JScrollPane chatPanel;
    private static JLabel lbReceiver = new JLabel(" ");
    private static JLabel lbRoom = new JLabel(" ");
    private static JPanel contentPane;
    private JTextPane txtMessage;
    private JTextPane chatWindow;
    JComboBox<String> onlineUsers = new JComboBox<String>();
    static Socket socket=null;
    static String name="";
    static String phong="";
    static String host="localhost";
    static int port=999;
    static DataInputStream dis;
    static DataOutputStream dos;
    static JComboBox<String>listUsers=new JComboBox<>();
    private static HashMap<String, JTextPane> chatWindows = new HashMap<String, JTextPane>();
    Thread receive;
    public static void main(String arg[]) throws IOException, ClassNotFoundException {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Client frame=new Client();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public Client() throws IOException, ClassNotFoundException {
        receive=new Thread();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss");
        JFrame frame=new JFrame();
        frame.setTitle("Client");
        frame.setSize(700,700);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setResizable(false);
        ImageIcon icon=new ImageIcon("chat.png");
        frame.setIconImage(icon.getImage());
        ImageIcon back=new ImageIcon("background.jpg");
        JLabel background=new JLabel(back);
        JPanel ground=new JPanel();
        ground.add(background);
        frame.add(ground);
        JTextField username=new HintTextField("Username");
        username.setBounds(170,100,300,25);
        JPasswordField password=new HintPasswordField("Password");
        password.setBounds(170,170,300,25);
        JButton button=new JButton("Login");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource()==button)
                {
                    String user=username.getText();
                    String pass=String.valueOf(password.getPassword());
                    String response=Login(user,pass);
                    if ( response.equals("Log in successful") ) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                name=user;
                                JOptionPane.showMessageDialog(null,"Đăng nhập thành công!");
                                frame.dispose();
                                JFrame selectFrame=new JFrame();
                                selectFrame.addWindowListener(new WindowAdapter() {
                                    public void windowClosing(WindowEvent e) {

                                        try {
                                            dos.writeUTF("Log out");
                                            dos.flush();

                                            try {
                                                receive.join();
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }

                                            if (dos != null) {
                                                dos.close();
                                            }
                                            if (dis != null) {
                                                dis.close();
                                            }
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                });
                                selectFrame.setTitle("Client");
                                selectFrame.setSize(600,600);
                                selectFrame.setVisible(true);
                                selectFrame.setResizable(false);
                                ImageIcon Selecticon=new ImageIcon("chat.png");
                                selectFrame.setIconImage(Selecticon.getImage());
                                JLabel Selectlabel=new JLabel("Chức năng");
                                Selectlabel.setBounds(200,0,200,40);
                                Selectlabel.setFont(new Font("Time New Romans", Font.PLAIN,30));
                                Selectlabel.setBorder(BorderFactory.createLineBorder(Color.black));
                                Selectlabel.setHorizontalAlignment(0);
                                JPanel mainSl=new JPanel();
                                JLabel info=new JLabel("User: "+name);
                                JLabel info2=new JLabel("Server port: "+port);
                                info.setBounds(0,0,150,30);
                                info.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                                info2.setBounds(0,30,150,30);
                                info2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                                mainSl.setLayout(null);
                                mainSl.add(Selectlabel);
                                mainSl.add(info);
                                mainSl.add(info2);
                                JButton privateChat=new JButton("Private Chat");
                                privateChat.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if(e.getSource()==privateChat)
                                        {
                                            try {
                                                privateChat();
                                            } catch (IOException ioException) {
                                                ioException.printStackTrace();
                                            }
                                            JFrame frame1=new JFrame();
                                            frame1.setVisible(true);
                                            frame1.setResizable(false);
                                            frame1.setSize(600,600);
                                            frame1.setTitle("Private Chat");
                                            receive = new Thread(new Receiver(dis));
                                            receive.start();
                                            contentPane = new JPanel();
                                            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                                            frame1.setContentPane(contentPane);
                                            JPanel header = new JPanel();
                                            txtMessage = new JTextPane();
                                            txtMessage.setEnabled(false);
                                            btnSend = new JButton("Gửi");
                                            btnSend.setBounds(500,500,90,50);
                                            chatPanel = new JScrollPane();
                                            chatPanel.setBounds(100,70,490,400);
                                            chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                                            contentPane.setLayout(null);
                                            JPanel leftPanel = new JPanel();
                                            leftPanel.setBackground(Color.CYAN);
                                            contentPane.add(header);
                                            header.setBounds(0,0,600,70);
                                            contentPane.add(leftPanel);
                                            leftPanel.setBounds(0,70,100,530);
                                            txtMessage.setBounds(100,500,400,50);
                                            contentPane.add(btnSend);
                                            contentPane.add(chatPanel);
                                            chatPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                            JLabel lblNewLabel_1 = new JLabel("User Online");
                                            lblNewLabel_1.setForeground(Color.RED);
                                            lblNewLabel_1.setBounds(20,120,90,30);
                                            lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
                                            leftPanel.setLayout(null);
                                            leftPanel.add(lblNewLabel_1);
                                            leftPanel.add(onlineUsers);
                                            leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                            onlineUsers.setBounds(10,150,90,30);
                                            onlineUsers.addItemListener(new ItemListener() {
                                                public void itemStateChanged(ItemEvent e) {
                                                    if (e.getStateChange() == ItemEvent.SELECTED) {
                                                        lbReceiver.setText((String) onlineUsers.getSelectedItem());
                                                        if (chatWindow != chatWindows.get(lbReceiver.getText())) {
                                                            txtMessage.setText("");
                                                            chatWindow = chatWindows.get(lbReceiver.getText());
                                                            chatPanel.setViewportView(chatWindow);
                                                            chatPanel.validate();
                                                        }

                                                        if (lbReceiver.getText().isBlank()) {
                                                            btnSend.setEnabled(false);
                                                            txtMessage.setEnabled(false);
                                                        } else {
                                                            btnSend.setEnabled(true);
                                                            txtMessage.setEnabled(true);
                                                        }
                                                    }

                                                }
                                            });
                                            btnFile.setBounds(500,470,90,30);
                                            btnFile.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    if(e.getSource()==btnFile)
                                                    {
                                                        JFileChooser fileChooser = new JFileChooser();
                                                        int rVal = fileChooser.showOpenDialog(contentPane.getParent());
                                                        if (rVal == JFileChooser.APPROVE_OPTION) {
                                                            byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
                                                            BufferedInputStream bis;
                                                            try {
                                                                bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                                                                bis.read(selectedFile, 0, selectedFile.length);

                                                                dos.writeUTF("File");
                                                                dos.writeUTF(lbReceiver.getText());
                                                                dos.writeUTF(fileChooser.getSelectedFile().getName());
                                                                dos.writeUTF(String.valueOf(selectedFile.length));

                                                                int size = selectedFile.length;
                                                                int bufferSize = 2048;
                                                                int offset = 0;

                                                                while (size > 0) {
                                                                    dos.write(selectedFile, offset, Math.min(size, bufferSize));
                                                                    offset += Math.min(size, bufferSize);
                                                                    size -= bufferSize;
                                                                }

                                                                dos.flush();

                                                                bis.close();

                                                                newFile(name, fileChooser.getSelectedFile().getName(), selectedFile, true);
                                                            } catch (IOException e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                            contentPane.add(btnFile);
                                            JLabel lbUsername = new JLabel("User:");
                                            lbUsername.setForeground(Color.RED);
                                            lbUsername.setBounds(0,30,80,40);
                                            JLabel tenuser=new JLabel(name);
                                            tenuser.setForeground(Color.RED);
                                            tenuser.setBounds(0,60,60,30);
                                            lbUsername.setFont(new Font("Arial", Font.BOLD, 15));
                                            tenuser.setFont(new Font("Arial", Font.BOLD, 15));
                                            tenuser.setBorder(BorderFactory.createLineBorder(Color.RED));
                                            leftPanel.add(lbUsername);
                                            leftPanel.add(tenuser);
                                            JLabel headerContent = new JLabel("Private Chat");
                                            headerContent.setForeground(Color.RED);
                                            headerContent.setFont(new Font("Time New Romans",Font.PLAIN,24));
                                            header.add(headerContent);
                                            header.setBackground(Color.CYAN);
                                            JPanel usernamePanel = new JPanel();
                                            usernamePanel.setForeground(Color.RED);
                                            usernamePanel.setBackground(Color.CYAN);
                                            chatPanel.setColumnHeaderView(usernamePanel);
                                            JLabel fourze=new JLabel(new ImageIcon("fourze.png"));
                                            fourze.setBounds(100,470,25,25);
                                            fourze.addMouseListener(new IconListener(new ImageIcon("fourze.png").toString()));
                                            contentPane.add(fourze);
                                            JLabel zeroone=new JLabel(new ImageIcon("zeroone.png"));
                                            zeroone.setBounds(130,470,25,25);
                                            zeroone.addMouseListener(new IconListener(new ImageIcon("zeroone.png").toString()));
                                            contentPane.add(zeroone);
                                            JLabel exaid=new JLabel(new ImageIcon("exaid.png"));
                                            exaid.setBounds(160,470,25,25);
                                            exaid.addMouseListener(new IconListener(new ImageIcon("exaid.png").toString()));
                                            contentPane.add(exaid);
                                            JLabel zio=new JLabel(new ImageIcon("zio.png"));
                                            zio.setBounds(190,470,25,25);
                                            zio.addMouseListener(new IconListener(new ImageIcon("zio.png").toString()));
                                            contentPane.add(zio);
                                            JLabel decade=new JLabel(new ImageIcon("decade.png"));
                                            decade.setBounds(220,470,25,25);
                                            decade.addMouseListener(new IconListener(new ImageIcon("decade.png").toString()));
                                            contentPane.add(decade);
                                            lbReceiver.setFont(new Font("Time New Romans",Font.PLAIN,16));
                                            usernamePanel.add(lbReceiver);
                                            lbReceiver.setForeground(Color.RED);
                                            chatWindows.put(" ", new JTextPane());
                                            chatWindow = chatWindows.get(" ");
                                            chatWindow.setFont(new Font("Time New Romans",Font.PLAIN,14));
                                            chatWindow.setEditable(false);
                                            chatPanel.setViewportView(chatWindow);
                                            JScrollPane textMessScroll=new JScrollPane(txtMessage);
                                            textMessScroll.setBounds(100,500,400,50);
                                            contentPane.add(textMessScroll);
                                            // Set action perform to send button.
                                            btnSend.addActionListener(new ActionListener() {
                                                public void actionPerformed(ActionEvent e) {

                                                    try {
                                                        dos.writeUTF("Text");
                                                        dos.writeUTF(lbReceiver.getText());
                                                        dos.writeUTF(txtMessage.getText());
                                                        dos.flush();
                                                    } catch (IOException e1) {
                                                        e1.printStackTrace();
                                                        newMessage("ERROR" , "Network error!" , true);
                                                    }

                                                    newMessage(name , txtMessage.getText() , true);
                                                    txtMessage.setText("");
                                                }
                                            });
                                            txtMessage.addKeyListener(new KeyAdapter() {
                                                @Override
                                                public void keyReleased(KeyEvent e) {
                                                    if (txtMessage.getText().isBlank() || lbReceiver.getText().isBlank()) {
                                                        btnSend.setEnabled(false);
                                                    } else {
                                                        btnSend.setEnabled(true);
                                                    }
                                                    if(e.getKeyCode()==KeyEvent.VK_ENTER)
                                                    {
                                                        Object[]options={"Xuống dòng","Gửi"};
                                                        int select=JOptionPane.showOptionDialog(null,"Hãy chọn thao tác","Select",JOptionPane.YES_NO_OPTION,
                                                                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
                                                        if(select==JOptionPane.YES_OPTION)
                                                        {
                                                            String message=txtMessage.getText();
                                                            txtMessage.setText(message);
                                                        }
                                                        else
                                                        {
                                                            String message=txtMessage.getText();
                                                            String newmess=message.substring(0,message.length()-2);
                                                            txtMessage.setText(newmess);
                                                            btnSend.doClick();
                                                        }
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                                privateChat.setBounds(80,100,200,100);
                                privateChat.setIcon(new ImageIcon("private.png"));
                                mainSl.add(privateChat);
                                JButton groupChat=new JButton("Group Chat");
                                groupChat.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if(e.getSource()==groupChat)
                                        {
                                            JFrame frame1=new JFrame();
                                            frame1.setSize(300,300);
                                            frame1.setVisible(true);
                                            frame1.setLayout(null);
                                            frame1.setTitle("Select");
                                            JButton create=new JButton("Tạo nhóm");
                                            create.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    if(e.getSource()==create)
                                                    {
                                                        frame1.dispose();
                                                        JFrame frame2=new JFrame();
                                                        frame2.setSize(300,300);
                                                        frame2.setVisible(true);
                                                        frame2.setLayout(null);
                                                        frame2.setTitle("Create");
                                                        JLabel label=new JLabel("Nhập tên phòng");
                                                        label.setBounds(90,50,100,50);
                                                        JTextField id=new JTextField();
                                                        id.setBounds(90,100,100,30);
                                                        JButton button1=new JButton("Tạo phòng");
                                                        button1.addActionListener(new ActionListener() {
                                                            @Override
                                                            public void actionPerformed(ActionEvent e) {
                                                                if(e.getSource()==button1)
                                                                {
                                                                    String res=CreateRoom(user,id.getText());
                                                                    System.out.println(res);
                                                                    if(res.equals("Exist"))
                                                                    {
                                                                        JOptionPane.showMessageDialog(null,"Phòng đã tồn tại");
                                                                    }
                                                                    else {
                                                                        if(res.equals("Successful"))
                                                                        {
                                                                            phong=id.getText();
                                                                            chatWindows.put(phong,new JTextPane());
                                                                            frame2.dispose();
                                                                            EventQueue.invokeLater(new Runnable() {
                                                                                public void run() {
                                                                                    JFrame frame3=new JFrame();
                                                                                    frame3.setVisible(true);
                                                                                    frame3.setResizable(false);
                                                                                    frame3.setSize(600,600);
                                                                                    frame3.setTitle("Group Chat");
                                                                                    receive = new Thread(new Receiver(dis));
                                                                                    receive.start();
                                                                                    contentPane = new JPanel();
                                                                                    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                                                                                    frame3.setContentPane(contentPane);
                                                                                    JPanel header = new JPanel();
                                                                                    txtMessage = new JTextPane();
                                                                                    btnSend = new JButton("Gửi");
                                                                                    btnSend.setBounds(500,500,90,50);
                                                                                    chatPanel = new JScrollPane();
                                                                                    chatPanel.setBounds(100,70,490,400);
                                                                                    chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                                                                                    contentPane.setLayout(null);
                                                                                    JPanel leftPanel = new JPanel();
                                                                                    leftPanel.setBackground(Color.CYAN);
                                                                                    contentPane.add(header);
                                                                                    header.setBounds(0,0,600,70);
                                                                                    contentPane.add(leftPanel);
                                                                                    leftPanel.setBounds(0,70,100,530);
                                                                                    txtMessage.setBounds(100,500,400,50);
                                                                                    contentPane.add(btnSend);
                                                                                    contentPane.add(chatPanel);
                                                                                    chatPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                    JLabel lblNewLabel_1 = new JLabel("User Online");
                                                                                    lblNewLabel_1.setForeground(Color.RED);
                                                                                    lblNewLabel_1.setBounds(20,120,90,30);
                                                                                    lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
                                                                                    leftPanel.setLayout(null);
                                                                                    leftPanel.add(lblNewLabel_1);
                                                                                    leftPanel.add(onlineUsers);
                                                                                    leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                    onlineUsers.setBounds(10,150,90,30);
                                                                                    lbRoom.setText(phong);
                                                                                    btnFile.setBounds(500,470,90,30);
                                                                                    btnFile.addActionListener(new ActionListener() {
                                                                                        @Override
                                                                                        public void actionPerformed(ActionEvent e) {
                                                                                            if(e.getSource()==btnFile)
                                                                                            {
                                                                                                JFileChooser fileChooser = new JFileChooser();
                                                                                                int rVal = fileChooser.showOpenDialog(contentPane.getParent());
                                                                                                if (rVal == JFileChooser.APPROVE_OPTION) {
                                                                                                    byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
                                                                                                    BufferedInputStream bis;
                                                                                                    try {
                                                                                                        bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                                                                                                        bis.read(selectedFile, 0, selectedFile.length);

                                                                                                        dos.writeUTF("File Group");
                                                                                                        dos.writeUTF(user);
                                                                                                        dos.writeUTF(lbRoom.getText());
                                                                                                        dos.writeUTF(fileChooser.getSelectedFile().getName());
                                                                                                        dos.writeUTF(String.valueOf(selectedFile.length));

                                                                                                        int size = selectedFile.length;
                                                                                                        int bufferSize = 2048;
                                                                                                        int offset = 0;

                                                                                                        while (size > 0) {
                                                                                                            dos.write(selectedFile, offset, Math.min(size, bufferSize));
                                                                                                            offset += Math.min(size, bufferSize);
                                                                                                            size -= bufferSize;
                                                                                                        }

                                                                                                        dos.flush();

                                                                                                        bis.close();

                                                                                                    } catch (IOException e1) {
                                                                                                        e1.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                    contentPane.add(btnFile);
                                                                                    JLabel lbUsername = new JLabel("User:");
                                                                                    lbUsername.setForeground(Color.RED);
                                                                                    lbUsername.setBounds(0,30,80,40);
                                                                                    JLabel tenuser=new JLabel(name);
                                                                                    tenuser.setForeground(Color.RED);
                                                                                    tenuser.setBounds(0,60,60,30);
                                                                                    lbUsername.setFont(new Font("Arial", Font.BOLD, 15));
                                                                                    tenuser.setFont(new Font("Arial", Font.BOLD, 15));
                                                                                    tenuser.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                    leftPanel.add(lbUsername);
                                                                                    leftPanel.add(tenuser);
                                                                                    JLabel headerContent = new JLabel("Group Chat");
                                                                                    headerContent.setForeground(Color.RED);
                                                                                    headerContent.setFont(new Font("Time New Romans",Font.PLAIN,24));
                                                                                    header.add(headerContent);
                                                                                    header.setBackground(Color.CYAN);
                                                                                    JPanel usernamePanel = new JPanel();
                                                                                    usernamePanel.setForeground(Color.RED);
                                                                                    usernamePanel.setBackground(Color.CYAN);
                                                                                    chatPanel.setColumnHeaderView(usernamePanel);
                                                                                    JLabel fourze=new JLabel(new ImageIcon("fourze.png"));
                                                                                    fourze.setBounds(100,470,25,25);
                                                                                    fourze.addMouseListener(new IconListener2(new ImageIcon("fourze.png").toString()));
                                                                                    contentPane.add(fourze);
                                                                                    JLabel zeroone=new JLabel(new ImageIcon("zeroone.png"));
                                                                                    zeroone.setBounds(130,470,25,25);
                                                                                    zeroone.addMouseListener(new IconListener2(new ImageIcon("zeroone.png").toString()));
                                                                                    contentPane.add(zeroone);
                                                                                    JLabel exaid=new JLabel(new ImageIcon("exaid.png"));
                                                                                    exaid.setBounds(160,470,25,25);
                                                                                    exaid.addMouseListener(new IconListener2(new ImageIcon("exaid.png").toString()));
                                                                                    contentPane.add(exaid);
                                                                                    JLabel zio=new JLabel(new ImageIcon("zio.png"));
                                                                                    zio.setBounds(190,470,25,25);
                                                                                    zio.addMouseListener(new IconListener2(new ImageIcon("zio.png").toString()));
                                                                                    contentPane.add(zio);
                                                                                    JLabel decade=new JLabel(new ImageIcon("decade.png"));
                                                                                    decade.setBounds(220,470,25,25);
                                                                                    decade.addMouseListener(new IconListener2(new ImageIcon("decade.png").toString()));
                                                                                    contentPane.add(decade);
                                                                                    lbRoom.setFont(new Font("Time New Romans",Font.PLAIN,16));
                                                                                    usernamePanel.add(lbRoom);
                                                                                    lbRoom.setForeground(Color.RED);
                                                                                    chatWindow = chatWindows.get(phong);
                                                                                    chatWindow.setFont(new Font("Time New Romans",Font.PLAIN,14));
                                                                                    chatWindow.setEditable(false);
                                                                                    chatPanel.setViewportView(chatWindow);
                                                                                    JScrollPane textMessScroll=new JScrollPane(txtMessage);
                                                                                    textMessScroll.setBounds(100,500,400,50);
                                                                                    contentPane.add(textMessScroll);
                                                                                    btnSend.addActionListener(new ActionListener() {
                                                                                        public void actionPerformed(ActionEvent e) {

                                                                                            try {
                                                                                                dos.writeUTF("Text Group");
                                                                                                dos.writeUTF(user);
                                                                                                dos.writeUTF(lbRoom.getText());
                                                                                                dos.writeUTF(txtMessage.getText());
                                                                                                dos.flush();
                                                                                            } catch (IOException e1) {
                                                                                                e1.printStackTrace();
                                                                                                newMessage2("ERROR" , "Network error!" , true);
                                                                                            }

                                                                                            txtMessage.setText("");
                                                                                        }
                                                                                    });
                                                                                    txtMessage.addKeyListener(new KeyAdapter() {
                                                                                        @Override
                                                                                        public void keyReleased(KeyEvent e) {
                                                                                            if (txtMessage.getText().isBlank()) {
                                                                                                btnSend.setEnabled(false);
                                                                                            } else {
                                                                                                btnSend.setEnabled(true);
                                                                                            }
                                                                                            if(e.getKeyCode()==KeyEvent.VK_ENTER)
                                                                                            {
                                                                                                Object[]options={"Xuống dòng","Gửi"};
                                                                                                int select=JOptionPane.showOptionDialog(null,"Hãy chọn thao tác","Select",JOptionPane.YES_NO_OPTION,
                                                                                                        JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
                                                                                                if(select==JOptionPane.YES_OPTION)
                                                                                                {
                                                                                                    String message=txtMessage.getText();
                                                                                                    txtMessage.setText(message);
                                                                                                }
                                                                                                else
                                                                                                {
                                                                                                    String message=txtMessage.getText();
                                                                                                    String newmess=message.substring(0,message.length()-2);
                                                                                                    txtMessage.setText(newmess);
                                                                                                    btnSend.doClick();
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    });

                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        button1.setBounds(90,150,100,50);
                                                        frame2.add(label);
                                                        frame2.add(id);
                                                        frame2.add(button1);
                                                    }
                                                }
                                            });
                                            create.setBounds(90,50,100,50);
                                            JButton join=new JButton("Tham gia");
                                            join.addActionListener(new ActionListener() {
                                                @Override
                                                public void actionPerformed(ActionEvent e) {
                                                    if(e.getSource()==join)
                                                    {
                                                        frame1.dispose();
                                                        JFrame frame4=new JFrame();
                                                        frame4.setSize(300,300);
                                                        frame4.setVisible(true);
                                                        frame4.setLayout(null);
                                                        frame4.setTitle("Join");
                                                        JLabel label=new JLabel("Nhập tên phòng");
                                                        label.setBounds(90,50,100,50);
                                                        JTextField idroom=new JTextField();
                                                        idroom.setBounds(90,100,100,30);
                                                        JButton button1=new JButton("Vào phòng");
                                                        button1.addActionListener(new ActionListener() {
                                                            @Override
                                                            public void actionPerformed(ActionEvent e) {
                                                                if(e.getSource()==button1)
                                                                {
                                                                    String rep=Join(user,idroom.getText());
                                                                    if(rep.equals("Successful"))
                                                                    {
                                                                        phong=idroom.getText();
                                                                        frame4.dispose();
                                                                        EventQueue.invokeLater(new Runnable() {
                                                                            public void run() {
                                                                                JFrame frame3=new JFrame();
                                                                                frame3.setVisible(true);
                                                                                frame3.setResizable(false);
                                                                                frame3.setSize(600,600);
                                                                                frame3.setTitle("Group Chat");
                                                                                receive = new Thread(new Receiver(dis));
                                                                                receive.start();
                                                                                contentPane = new JPanel();
                                                                                contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
                                                                                frame3.setContentPane(contentPane);
                                                                                JPanel header = new JPanel();
                                                                                txtMessage = new JTextPane();
                                                                                btnSend = new JButton("Gửi");
                                                                                btnSend.setBounds(500,500,90,50);
                                                                                chatPanel = new JScrollPane();
                                                                                chatPanel.setBounds(100,70,490,400);
                                                                                chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                                                                                contentPane.setLayout(null);
                                                                                JPanel leftPanel = new JPanel();
                                                                                leftPanel.setBackground(Color.CYAN);
                                                                                contentPane.add(header);
                                                                                header.setBounds(0,0,600,70);
                                                                                contentPane.add(leftPanel);
                                                                                leftPanel.setBounds(0,70,100,530);
                                                                                txtMessage.setBounds(100,500,400,50);
                                                                                contentPane.add(btnSend);
                                                                                contentPane.add(chatPanel);
                                                                                chatPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                JLabel lblNewLabel_1 = new JLabel("User Online");
                                                                                lblNewLabel_1.setForeground(Color.RED);
                                                                                lblNewLabel_1.setBounds(20,120,90,30);
                                                                                lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
                                                                                leftPanel.setLayout(null);
                                                                                leftPanel.add(lblNewLabel_1);
                                                                                leftPanel.add(onlineUsers);
                                                                                leftPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                onlineUsers.setBounds(10,150,90,30);
                                                                                lbRoom.setText(phong);
                                                                                btnFile.setBounds(500,470,90,30);
                                                                                btnFile.addActionListener(new ActionListener() {
                                                                                    @Override
                                                                                    public void actionPerformed(ActionEvent e) {
                                                                                        if(e.getSource()==btnFile)
                                                                                        {
                                                                                            JFileChooser fileChooser = new JFileChooser();
                                                                                            int rVal = fileChooser.showOpenDialog(contentPane.getParent());
                                                                                            if (rVal == JFileChooser.APPROVE_OPTION) {
                                                                                                byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
                                                                                                BufferedInputStream bis;
                                                                                                try {
                                                                                                    bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                                                                                                    bis.read(selectedFile, 0, selectedFile.length);

                                                                                                    dos.writeUTF("File Group");
                                                                                                    dos.writeUTF(user);
                                                                                                    dos.writeUTF(lbRoom.getText());
                                                                                                    dos.writeUTF(fileChooser.getSelectedFile().getName());
                                                                                                    dos.writeUTF(String.valueOf(selectedFile.length));

                                                                                                    int size = selectedFile.length;
                                                                                                    int bufferSize = 2048;
                                                                                                    int offset = 0;

                                                                                                    while (size > 0) {
                                                                                                        dos.write(selectedFile, offset, Math.min(size, bufferSize));
                                                                                                        offset += Math.min(size, bufferSize);
                                                                                                        size -= bufferSize;
                                                                                                    }

                                                                                                    dos.flush();

                                                                                                    bis.close();

                                                                                                } catch (IOException e1) {
                                                                                                    e1.printStackTrace();
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                });
                                                                                contentPane.add(btnFile);
                                                                                JLabel lbUsername = new JLabel("User:");
                                                                                lbUsername.setForeground(Color.RED);
                                                                                lbUsername.setBounds(0,30,80,40);
                                                                                JLabel tenuser=new JLabel(name);
                                                                                tenuser.setForeground(Color.RED);
                                                                                tenuser.setBounds(0,60,60,30);
                                                                                lbUsername.setFont(new Font("Arial", Font.BOLD, 15));
                                                                                tenuser.setFont(new Font("Arial", Font.BOLD, 15));
                                                                                tenuser.setBorder(BorderFactory.createLineBorder(Color.RED));
                                                                                leftPanel.add(lbUsername);
                                                                                leftPanel.add(tenuser);
                                                                                JLabel headerContent = new JLabel("Group Chat");
                                                                                headerContent.setForeground(Color.RED);
                                                                                headerContent.setFont(new Font("Time New Romans",Font.PLAIN,24));
                                                                                header.add(headerContent);
                                                                                header.setBackground(Color.CYAN);
                                                                                JPanel usernamePanel = new JPanel();
                                                                                usernamePanel.setForeground(Color.RED);
                                                                                usernamePanel.setBackground(Color.CYAN);
                                                                                chatPanel.setColumnHeaderView(usernamePanel);
                                                                                JLabel fourze=new JLabel(new ImageIcon("fourze.png"));
                                                                                fourze.setBounds(100,470,25,25);
                                                                                fourze.addMouseListener(new IconListener2(new ImageIcon("fourze.png").toString()));
                                                                                contentPane.add(fourze);
                                                                                JLabel zeroone=new JLabel(new ImageIcon("zeroone.png"));
                                                                                zeroone.setBounds(130,470,25,25);
                                                                                zeroone.addMouseListener(new IconListener2(new ImageIcon("zeroone.png").toString()));
                                                                                contentPane.add(zeroone);
                                                                                JLabel exaid=new JLabel(new ImageIcon("exaid.png"));
                                                                                exaid.setBounds(160,470,25,25);
                                                                                exaid.addMouseListener(new IconListener2(new ImageIcon("exaid.png").toString()));
                                                                                contentPane.add(exaid);
                                                                                JLabel zio=new JLabel(new ImageIcon("zio.png"));
                                                                                zio.setBounds(190,470,25,25);
                                                                                zio.addMouseListener(new IconListener2(new ImageIcon("zio.png").toString()));
                                                                                contentPane.add(zio);
                                                                                JLabel decade=new JLabel(new ImageIcon("decade.png"));
                                                                                decade.setBounds(220,470,25,25);
                                                                                decade.addMouseListener(new IconListener2(new ImageIcon("decade.png").toString()));
                                                                                contentPane.add(decade);
                                                                                lbRoom.setFont(new Font("Time New Romans",Font.PLAIN,16));
                                                                                usernamePanel.add(lbRoom);
                                                                                lbRoom.setForeground(Color.RED);
                                                                                if(!chatWindows.containsKey(phong))  chatWindows.put(phong, new JTextPane());
                                                                                chatWindow = chatWindows.get(phong);
                                                                                chatWindow.setFont(new Font("Time New Romans",Font.PLAIN,14));
                                                                                chatWindow.setEditable(false);
                                                                                chatPanel.setViewportView(chatWindow);
                                                                                JScrollPane textMessScroll=new JScrollPane(txtMessage);
                                                                                textMessScroll.setBounds(100,500,400,50);
                                                                                contentPane.add(textMessScroll);
                                                                                btnSend.addActionListener(new ActionListener() {
                                                                                    public void actionPerformed(ActionEvent e) {

                                                                                        try {
                                                                                            dos.writeUTF("Text Group");
                                                                                            dos.writeUTF(user);
                                                                                            dos.writeUTF(lbRoom.getText());
                                                                                            dos.writeUTF(txtMessage.getText());
                                                                                            dos.flush();
                                                                                        } catch (IOException e1) {
                                                                                            e1.printStackTrace();
                                                                                            newMessage2("ERROR" , "Network error!" , true);
                                                                                        }

                                                                                        txtMessage.setText("");
                                                                                    }
                                                                                });
                                                                                txtMessage.addKeyListener(new KeyAdapter() {
                                                                                    @Override
                                                                                    public void keyReleased(KeyEvent e) {
                                                                                        if (txtMessage.getText().isBlank()) {
                                                                                            btnSend.setEnabled(false);
                                                                                        } else {
                                                                                            btnSend.setEnabled(true);
                                                                                        }
                                                                                        if(e.getKeyCode()==KeyEvent.VK_ENTER)
                                                                                        {
                                                                                            Object[]options={"Xuống dòng","Gửi"};
                                                                                            int select=JOptionPane.showOptionDialog(null,"Hãy chọn thao tác","Select",JOptionPane.YES_NO_OPTION,
                                                                                                    JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
                                                                                            if(select==JOptionPane.YES_OPTION)
                                                                                            {
                                                                                                String message=txtMessage.getText();
                                                                                                txtMessage.setText(message);
                                                                                            }
                                                                                            else
                                                                                            {
                                                                                                String message=txtMessage.getText();
                                                                                                String newmess=message.substring(0,message.length()-2);
                                                                                                txtMessage.setText(newmess);
                                                                                                btnSend.doClick();
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        });
                                                                    }
                                                                    else
                                                                    {
                                                                        JOptionPane.showMessageDialog(null,"Phòng không tồn tại!");
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        button1.setBounds(90,150,100,50);
                                                        frame4.add(label);
                                                        frame4.add(idroom);
                                                        frame4.add(button1);
                                                    }
                                                }
                                            });
                                            join.setBounds(90,150,100,50);
                                            frame1.add(join);
                                            frame1.add(create);
                                        }
                                    }
                                });
                                groupChat.setBounds(300,100,200,100);
                                groupChat.setIcon(new ImageIcon("group.png"));
                                mainSl.add(groupChat);
                                JButton voiceChat=new JButton("Voice Chat");
                                voiceChat.setBounds(80,300,200,100);
                                voiceChat.setIcon(new ImageIcon("voice.png"));
                                mainSl.add(voiceChat);
                                JButton videoChat=new JButton("Video Chat");
                                videoChat.setBounds(300,300,200,100);
                                videoChat.setIcon(new ImageIcon("video.png"));
                                mainSl.add(videoChat);
                                selectFrame.add(mainSl);
                            }
                        });
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null,"Sai tài khoản hoặc mật khẩu!");
                    }
                }
            }
        });
        button.setSize(new Dimension(120,30));
        button.setLocation(190,250);
        button.setVerticalAlignment(0);
        JButton button1=new JButton("Register");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource()==button1)
                {
                    JFrame regisFrame=new JFrame();
                    regisFrame.setTitle("Register");
                    regisFrame.setSize(300,200);
                    regisFrame.setVisible(true);
                    regisFrame.setResizable(false);
                    regisFrame.setIconImage(icon.getImage());
                    JPanel panel=new JPanel();
                    regisFrame.add(panel);
                    panel.setLayout(null);
                    JLabel usertext=new JLabel("Username:");
                    usertext.setBounds(0,20,70,25);
                    panel.add(usertext);
                    JLabel passtext=new JLabel("Password:");
                    passtext.setBounds(0,50,70,25);
                    panel.add(passtext);
                    JTextField field=new JTextField();
                    field.setBounds(75,20,200,25);
                    JTextField field1=new JTextField();
                    field1.setBounds(75,50,200,25);
                    panel.add(field);
                    panel.add(field1);
                    JButton jButton=new JButton("Register");
                    jButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if(e.getSource()==jButton)
                            {
                                String response=Signup(field.getText(),field1.getText());
                                if ( response.equals("Sign up successful") ) {
                                    EventQueue.invokeLater(new Runnable() {
                                        public void run() {
                                            try {
                                                JOptionPane.showMessageDialog(null, "Sign up successful");

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    dispose();
                                } else {
                                    JOptionPane.showMessageDialog(null,"Đăng ký thất bại");
                                }
                            }
                        }
                    });
                    jButton.setSize(150,30);
                    jButton.setLocation(60,100);
                    panel.add(jButton);
                }
            }
        });
        button1.setSize(new Dimension(120,30));
        button1.setLocation(330,250);
        JLabel label=new JLabel("LOGIN FORM");
        label.setForeground(Color.GREEN);
        label.setBounds(280,20,100,80);
        background.add(label);
        background.add(button);
        background.add(button1);
        background.add(username);
        background.add(password);
        frame.pack();
    }
    private void newFile(String username, String filename, byte[] file, Boolean yourMessage) {

        StyledDocument doc;
        String window = null;
        if (username.equals(this.name)) {
            window = lbReceiver.getText();
        } else {
            window = username;
        }
        doc = chatWindows.get(window).getStyledDocument();

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style linkStyle = doc.getStyle("Link style");
        if (linkStyle == null) {
            linkStyle = doc.addStyle("Link style", null);
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link", new HyberlinkListener(filename, file));
        }

        if (chatWindows.get(window).getMouseListeners() != null) {
            // Tạo MouseListener cho các đường dẫn tải về file
            chatWindows.get(window).addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e)
                {
                    Element ele = doc.getCharacterElement(chatWindow.viewToModel(e.getPoint()));
                    AttributeSet as = ele.getAttributes();
                    HyberlinkListener listener = (HyberlinkListener)as.getAttribute("link");
                    if(listener != null)
                    {
                        listener.execute();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

            });
        }

        // In ra đường dẫn tải file
        try {
            doc.insertString(doc.getLength(),"<" + filename + ">", linkStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

    }
    private void newFile2(String username, String filename, byte[] file, Boolean yourMessage) {

        StyledDocument doc;
        String window = null;
            window = lbRoom.getText();
        doc = chatWindows.get(window).getStyledDocument();

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style linkStyle = doc.getStyle("Link style");
        if (linkStyle == null) {
            linkStyle = doc.addStyle("Link style", null);
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setUnderline(linkStyle, true);
            StyleConstants.setBold(linkStyle, true);
            linkStyle.addAttribute("link", new HyberlinkListener(filename, file));
        }

        if (chatWindows.get(window).getMouseListeners() != null) {
            // Tạo MouseListener cho các đường dẫn tải về file
            chatWindows.get(window).addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e)
                {
                    Element ele = doc.getCharacterElement(chatWindow.viewToModel(e.getPoint()));
                    AttributeSet as = ele.getAttributes();
                    HyberlinkListener listener = (HyberlinkListener)as.getAttribute("link");
                    if(listener != null)
                    {
                        listener.execute();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // TODO Auto-generated method stub

                }

            });
        }

        // In ra đường dẫn tải file
        try {
            doc.insertString(doc.getLength(),"<" + filename + ">", linkStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

        // Xuống dòng
        try {
            doc.insertString(doc.getLength(), "\n", userStyle);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }

    }
    private static void newMessage(String username, String message, Boolean yourMessage) {

        StyledDocument doc;
        if (username.equals(Client.name)) {
            doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
        } else {
            doc = chatWindows.get(username).getStyledDocument();
        }

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra tên người gửi
        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style messageStyle = doc.getStyle("Message style");
        if (messageStyle == null) {
            messageStyle = doc.addStyle("Message style", null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }

        // In ra nội dung tin nhắn
        try { doc.insertString(doc.getLength(), message + "\n",messageStyle); }
        catch (BadLocationException e){}

    }
    private static void newMessage2(String username, String message, Boolean yourMessage) {

        StyledDocument doc;
            doc = chatWindows.get(lbRoom.getText()).getStyledDocument();

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra tên người gửi
        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style messageStyle = doc.getStyle("Message style");
        if (messageStyle == null) {
            messageStyle = doc.addStyle("Message style", null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }

        // In ra nội dung tin nhắn
        try { doc.insertString(doc.getLength(), message + "\n",messageStyle); }
        catch (BadLocationException e){}

    }
    class Receiver implements Runnable{

        private DataInputStream dis;

        public Receiver(DataInputStream dis) {
            this.dis = dis;
        }

        @Override
        public void run() {
            try {

                while (true) {
                    // Chờ tin nhắn từ server
                    String method = dis.readUTF();

                    if (method.equals("Text")) {
                        // Nhận một tin nhắn văn bản
                        String sender =	dis.readUTF();
                        String message = dis.readUTF();

                        // In tin nhắn lên màn hình chat với người gửi
                        newMessage(sender, message, false);
                    }
                    else if (method.equals("Text Group")) {
                        // Nhận một tin nhắn văn bản
                        String sender =	dis.readUTF();
                        String r=dis.readUTF();
                        String message = dis.readUTF();
                        // In tin nhắn lên màn hình chat với người gửi
                        newMessage2(sender, message, false);
                    }
                    else if (method.equals("Emoji")) {
                        // Nhận một tin nhắn Emoji
                        String sender = dis.readUTF();
                        String emoji = dis.readUTF();

                        // In tin nhắn lên màn hình chat với người gửi
                        newEmoji(sender, emoji, false);
                    }
                    else if (method.equals("Emoji Group")) {
                        // Nhận một tin nhắn Emoji
                        String sender = dis.readUTF();
                        String emoji = dis.readUTF();

                        // In tin nhắn lên màn hình chat với người gửi
                        newEmoji2(sender, emoji, false);
                    }

                    else if (method.equals("File")) {
                        // Nhận một file
                        String sender = dis.readUTF();
                        String filename = dis.readUTF();
                        int size = Integer.parseInt(dis.readUTF());
                        int bufferSize = 2048;
                        byte[] buffer = new byte[bufferSize];
                        ByteArrayOutputStream file = new ByteArrayOutputStream();

                        while (size > 0) {
                            dis.read(buffer, 0, Math.min(bufferSize, size));
                            file.write(buffer, 0, Math.min(bufferSize, size));
                            size -= bufferSize;
                        }

                        // In ra màn hình file đó
                        newFile(sender, filename, file.toByteArray(), false);

                    }

                    else if (method.equals("File Group")) {
                        // Nhận một file
                        String sender=dis.readUTF();
                        String r = dis.readUTF();
                        String filename = dis.readUTF();
                        int size = Integer.parseInt(dis.readUTF());
                        int bufferSize = 2048;
                        byte[] buffer = new byte[bufferSize];
                        ByteArrayOutputStream file = new ByteArrayOutputStream();

                        while (size > 0) {
                            dis.read(buffer, 0, Math.min(bufferSize, size));
                            file.write(buffer, 0, Math.min(bufferSize, size));
                            size -= bufferSize;
                        }

                        // In ra màn hình file đó
                        newFile2(sender, filename, file.toByteArray(), false);

                    }
                    else if (method.equals("Online users")) {
                        // Nhận yêu cầu cập nhật danh sách người dùng trực tuyến
                        String[] users = dis.readUTF().split(",");
                        onlineUsers.removeAllItems();

                        String chatting = lbReceiver.getText();

                        boolean isChattingOnline = false;

                        for (String user: users) {
                            if (user.equals(name) == false) {
                                // Cập nhật danh sách các người dùng trực tuyến vào ComboBox onlineUsers (trừ bản thân)
                                onlineUsers.addItem(user);
                                if (chatWindows.get(user) == null) {
                                    JTextPane temp = new JTextPane();
                                    temp.setFont(new Font("Arial", Font.PLAIN, 14));
                                    temp.setEditable(false);
                                    chatWindows.put(user, temp);
                                }
                            }
                            if (chatting.equals(user)) {
                                isChattingOnline = true;
                            }
                        }

                        if (isChattingOnline == false) {
                            // Nếu người đang chat không online thì chuyển hướng về màn hình mặc định và thông báo cho người dùng
                            onlineUsers.setSelectedItem(" ");
                            JOptionPane.showMessageDialog(null, chatting + " is offline!\nYou will be redirect to default chat window");
                        } else {
                            onlineUsers.setSelectedItem(chatting);
                        }

                        onlineUsers.validate();
                    }

                    else if (method.equals("Safe to leave")) {
                        // Thông báo có thể thoát
                        break;
                    }

                }

            } catch(IOException ex) {
                System.err.println(ex);
            } finally {
                try {
                    if (dis != null) {
                        dis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void Connect() {
        try {
            if (socket != null) {
                socket.close();
            }
            socket = new Socket(host, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static String Login(String username, String password) {
        try {
            Connect();

            dos.writeUTF("Log in");
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Log in fail";
        }
    }
    public String Signup(String username, String password) {
        try {
            Connect();

            dos.writeUTF("Sign up");
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Sign up fail";
        }
    }
    static String CreateRoom(String user,String room) {
        try {
            dos.writeUTF("Create room");
            dos.writeUTF(user);
            dos.writeUTF(room);
            dos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Join fail";
        }
    }
    static String Join(String user,String room) {
        try {
            dos.writeUTF("Join room");
            dos.writeUTF(user);
            dos.writeUTF(room);
            dos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Join fail";
        }
    }
    static void privateChat() throws IOException {
            dos.writeUTF("Private chat");
            dos.flush();
    }
    @Override
    public void run() {

    }
    private void newEmoji(String username, String emoji, Boolean yourMessage) {

        StyledDocument doc;
        if (username.equals(name)) {
            doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
        } else {
            doc = chatWindows.get(username).getStyledDocument();
        }

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra màn hình tên người gửi
        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style iconStyle = doc.getStyle("Icon style");
        if (iconStyle == null) {
            iconStyle = doc.addStyle("Icon style", null);
        }

        StyleConstants.setIcon(iconStyle, new ImageIcon(emoji));

        // In ra màn hình Emoji
        try { doc.insertString(doc.getLength(), "invisible text", iconStyle); }
        catch (BadLocationException e){}

        // Xuống dòng
        try { doc.insertString(doc.getLength(), "\n", userStyle); }
        catch (BadLocationException e){}

    }
    private void newEmoji2(String username, String emoji, Boolean yourMessage) {

        StyledDocument doc;

            doc = chatWindows.get(lbRoom.getText()).getStyledDocument();

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage == true) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra màn hình tên người gửi
        try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}

        Style iconStyle = doc.getStyle("Icon style");
        if (iconStyle == null) {
            iconStyle = doc.addStyle("Icon style", null);
        }

        StyleConstants.setIcon(iconStyle, new ImageIcon(emoji));

        // In ra màn hình Emoji
        try { doc.insertString(doc.getLength(), "invisible text", iconStyle); }
        catch (BadLocationException e){}

        // Xuống dòng
        try { doc.insertString(doc.getLength(), "\n", userStyle); }
        catch (BadLocationException e){}

    }
    class IconListener extends MouseAdapter {
        String emoji;

        public IconListener(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (txtMessage.isEnabled() == true) {

                try {
                    dos.writeUTF("Emoji");
                    dos.writeUTF(lbReceiver.getText());
                    dos.writeUTF(this.emoji);
                    dos.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    newMessage("ERROR" , "Network error!" , true);
                }

                // In Emoji lên màn hình chat
                newEmoji(name, this.emoji, true);
            }
        }
    }
    class IconListener2 extends MouseAdapter {
        String emoji;

        public IconListener2(String emoji) {
            this.emoji = emoji;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (txtMessage.isEnabled() == true) {

                try {
                    dos.writeUTF("Emoji Group");
                    dos.writeUTF(lbRoom.getText());
                    dos.writeUTF(this.emoji);
                    dos.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    newMessage("ERROR" , "Network error!" , true);
                }
            }
        }
    }
    class HyberlinkListener extends AbstractAction {
        String filename;
        byte[] file;

        public HyberlinkListener(String filename, byte[] file) {
            this.filename = filename;
            this.file = Arrays.copyOf(file, file.length);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            execute();
        }

        public  void execute() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(filename));
            int rVal = fileChooser.showSaveDialog(contentPane.getParent());
            if (rVal == JFileChooser.APPROVE_OPTION) {

                // Mở file đã chọn sau đó lưu thông tin xuống file đó
                File saveFile = fileChooser.getSelectedFile();
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (bos != null) {
                    try {
                        bos.write(this.file);
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}



