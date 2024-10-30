package com.ybh.multisocket;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiServerApp {
    private final static int port = 33334;
    // 0~9999 까지는 대부분 프로그램이 사용한다.
    // 그래서 1만번이상 하는게 좋다. 10000 ~ 65534

    public ServerSocket serverSocket = null;
    public BufferedReader keyboardReader = null;

    public List<MultiClientSocket> multiClientSocketList = null;

//    public Socket acceptSocket = null;
//    public BufferedWriter socketWriter = null;
//    public BufferedReader socketReader = null;

    class MultiClientSocket {
        public InetAddress ipAddr = null;
        public Socket acceptSocket = null;
        public BufferedWriter socketWriter = null;
        public BufferedReader socketReader = null;

        /**
         * 비기본 생성자 socket 매개변수를 받아서 저장하고, 버퍼읽기, 버퍼쓰기 객체를 생성한다.
         * @param socket
         * @throws IOException
         */
        public MultiClientSocket(Socket socket) throws IOException {
            this.acceptSocket = socket;
            this.socketWriter = new BufferedWriter(
                    new OutputStreamWriter(this.acceptSocket.getOutputStream())
            );
            this.socketReader = new BufferedReader(
                    new InputStreamReader(this.acceptSocket.getInputStream())
            );
            this.ipAddr = this.acceptSocket.getInetAddress();
        }

        /**
         * 소켓에 버퍼쓰기로 데이터를 쓴다.
         * @param message
         * @throws IOException
         */
        public void write(String message) throws IOException {
            this.socketWriter.write(message);
            this.socketWriter.newLine();
            this.socketWriter.flush();
        }

        /**
         * 버퍼읽기, 버퍼쓰기, 소켓을 해제한다.
         * @throws IOException
         */
        public void close() throws IOException {
            if (this.socketReader != null) {
                this.socketReader.close();
            }
            if (this.socketWriter != null) {
                this.socketWriter.close();
            }
            if (this.acceptSocket != null) {
                this.acceptSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        // 서버 소켓을 만든다. (serverSocket)
        // 포트를 지정하고 bind, listen 으로 클라이언트 접속 할때까지 대기한다. (블로킹 상태) 동기상태
        // 클라이언트 로부터 접속이 되면 클라이언트와 연결할 소켓을 리턴하다. (acceptSocket)
        // 클라이언트와 연결된 소켓으로 읽거나 쓴다. 읽을때는 동기상태 (블로킹)

        MultiServerApp sa = new MultiServerApp();
        sa.doNetworking();
    }

    public void init() throws IOException {
        System.out.println("서버소켓으로 클라이언트 접속 대기 중");
        this.keyboardReader = new BufferedReader(
                new InputStreamReader(System.in)
        );
        this.multiClientSocketList = new ArrayList<>();
    }

    public void doNetworking() {
        try {
            this.serverSocket = new ServerSocket(port);
            this.init();

            AcceptClientSocketThread acst = new AcceptClientSocketThread();
            acst.start();

            while(true) {
                String keyboardMsg = this.keyboardReader.readLine(); // 블로킹 상태
                if (keyboardMsg.isEmpty()) {
                    continue;
                }
                this.writeClientList(keyboardMsg, null);
                if( "exit".equalsIgnoreCase(keyboardMsg) ) {
                    break;
                }
            }
        } catch (IOException ioE) {
            System.out.println("IOException");
            System.out.println(ioE.toString());
        } catch (Exception ex) {
            System.out.println("Exception");
            System.out.println(ex.toString());
        } finally {
            this.closeAll();
            System.out.println("서버 프로그램 종료");
            System.exit(-1);
        }
    }

    public void writeClientList(String message, MultiClientSocket mcs) throws IOException {
        String msg = "";
        if (mcs == null) {
            msg = String.format("Server : %s", message);
        } else {
            msg = String.format("%s : %s", mcs.ipAddr, message);
        }
        for (MultiClientSocket socket : this.multiClientSocketList) {
            if (mcs == null) {
                socket.write(msg);
            } else if (mcs.hashCode() != socket.hashCode()) {
                socket.write(msg);
            }
        }
    }

    public void closeAll() {
        try {
            if (this.keyboardReader != null) {
                this.keyboardReader.close();
            }
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AcceptClientSocketThread extends Thread {
        @Override
        public void run() {
            while(true) {
                try {
                    Socket accept = serverSocket.accept();
                    MultiClientSocket mcs = new MultiClientSocket(accept);
                    multiClientSocketList.add(mcs);
                    System.out.printf("클라이언트[%s] 연결, [%d]\n", mcs.ipAddr, multiClientSocketList.size());

                    ReadClientSocketThread rcst = new ReadClientSocketThread(mcs);
                    rcst.start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    break;
                }
            }
        }
    }

    class ReadClientSocketThread extends Thread {
        private MultiClientSocket myClientSocket;

        public ReadClientSocketThread(MultiClientSocket mcs) {
            this.myClientSocket = mcs;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    String readMsg = this.myClientSocket.socketReader.readLine(); // 블로킹 상태
                    System.out.printf("%s : %s%n", this.myClientSocket.ipAddr, readMsg);
                    if( readMsg == null || "exit".equalsIgnoreCase(readMsg) ) {
                        this.myClientSocket.close();
                        multiClientSocketList.remove(this.myClientSocket);
                        System.out.printf("클라이언트 접속 해제, [%d]\n", multiClientSocketList.size());
                        break;
                    } else {
                        writeClientList(readMsg, this.myClientSocket);
                    }
                } catch (Exception ex) {
                    try {
                        this.myClientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        break;
                    }
                }
            }
        }
    }
}
