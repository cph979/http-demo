package org.cph.http.demo;

import java.io.*;
import java.net.Socket;

/**
 * 请求处理线程
 *
 * @author cph
 * @date 2021/01/02
 */
public class RequestDemo implements Runnable {
    private Socket socket;

    public RequestDemo() {
    }

    public RequestDemo(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // 从socket中取出请求信息
        BufferedReader bf = null;
        try {
            // 获取输入流
            bf = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String line;
            int lineNum = 1;
            String requestMethod = "";
            String requestURL = "";
            String host = "";
            while ((line = bf.readLine()) != null) {

                // 只取出请求的请求行和Host
                switch (lineNum) {
                    case 1:
                        String[] head = line.split(" ");
                        requestMethod = head[0];
                        requestURL = head[1];
                        break;
                    case 2:
                        // Host: 127.0.0.1:8080
                        String[] hosts = line.split(": ");
                        host = hosts[1];
                    default:
                        break;
                }

                lineNum++;
                // 读取到空行就结束，因为http请求是长连接，无法读取到文件的末尾
                if ("".equals(line)) {
                    break;
                }
            }

            // 处理具体URL请求
            handleRequest(socket.getOutputStream(), requestURL, host, requestMethod);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理具体请求
     *
     * @param os         socket的输出流
     * @param requestURL 请求的url pattern
     * @param host       请求地址
     */
    private void handleRequest(OutputStream os, String requestURL, String host, String requestMethod) {
        PrintWriter pw = null;
        try {
            System.out.println("处理请求: http://" + host + requestURL);
            // 字节流包装成字符流
            pw = new PrintWriter(os);
            // 判断请求是否是具体文件
            int lastIndexOf = requestURL.lastIndexOf(".");
            if (lastIndexOf != -1) {    // 文件请求
                String ext = requestURL.substring(lastIndexOf + 1);
                File file = new File(Path.path + requestURL);   // 文件绝对路径
                if (file.exists()) {
                    resp200(os, file, ext);
                } else {
                    resp404(pw);
                }
            } else {    // 非文件请求
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type: text/html;charset=UTF-8");
                pw.println();
                if ("/".equals(requestURL)) {   // 首页
                    pw.println("<h3>欢迎访问Http服务器; author: cph</h3>"); // 响应体
                } else {    // 非首页
                    pw.println("<h3>非文件请求暂未开发完成</h3>");
                }
                pw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * 能找到资源并响应
     *
     * @param out      输出流
     * @param filePath 文件路径
     * @param ext      文件后缀，根据后缀响应 具体响应头
     */
    private void resp200(OutputStream out, File filePath, String ext) throws IOException {
        if ("jpg".equals(ext) || "png".equals(ext) || "gif".equals(ext)) {
            // 字节流读取磁盘图片
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            switch (ext) {
                case "jpg":
                    out.write("Content-Type: image/jpg\r\n".getBytes());
                    break;
                case "png":
                    out.write("Content-Type: image/png\r\n".getBytes());
                    break;
                case "gif":
                    out.write("Content-Type: image/gif\r\n".getBytes());
                    break;
            }
            out.write("\r\n".getBytes());
            int len;
            byte[] bytes = new byte[1024];
            while ((len = bis.read(bytes)) != -1) {
                out.write(bytes, 0, len);
                out.flush();
            }
        } else if ("html".equals(ext) || "css".equals(ext) || "js".equals(ext) || "txt".equals(ext)) {
            // 字符流读取磁盘文本内容
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            PrintWriter pw = new PrintWriter(out);
            pw.println("HTTP/1.1 200 OK");
            switch (ext) {
                case "html":
                    pw.println("Content-Type: text/html;charset=UTF-8");
                    break;
                case "css":
                    pw.println("Content-Type: text/css");
                    break;
                case "js":
                    pw.println("Content-Type: application/x-javascript");
                    break;
                default:
                    pw.println("Content-Type: text/plain;charset=UTF-8");
                    break;
            }
            pw.println();
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(line);
                pw.flush();
            }
        }
    }

    /**
     * 找不到资源响应
     *
     * @param pw
     */
    private void resp404(PrintWriter pw) {
        pw.println("HTTP/1.1 404");
        pw.println("Content-Type: text/html;charset=UTF-8");
        pw.println();
        pw.println("<h3>Page Not Found</h3>");
        pw.flush();
    }

}
