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
                    int KQ = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn đóng server không?", "Notice", JOptionPane.YES_NO_OPTION);
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
            // Object dùng để synchronize cho việc giao tiếp với các người dùng
            lock = new Object();

            // Đọc danh sách tài khoản đã đăng ký
            this.loadAccounts();
            // Socket dùng để xử lý các yêu cầu đăng nhập/đăng ký từ user
            while (true) {
                // Đợi request đăng nhập/đăng xuất từ client
                socket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                // Đọc yêu cầu đăng nhập/đăng xuất
                String request = dis.readUTF();

                if (request.equals("Sign up")) {
                    // Yêu cầu đăng ký từ user

                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    // Kiểm tra tên đăng nhập đã tồn tại hay chưa
                    if (isExisted(username) == false) {

                        // Tạo một Handler để giải quyết các request từ user này
                        Handler newHandler = new Handler(socket, username, password, true, lock);
                        listClients.add(newHandler);
                        // Lưu danh sách tài khoản xuống file và gửi thông báo đăng nhập thành công cho user
                        this.saveAccounts();
                        dos.writeUTF("Sign up successful");
                        dos.flush();
                        // Tạo một Thread để giao tiếp với user này
                        Thread t = new Thread(newHandler);
                        t.start();

                        // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                    } else {

                        // Thông báo đăng nhập thất bại
                        dos.writeUTF("This username is being used");
                        dos.flush();
                    }
                } else if (request.equals("Log in")) {
                    // Yêu cầu đăng nhập từ user

                    String username = dis.readUTF();
                    String password = dis.readUTF();

                    // Kiểm tra tên đăng nhập có tồn tại hay không
                    if (isExisted(username) == true) {
                        for (Handler client : listClients) {
                            if (client.getUsername().equals(username)) {
                                // Kiểm tra mật khẩu có trùng khớp không
                                if (password.equals(client.getPassword())) {

                                    // Tạo Handler mới để giải quyết các request từ user này
                                    Handler newHandler = client;
                                    newHandler.setSocket(socket);
                                    newHandler.setIsLoggedIn(true);

                                    // Thông báo đăng nhập thành công cho người dùng
                                    dos.writeUTF("Log in successful");
                                    dos.flush();
                                    textArea.append("|" + simpleDateFormat.format(new Date()) + "|" +socket +" is connected!\n");
                                    // Tạo một Thread để giao tiếp với user này
                                    Thread t = new Thread(newHandler);
                                    t.start();

                                    // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
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
        // Object để synchronize các hàm cần thiết
        // Các client đều có chung object này được thừa hưởng từ chính server
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
         * Đóng socket kết nối với client
         * Được gọi khi người dùng offline
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

                    // Đọc yêu cầu từ user
                    message = dis.readUTF();

                    // Yêu cầu đăng xuất từ user
                    if (message.equals("Log out")) {

                        // Thông báo cho user có thể đăng xuất
                        dos.writeUTF("Safe to leave");
                        dos.flush();

                        // Đóng socket và chuyển trạng thái thành offline
                        socket.close();
                        this.isLoggedIn = false;

                        // Thông báo cho các user khác cập nhật danh sách người dùng trực tuyến
                        Server.updateOnlineUsers();
                        break;
                    }
                    else if(message.equals("Private chat"))
                    {
                        Server.updateOnlineUsers();
                    }
                    // Yêu cầu gửi tin nhắn dạng văn bản
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

                    // Yêu cầu gửi tin nhắn dạng Emoji
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
                    // Yêu cầu gửi File
                    else if (message.equals("File")) {

                        // Đọc các header của tin nhắn gửi file
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
                                        // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
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

                        // Đọc các header của tin nhắn gửi file
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
                                        // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
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
