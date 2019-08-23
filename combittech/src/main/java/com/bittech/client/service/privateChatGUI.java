package com.bittech.client.service;

import com.bittech.server.Connect2Service;
import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class privateChatGUI {
    private JPanel privateChatPanel;
    private JTextArea readFromServer;
    private JTextField send2server;

    private String friendName;
    private String myName;
    private Connect2Service connect2Service;
    private JFrame frame;
    private PrintStream out;

    public privateChatGUI(String friendName,String myName,Connect2Service connect2Service) {
        this.friendName = friendName;
        this.myName = myName;
        this.connect2Service = connect2Service;
        try {
            this.out = new PrintStream(connect2Service.getOut(),true,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        frame = new JFrame("与"+friendName+"私聊中..."+" - "+myName);
        frame.setContentPane(privateChatPanel);
        //设置窗口关闭的操作，将其设置为隐藏
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(400,400);
        frame.setVisible(true);

        //捕捉输入框的键盘输入   回车发送
        send2server.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(send2server.getText());
                // 1.捕捉到enter
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // 2.将当前信息发送到服务器
                    String msg = sb.toString();
                    if(null == msg) {
                        return ;
                    }
                    MessageVO messageVO = new MessageVO();
                    messageVO.setType(2);
                    messageVO.setContent(myName+"-"+msg);
                    messageVO.setToName(friendName);
                    privateChatGUI.this.out.println(commUtil.object2json(messageVO));
                    // 3.将自己发送的信息展示到当前私聊界面
                    readFromServer(myName+": "+msg);
                    send2server.setText("");
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
