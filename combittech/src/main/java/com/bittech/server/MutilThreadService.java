package com.bittech.server;

import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MutilThreadService {
    private static final Integer PORT;
    static {
        Properties pros = commUtil.loadProperties("socket.properties");
        PORT = Integer.valueOf(pros.getProperty("PORT"));
    }

    //服务器缓存所有连接的客服端对象
    private static Map<String,Socket> clients = new ConcurrentHashMap<>();

    //缓存所有群名称以及群众的成员姓名
    private static Map<String,Set<String>> groupInfo = new ConcurrentHashMap<>();

    //服务器具体处理客户端请求的任务
    private static class ExecutClinet implements Runnable {
        private Socket client;
        private Scanner in;
        private PrintStream out;

        public ExecutClinet(Socket client) {
            this.client = client;
            try {
                this.in = new Scanner(client.getInputStream());
                this.out = new PrintStream(client.getOutputStream(),true,"UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(true) {
                if(in.hasNextLine()) {
                    String strFromClient = in.nextLine();
                    MessageVO msgFromClient = (MessageVO) commUtil.json2Object(strFromClient,MessageVO.class);
                    if(msgFromClient.getType().equals(1)) {
                        //新用户注册
                        String userName = msgFromClient.getContent();
                        //将当前聊天室在线好友信息发回给新用户
                        Set<String> names = clients.keySet();
                        MessageVO msg2Client = new MessageVO();
                        msg2Client.setType(1);
                        msg2Client.setContent(commUtil.object2json(names));
                        out.println(commUtil.object2json(msg2Client));
                        //新用户上限信息发给其他在线用户
                        String loginMsg = "newLogin:"+userName;
                        for(Socket socket : clients.values()) {
                            try {
                                PrintStream out = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                                out.println(loginMsg);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //将新用户信息保存到当前服务器缓存
                        System.out.println(userName+"上线了！");
                        clients.put(userName,client);
                        System.out.println("当前聊天室在线人数为："+clients.size());
                    }
                    else if(msgFromClient.getType().equals(2)){
                        String friendName = msgFromClient.getToName();
                        Socket socket = clients.get(friendName);
                        try {
                            PrintStream out = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                            MessageVO msg2client = new MessageVO();
                            msg2client.setType(2);
                            msg2client.setContent(msgFromClient.getContent());
                            System.out.println("收到私聊信息，内容为: "+msgFromClient.getContent());
                            out.println(commUtil.object2json(msg2client));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(msgFromClient.getType().equals(3)) {
                        //注册群信息
                        String groupName = msgFromClient.getContent();
                        Set<String> friends = (Set<String>) commUtil.json2Object(msgFromClient.getToName(),Set.class);
                        groupInfo.put(groupName,friends);
                        System.out.println("注册群成功，当前共有"+groupInfo.size()+"个群");
                    }
                    else if(msgFromClient.getType().equals(4)) {
                        //群聊信息
                        String groupName = msgFromClient.getToName();
                        Set<String> friends = groupInfo.get(groupName);
                        //将群聊信息转发到响应的客户端
                        Iterator<String> iterator = friends.iterator();
                        while(iterator.hasNext()) {
                            String socketName = iterator.next();
                            Socket client = clients.get(socketName);
                            try {
                                PrintStream out = new PrintStream(client.getOutputStream(),true,"UTF-8");
                                //type:4
                                //content:senderName-msg
                                //toName:groupName-[群好友列表]
                                MessageVO messageVO = new MessageVO();
                                messageVO.setType(4);
                                messageVO.setContent(msgFromClient.getContent());
                                messageVO.setToName(groupInfo+"-"+commUtil.object2json(friends));
                                out.println(commUtil.object2json(messageVO));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        //创建50大小的固定大小线程池  表示可以让50个客户端同时在线
        ExecutorService executors = Executors.newFixedThreadPool(50);
        for(int i= 0;i<50;++i) {
            System.out.println("等待客户端连接...");
            Socket client = server.accept();
            System.out.println("有新的连接，端口号为："+client.getPort());
            executors.submit(new ExecutClinet(client));
        }
    }
}
