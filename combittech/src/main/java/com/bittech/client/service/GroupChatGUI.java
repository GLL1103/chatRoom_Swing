package com.bittech.client.service;

import com.bittech.server.Connect2Service;
import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

public class GroupChatGUI {
    private JPanel GroupChatGUI;
    private JTextArea readFromServer;
    private JTextField send2Server;
    private JPanel friendPanel;
    private JFrame frame;

    private String groupName;
    private Set<String> friends;
    private Connect2Service connect2Service;
    private String myName;

    public GroupChatGUI(String groupName,Set<String> friends,Connect2Service connect2Service,String myName) {
        this.groupName = groupName;
        this.friends = friends;
        this.connect2Service = connect2Service;
        this.myName = myName;

        frame = new JFrame(groupName+"-"+myName);
        frame.setContentPane(GroupChatGUI);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //好友列表的展示
        friendPanel.setLayout(new BoxLayout(friendPanel,BoxLayout.Y_AXIS));
        Iterator<String> iterator = friends.iterator();
        while(iterator.hasNext()) {
            String friendName = iterator.next();
            JLabel label = new JLabel(friendName);
            friendPanel.add(label);
        }

        //文本框发送事件
        send2Server.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(send2Server.getText());
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //type:4
                    //content:senderName-msg
                    //toName:groupName
                    String msg = sb.toString();
                    if(null == msg) {
                        return;
                    }
                    MessageVO messageVO = new MessageVO();
                    messageVO.setType(4);
                    messageVO.setContent(myName+"-"+msg);
                    messageVO.setToName(groupName);
                    try {
                        PrintStream out = new PrintStream(connect2Service.getOut(),true,"UTF-8");
                        out.println(commUtil.object2json(messageVO));
                        send2Server.setText("");
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public void readFromServer(String msg) {
        readFromServer.append(msg+"\n");
    }

    public JFrame getFrame() {
        return frame;
    }
}
