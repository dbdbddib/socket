package com.ybh.simplesocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    private final static int port = 33333;
    // 0~9999 까지는 대부분 프로그램이 사용한다.
    // 그래서 1만번이상 하는게 좋다. 10000 ~ 65534

    public ServerSocket serverSocket = null;
    public Socket acceptSocket = null;
    public BufferedWriter socketWriter = null;
    public BufferedReader socketReader = null;
    public BufferedReader keyboardReader = null;

    public static void main(String[] args) {
        // 서버 소켓을 만든다. (serverSocket)
        // 포트를 지정하고 bind, listen 으로 클라이언트 접속 할때까지 대기한다. (
        // 블로킹 상태) 동기상태
        // 클라이언트 로부터 접속이 되면 클라이언트와 연결할 소켓을 리턴하다. (acceptSocket)
        // 클라이언트와 연결된 소켓으로 읽거나 쓴다. 읽을때는 동기상태 (블로킹)

        ServerApp sa = new ServerApp();
        sa.doNetworking();
    }

    public void init() throws IOException {
//        this.serverSocket = new ServerSocket(port);
        System.out.println("서버소켓으로 클라이언트 접속 대기 중");
        this.acceptSocket = serverSocket.accept(); // 블로킹 상태
        System.out.printf("클라이언트[%s] 연결 됨%n", this.acceptSocket.getInetAddress());

        this.socketWriter = new BufferedWriter(
                new OutputStreamWriter(this.acceptSocket.getOutputStream())
        );
        this.socketReader = new BufferedReader(
                new InputStreamReader(this.acceptSocket.getInputStream())
        );
        this.keyboardReader = new BufferedReader(
                new InputStreamReader(System.in)
        );
    }

    public void doNetworking() {
        try {
            this.serverSocket = new ServerSocket(port);
            this.init();

            ReadClientSocketThread rcst = new ReadClientSocketThread();
            rcst.start();

            while(true) {
                System.out.print("서버에서 문자열 입력 : ");
                String keyboardMsg = this.keyboardReader.readLine(); // 블로킹 상 태
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
            this.closeAll();
            System.out.println("서버 프로그램 종료");
        }
    }

    public void closeAll() {
        try {
            if (this.keyboardReader != null) {
                this.keyboardReader.close();
            }
            if (this.socketReader != null) {
                this.socketReader.close();
            }
            if (this.socketWriter != null) {
                this.socketWriter.close();
            }
            if (this.acceptSocket != null) {
                this.acceptSocket.close();
            }
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ReadClientSocketThread extends Thread {
        @Override
        public void run() {
            while(true) {
                try {
                    String readMsg = socketReader.readLine(); // 블로킹 상태
                    System.out.printf("서버가 받은 메시지 : %s%n", readMsg);
                    if( readMsg == null || "exit".equalsIgnoreCase(readMsg) ) {
                        System.out.println("클라이언트 접속 해제");
                        if (socketReader != null) {
                            socketReader.close();
                        }
                        if (socketWriter != null) {
                            socketWriter.close();
                        }
                        if (acceptSocket != null) {
                            acceptSocket.close();
                        }
                        init();
                    }
                } catch (Exception ex) {
                    closeAll();
                    break;
                }
            }
        }
    }
}
