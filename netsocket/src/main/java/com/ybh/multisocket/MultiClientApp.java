package com.ybh.multisocket;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MultiClientApp {
    private static int port;
//    private final static String serverIp = "127.0.0.1";
//    private final static String serverIp = "192.168.0.10";
    private static String serverIp;

    private Socket clientSocket = null;
    private BufferedWriter socketWriter = null;
    private BufferedReader socketReader = null;
    private BufferedReader keyboardReader = null;

    public MultiClientApp(String ip,int port) {
        this.serverIp = ip;
        this.port = port;
    }

    public static void main(String[] args) {
        // 클라이언트 소켓을 만든다. (IP주소, 포트번호) (clientSocket)
        // 클라이언트 소켓으로 서버에 접속 시도 한다.
        // 서버와 연결된 클라이언트 소켓으로 읽거나 쓴다.
        // 읽을때는 동기상태 (블로킹)

        String ip;
        int port;

        Scanner sc = new Scanner(System.in);
        if (args.length > 0) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        } else {
                System.out.print("ip 입력하세요 ( 0.0.0.0 ): ");
                ip = sc.nextLine();
                System.out.print("port 입력하세요: ");
                port = sc.nextInt();
        }

        MultiClientApp ca = new MultiClientApp(ip, port);
        ca.doNetworking();
    }

    public void init() throws IOException {
        this.clientSocket = new Socket(serverIp, port);
        System.out.println("클라이언트 소켓 생성후 서버에 접속 성공");

        socketWriter = new BufferedWriter(
                new OutputStreamWriter(clientSocket.getOutputStream())
        );
        socketReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );
        keyboardReader = new BufferedReader(
                new InputStreamReader(System.in)
        );
//        socketWriter.write(String.format("클라이언트[%s] 에서 첫 문자열 전송 함", getMyIp()));
//        socketWriter.newLine();
//        socketWriter.flush();
    }
    
    private String getMyIp() {
        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
        }
        catch ( UnknownHostException e ) {
            e.printStackTrace();
        }
        if( local == null ) {
            return "0.0.0.0";
        }
        else {
            String ip = local.getHostAddress();
            return ip;
        }

    }

    public void doNetworking() {
        try {
            this.init();

            Thread rsst = new Thread(new ReadServerSocketThread());
            rsst.start();

            while (true) {
                String keyboardMsg = this.keyboardReader.readLine(); // 블로킹 상태
                if (keyboardMsg.isEmpty()) {
                    continue;
                }
                this.socketWriter.write(keyboardMsg);
                this.socketWriter.newLine();
                this.socketWriter.flush();
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
            closeAll();
            System.out.println("클라이언트 프로그램 종료");
        }
    }

    public void closeAll() {
        try {
            if (keyboardReader != null) {
                keyboardReader.close();
            }
            if (socketReader != null) {
                socketReader.close();
            }
            if (socketWriter != null) {
                socketWriter.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    class ReadServerSocketThread implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    String readMsg = socketReader.readLine(); // 블로킹 상태
                    System.out.println(readMsg);
                    if (readMsg == null || "Server : exit".equalsIgnoreCase(readMsg)) {
                        System.exit(-1);
                    }
                } catch (IOException ex) {
                    closeAll();
                    break;
                }
            }
        }
    }
}
