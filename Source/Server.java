import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server extends JFrame implements Runnable{
    private Object lock;
    static Vector<Handler> listClients = new Vector<>();
    static Vector<String>User=new Vector<>();
    static Vector<String>Room=new Vector<>();
    static JTextArea textArea;
    JTabbedPane tabbedPane;
    int PORT=999;
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    ServerSocket serverSocket=new ServerSocket(PORT);
    static Thread t;
    BufferedReader br = null;
    Server thisServer;
    Socket socket;
    public Server() throws IOException {
        JFrame mainFrame=new JFrame();
        mainFrame.setTitle("Server");
        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        mainFrame.setResizable(false);
        JPanel panel = new JPanel();
        mainFrame.add(panel);
        panel.setLayout(new BorderLayout());
        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");
        JLabel label = new JLabel("Chat Server");
        label.setHorizontalAlignment(0);
        textArea = new JTextArea(20, 50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.green);
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());
        southPanel.add(start);
        southPanel.add(stop);
        panel.add(southPanel, BorderLayout.SOUTH);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(panel);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == start) {
                    textArea.append("|" + simpleDateFormat.format(new Date()) + "|" + " Server is running at port:"+serverSocket.getLocalPort()+"\n");
                    textArea.append("|" + simpleDateFormat.format(new Date()) + "|" + " Waiting for client!\n");
                    t=new Thread(Server.this);
                    t.start();
                }
            }
        });
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == stop) {
                    int KQ = JOptionPane.showConfirmDialog(null, "B???n c?? ch???c ch???n mu???n ????ng server kh??ng?", "Notice", JOptionPane.YES_NO_OPTION);
                    if (KQ == JOptionPane.YES_OPTION) {
                        t.stop();
                        textArea.append("|" + simpleDateFormat.format(new Date()) + "|" + " Server stopped!\n");
                    }
                }
            }
        });
        mainFrame.pack();
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    public static void main(String arg[]) throws IOException {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Server server=new Server();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
            }
            return port;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }
    private void saveAccounts() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File("Account.txt"), "utf8");
        } catch (Exception ex ) {
            System.out.println(ex.getMessage());
        }
        for (Handler client : listClients) {
            pw.print(client.getUsername() + "," + client.getPassword() + "\n");
        }
        pw.println("");
        if (pw != null) {
            pw.close();
        }
    }
    private void loadAccounts() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("Account.txt"), "utf8"));

            String info = br.readLine();
            while (info != null && !(info.isEmpty())) {
                listClients.add(new Handler(info.split(",")[0], info.split(",")[1], false, lock));
                info = br.readLine();
            }

            br.close();
            for(int i=0;i<listClients.size();i++)
            {
                User.add(listClients.get(i).username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isExisted(String name) {
        for (Handler client:listClients) {
            if (client.getUsername().equals(name)) {
                return true;
            }
        }
        return false;
    }
    public static void updateOnlineUsers() {
        String message = " ";
        for (Handler client:listClients) {
            if (client.getIsLoggedIn() == true) {
                message += ",";
                message += client.getUsername();
            }
        }
        for (Handler client:listClients) {
            if (client.getIsLoggedIn() == true) {
                try {
                    client.getDos().writeUTF("Online users");
                    client.getDos().writeUTF(message);
                    client.getDos().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Vector<Handler> getListClients() {
        return listClients;
    }

    public void setListClients(Vector<Handler> listClients) {
        this.listClients = listClients;
    }

    @Override
    public void run() {
        try {
            // Object d??ng ????? synchronize cho vi???c giao ti???p v???i c??c ng?????i d??ng
            lock = new Object();

            // ?????c danh s??ch t??i kho???n ???? ????ng k??
            this.loadAccounts();
            // Socket d??ng ????? x??? l?? c??c y??u c???u ????ng nh???p/????ng k?? t??? user
            while (true) {
                // ?????i request ????ng nh???p/????ng xu???t t??? client
                socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // ?????c y??u c???u ????ng nh???p/????ng xu???t
                String request = dis.readUTF();

                if (request.equals("Sign up")) {
                    // Y??u c???u ????ng k?? t??? user

                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    // Ki???m tra t??n ????ng nh???p ???? t???n t???i hay ch??a
                    if (isExisted(username) == false) {

                        // T???o m???t Handler ????? gi???i quy???t c??c request t??? user n??y
                        Handler newHandler = new Handler(socket, username, password, true, lock);
                        listClients.add(newHandler);
                        // L??u danh s??ch t??i kho???n xu???ng file v?? g???i th??ng b??o ????ng nh???p th??nh c??ng cho user
                        this.saveAccounts();
                        dos.writeUTF("Sign up successful");
                        dos.flush();
                        // T???o m???t Thread ????? giao ti???p v???i user n??y
                        Thread t = new Thread(newHandler);
                        t.start();

                        // G???i th??ng b??o cho c??c client ??ang online c???p nh???t danh s??ch ng?????i d??ng tr???c tuy???n
                    } else {

                        // Th??ng b??o ????ng nh???p th???t b???i
                        dos.writeUTF("This username is being used");
                        dos.flush();
                    }
                } else if (request.equals("Log in")) {
                    // Y??u c???u ????ng nh???p t??? user

                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    // Ki???m tra t??n ????ng nh???p c?? t???n t???i hay kh??ng
                    if (isExisted(username) == true) {
                        for (Handler client : listClients) {
                            if (client.getUsername().equals(username)) {
                                // Ki???m tra m???t kh???u c?? tr??ng kh???p kh??ng
                                if (password.equals(client.getPassword())) {

                                    // T???o Handler m???i ????? gi???i quy???t c??c request t??? user n??y
                                    Handler newHandler = client;
                                    newHandler.setSocket(socket);
                                    newHandler.setIsLoggedIn(true);

                                    // Th??ng b??o ????ng nh???p th??nh c??ng cho ng?????i d??ng
                                    dos.writeUTF("Log in successful");
                                    dos.flush();
                                    textArea.append("|" + simpleDateFormat.format(new Date()) + "|" +socket +" is connected!\n");
                                    // T???o m???t Thread ????? giao ti???p v???i user n??y
                                    Thread t = new Thread(newHandler);
                                    t.start();

                                    // G???i th??ng b??o cho c??c client ??ang online c???p nh???t danh s??ch ng?????i d??ng tr???c tuy???n
                                } else {
                                    dos.writeUTF("Password is not correct");
                                    dos.flush();
                                }
                                break;
                            }
                        }

                    } else {
                        dos.writeUTF("This username is not exist");
                        dos.flush();
                    }
                }

            }

        } catch (Exception ex){
            System.err.println(ex);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Handler implements Runnable{
        // Object ????? synchronize c??c h??m c???n thi???t
        // C??c client ?????u c?? chung object n??y ???????c th???a h?????ng t??? ch??nh server
        private Object lock;

        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String username;
        private String password;
        private boolean isLoggedIn;
        private String phong;
        public Handler(Socket socket, String username, String password, boolean isLoggedIn, Object lock) throws IOException {
            this.socket = socket;
            this.username = username;
            this.password = password;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.isLoggedIn = isLoggedIn;
            this.lock = lock;
        }

        public Handler(Object lock, Socket socket, String username, String password, boolean isLoggedIn, String phong) throws IOException {
            this.lock = lock;
            this.socket = socket;
            this.username = username;
            this.password = password;
            this.isLoggedIn = isLoggedIn;
            this.phong = phong;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        }

        public DataInputStream getDis() {
            return dis;
        }

        public Handler(String username, String password, boolean isLoggedIn, Object lock) {
            this.username = username;
            this.password = password;
            this.isLoggedIn = isLoggedIn;
            this.lock = lock;
        }

        public void setIsLoggedIn(boolean IsLoggedIn) {
            this.isLoggedIn = IsLoggedIn;
        }

        public void setPhong(String phong) {
            this.phong = phong;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
            try {
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getPhong() {
            return phong;
        }

        /**
         * ????ng socket k???t n???i v???i client
         * ???????c g???i khi ng?????i d??ng offline
         */
        public void closeSocket() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean getIsLoggedIn() {
            return this.isLoggedIn;
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public DataOutputStream getDos() {
            return this.dos;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    String message = null;

                    // ?????c y??u c???u t??? user
                    message = dis.readUTF();

                    // Y??u c???u ????ng xu???t t??? user
                    if (message.equals("Log out")) {

                        // Th??ng b??o cho user c?? th??? ????ng xu???t
                        dos.writeUTF("Safe to leave");
                        dos.flush();

                        // ????ng socket v?? chuy???n tr???ng th??i th??nh offline
                        socket.close();
                        this.isLoggedIn = false;

                        // Th??ng b??o cho c??c user kh??c c???p nh???t danh s??ch ng?????i d??ng tr???c tuy???n
                        Server.updateOnlineUsers();
                        break;
                    }
                    else if(message.equals("Private chat"))
                    {
                        Server.updateOnlineUsers();
                    }
                    // Y??u c???u g???i tin nh???n d???ng v??n b???n
                    else if (message.equals("Text")){
                        String receiver = dis.readUTF();
                        String content = dis.readUTF();

                        for (Handler client: Server.listClients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("Text");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(content);
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }
                    else if (message.equals("Text Group")){
                        String sender= dis.readUTF();
                        String room = dis.readUTF();
                        String content = dis.readUTF();

                        for (Handler client: Server.listClients) {
                            if (client.getPhong()!=null) {
                                if(client.getPhong().equals(room))
                                synchronized (lock) {
                                    client.getDos().writeUTF("Text Group");
                                    client.getDos().writeUTF(sender);
                                    client.getDos().writeUTF(room);
                                    client.getDos().writeUTF(content);
                                    client.getDos().flush();
                                }
                            }
                        }
                    }

                    // Y??u c???u g???i tin nh???n d???ng Emoji
                    else if (message.equals("Emoji")) {
                        String receiver = dis.readUTF();
                        String emoji = dis.readUTF();

                        for (Handler client: Server.listClients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("Emoji");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(emoji);
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }
                    else if (message.equals("Emoji Group")) {
                        String room = dis.readUTF();
                        String emoji = dis.readUTF();

                        for (Handler client: Server.listClients) {
                            if (client.getPhong()!=null)
                            {
                                if(client.getPhong().equals(room)) {
                                    synchronized (lock) {
                                        client.getDos().writeUTF("Emoji Group");
                                        client.getDos().writeUTF(this.username);
                                        client.getDos().writeUTF(emoji);
                                        client.getDos().flush();
                                    }
                                }
                            }
                        }
                    }
                    else if (message.equals("Create room")) {
                        String member=dis.readUTF();
                        String room = dis.readUTF();
                        boolean check=false;
                        Handler cli = null;
                        for(Handler client:Server.listClients)
                        {
                            if(client.getUsername().equals(member))
                            {
                                cli=client;
                            }
                        }
                        for(int i=0;i<Room.size();i++)
                        {
                            if(Room.get(i).equals(room))
                            {
                                check=true;
                                cli.getDos().writeUTF("Exist");
                            }
                        }
                        if(check==false)
                        {
                            Room.add(room);
                            User.add(cli.username);
                            cli.setPhong(room);
                            cli.getDos().writeUTF("Successful");
                        }
                    }
                    else if (message.equals("Join room")) {
                        String member=dis.readUTF();
                        String room = dis.readUTF();
                        boolean check=false;
                        for(int i=0;i<Room.size();i++)
                        {
                            if(Room.get(i).equals(room))
                            {
                                check=true;
                                User.add(member);
                                Room.add(room);
                                dos.writeUTF("Successful");
                                Server.updateOnlineUsers();
                                break;
                            }
                        }
                        for(Handler client:listClients)
                        {
                            if(client.getUsername().equals(member))
                            {
                                client.setPhong(room);
                            }
                        }
                        if(check==false)
                        {
                            dos.writeUTF("Fail");
                        }
                    }
                    // Y??u c???u g???i File
                    else if (message.equals("File")) {

                        // ?????c c??c header c???a tin nh???n g???i file
                        String receiver = dis.readUTF();
                        String filename = dis.readUTF();
                        int size = Integer.parseInt(dis.readUTF());
                        int bufferSize = 2048;
                        byte[] buffer = new byte[bufferSize];

                        for (Handler client: Server.listClients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("File");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(filename);
                                    client.getDos().writeUTF(String.valueOf(size));
                                    while (size > 0) {
                                        // G???i l???n l?????t t???ng buffer cho ng?????i nh???n cho ?????n khi h???t file
                                        dis.read(buffer, 0, Math.min(size, bufferSize));
                                        client.getDos().write(buffer, 0, Math.min(size, bufferSize));
                                        size -= bufferSize;
                                    }
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }
                    else if (message.equals("File Group")) {

                        // ?????c c??c header c???a tin nh???n g???i file
                        String sender=dis.readUTF();
                        String r = dis.readUTF();
                        String filename = dis.readUTF();
                        int size = Integer.parseInt(dis.readUTF());
                        int bufferSize = 2048;
                        byte[] buffer = new byte[bufferSize];

                        for (Handler client: Server.listClients) {
                            if(client.getPhong()!=null){
                            if (client.getPhong().equals(r)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("File Group");
                                    client.getDos().writeUTF(sender);
                                    client.getDos().writeUTF(r);
                                    client.getDos().writeUTF(filename);
                                    client.getDos().writeUTF(String.valueOf(size));
                                    while (size > 0) {
                                        // G???i l???n l?????t t???ng buffer cho ng?????i nh???n cho ?????n khi h???t file
                                        dis.read(buffer, 0, Math.min(size, bufferSize));
                                        client.getDos().write(buffer, 0, Math.min(size, bufferSize));
                                        size -= bufferSize;
                                    }
                                    client.getDos().flush();
                                }
                            }}
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
