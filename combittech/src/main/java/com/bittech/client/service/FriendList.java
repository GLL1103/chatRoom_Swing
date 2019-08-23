package com.bittech.client.service;

import com.bittech.server.Connect2Service;
import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FriendList {
    private JPanel FriendListPanel;
    private JButton createGroupBtn;
    private JScrollPane groupPanel;
    private JScrollPane friendListPanel;

    private Connect2Service connect2Service;
    //存储所有在线好友
    private Set<String> names;
    private String myName;

    //存储所有私聊界面
    private Map<String,privateChatGUI> privateChatGUIList = new ConcurrentHashMap<>();
    //缓存当前客户端的群聊信息(群名称和群好友)
    private Map<String,Set<String>> groupInfo = new ConcurrentHashMap<>();
    //缓存当前客户端群聊界面
    private Map<String,GroupChatGUI> groupChatGUIList = new ConcurrentHashMap<>();


    //后台任务，不断监听服务器发来的信息
    //好友上线信息，用户私聊，群聊
    private class DaemonTask implements Runnable {
        private Scanner scanner = new Scanner(connect2Service.getIn());

        @Override
        public void run() {
            while(true) {
                if(scanner.hasNext()) {
                    String strFromServer = scanner.nextLine();
                    if(strFromServer.startsWith("newLogin:")) {
                        //好友上线提醒
                        String newFriend = strFromServer.split(":")[1];
                        JOptionPane.showMessageDialog(null,newFriend+"上线了！","上线提醒",JOptionPane.INFORMATION_MESSAGE);
                        names.add(newFriend);
                        //再次刷新好友列表
                        reloadFriendList();
                    }

                    //此时服务器发来的是一个json字符串
                    if(strFromServer.startsWith("{")) {
                        MessageVO messageVO = (MessageVO) commUtil.json2Object(strFromServer,MessageVO.class);
                        if(messageVO.getType().equals(2)) {
                            String friendName = messageVO.getContent().split("-")[0];
                            String msg = messageVO.getContent().split("-")[1];
                            //判断此私聊是否是第一次创建
                            if(privateChatGUIList.containsKey(friendName)) {
                                privateChatGUI privatechatgui = privateChatGUIList.get(friendName);
                                privatechatgui.getFrame().setVisible(true);
                                privatechatgui.readFromServer(friendName+" : "+msg);
                            }else {
                                privateChatGUI privatechatgui = new privateChatGUI(friendName,myName,connect2Service);
                                privateChatGUIList.put(friendName,privatechatgui);
                                privatechatgui.readFromServer(friendName+" : "+msg);
                            }
                        }
                        else if(messageVO.getType().equals(4)) {
                            //type:4
                            //content:senderName-msg
                            //toName:groupName-[群组好友列表]
                            String ret = messageVO.getToName().split("-")[0];
                            int index = ret.indexOf("=");
                            String groupName = ret.substring(1,index);
                            String senderName = messageVO.getContent().split("-")[0];
                            String groupMsg = messageVO.getContent().split("-")[1];
                            //若此群名称在群聊列表
                            if(groupInfo.containsKey(groupName)) {
                                if(groupChatGUIList.containsKey(groupName)) {
                                    //群聊界面弹出
                                    GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                                    groupChatGUI.getFrame().setVisible(true);
                                    groupChatGUI.readFromServer(senderName+" : "+groupMsg);
                                }
                                else {
                                    Set<String> userName = groupInfo.get(groupName);
                                    GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,userName,connect2Service,myName);
                                    groupChatGUIList.put(groupName,groupChatGUI);
                                    groupChatGUI.readFromServer(senderName+" : "+groupMsg);
                                }
                            }
                            else {
                                //群成员第一次收到群聊信息
                                //将群名称以及群成员保存到当前客户端群聊列表
                                Set<String> friends = (Set<String>) commUtil.json2Object(messageVO.getToName().split("-")[1],Set.class);
                                groupInfo.put(groupName,friends);
                                reloadGroupList();
                                //弹出群聊界面
                                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,friends,connect2Service,myName);
                                groupChatGUIList.put(groupName,groupChatGUI);
                                groupChatGUI.readFromServer(senderName+" : "+groupMsg);
                            }
                        }
                    }
                }
            }
        }
    }

    public FriendList(String myName,Connect2Service connect2Service,Set<String> names) {
        this.myName = myName;
        this.connect2Service = connect2Service;
        this.names = names;
        JFrame frame = new JFrame(myName);
        frame.setContentPane(FriendListPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        reloadFriendList();
        // 新启动一个后台线程不断监听服务器发来的信息
        Thread daemonThread = new Thread(new DaemonTask());
        daemonThread.setDaemon(true);
        daemonThread.start();

        //点击创建群组弹出创建界面
        createGroupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //FriendList.this  匿名内部类
                new CreateGroupGUI(names,myName,connect2Service,FriendList.this);
            }
        });
    }

    //私聊点击事件
    private class privateLabelAction implements MouseListener {
        private String labelName;
        public privateLabelAction(String labelName) {
            this.labelName = labelName;
        }
        //鼠标点击事件
        @Override
        public void mouseClicked(MouseEvent e) {
            //判断好友列表私聊界面缓存是否已经有指定标签
            if(privateChatGUIList.containsKey(labelName)) {
                privateChatGUI privatechatGUI = privateChatGUIList.get(labelName);
                privatechatGUI.getFrame().setVisible(true);
            }
            else {
                //第一次点击，创建私聊界面
                privateChatGUI privatechatUI = new privateChatGUI(labelName,myName,connect2Service);
                privateChatGUIList.put(labelName,privatechatUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    //群聊点击事件
    private class GroupLabelAction implements MouseListener {
        private String groupName;
        public GroupLabelAction(String groupName) {
            this.groupName = groupName;
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            if(groupChatGUIList.containsKey(groupName)) {
                GroupChatGUI groupChatGUI = groupChatGUIList.get(groupName);
                groupChatGUI.getFrame().setVisible(true);
            }
            else {
                Set<String> friends = groupInfo.get(groupName);
                GroupChatGUI groupChatGUI = new GroupChatGUI(groupName,friends,connect2Service,myName);
                groupChatGUIList.put(groupName,groupChatGUI);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }

    //刷新好友列表信息
    public void reloadFriendList() {
        JPanel friendLabelPanel = new JPanel();
        JLabel[] labels = new JLabel [names.size()];
        // 迭代遍历set集合
        Iterator<String> iterator = names.iterator();
        // 设置标签为纵向对其
        friendLabelPanel.setLayout(new BoxLayout(friendLabelPanel,
                BoxLayout.Y_AXIS));
        int i = 0;
        while (iterator.hasNext()) {
            String labelName = iterator.next();
            labels [i] = new JLabel(labelName);
            //添加标签点击事件
            labels[i].addMouseListener(new privateLabelAction(labelName));
            friendLabelPanel.add(labels[i]);
            i++;
        }
        this.friendListPanel.setViewportView(friendLabelPanel);
        // 设置滚动条为垂直滚动条
        this.friendListPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        friendLabelPanel.revalidate();
        this.friendListPanel.revalidate();
    }

    //刷新群聊列表的信息
    public void reloadGroupList() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));
        Set<String> groupNames = groupInfo.keySet();
        Iterator<String> iterator = groupNames.iterator();
        while(iterator.hasNext()) {
            String groupName = iterator.next();
            JLabel label = new JLabel(groupName);
            label.addMouseListener(new GroupLabelAction(groupName));
            jPanel.add(label);
        }

        groupPanel.setViewportView(jPanel);
        groupPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        groupPanel.revalidate();
    }

    public void addGroupInfo(String groupName,Set<String> friends) {
        groupInfo.put(groupName,friends);
    }
}
