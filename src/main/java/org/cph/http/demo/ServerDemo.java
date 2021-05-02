package org.cph.http.demo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务监听
 *
 * @author cph
 * @date 2021/01/02
 */
public class ServerDemo implements Runnable {
    private int port;

    public ServerDemo() {
    }

    public ServerDemo(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        // 服务器线程监听地址端口
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("监听 " + port + " 端口中...");

            // 处于【阻塞状态】，没有资源等待资源的状态
            // 接收到请求后服务关闭，故让其循环监听
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("服务器线程 [" + Thread.currentThread().getName() + "] 收到请求");

                // 交给另一个线程处理请求，该线程只负责监听
                Thread thread = new Thread(new RequestDemo(socket));
                System.out.println("请求处理线程 [" + thread.getName() + "] 处理请求");
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new ServerDemo(8088)).start();
    }

}
