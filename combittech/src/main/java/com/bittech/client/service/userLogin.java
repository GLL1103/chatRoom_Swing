package com.bittech.client.service;

import com.bittech.client.dao.AccountDao;
import com.bittech.client.entity.User;
import com.bittech.server.Connect2Service;
import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.Set;

public class userLogin {
    private JPanel LoginPanel;
    private JTextField usernameLogintext;
    private JPasswordField passwordLogintext;
    private JButton regBtn;
    private JButton LoginBtn;

    private AccountDao accountDao = new AccountDao();

    public userLogin() {
        JFrame frame = new JFrame("用户登录");
        frame.setContentPane(LoginPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        // 居中显示
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // 点击注册按钮，弹出注册页面
        regBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new userReg();
            }
        });
        // 点击登录按钮，验证用户输入信息是否正确
        LoginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取用户输入
                String userName = usernameLogintext.getText();
                String password = String.valueOf(passwordLogintext.getPassword());
                User user = accountDao.userLogin(userName,password);
                if (user != null) {
                    // 登录成功
                    JOptionPane.showMessageDialog(null,"登录成功", "提示信息",JOptionPane.INFORMATION_MESSAGE);
                    // 跳转到用户列表界面
                    //与服务器建立连接，将用户名注册到服务器缓存
                    Connect2Service connect2Service = new Connect2Service();
                    MessageVO messageVO = new MessageVO();
                    messageVO.setType(1);
                    messageVO.setContent(userName);
                    String msgJson = commUtil.object2json(messageVO);
                    try {
                        PrintStream out = new PrintStream(connect2Service.getOut(),true,"UTF-8");
                        out.println(msgJson);
                        //读取服务器大会的响应，读取到所有的在线好友信息
                        Scanner in = new Scanner(connect2Service.getIn());
                        if(in.hasNextLine()) {
                            String jsonStr = in.nextLine();
                            MessageVO msgFromServer = (MessageVO) commUtil.json2Object(jsonStr,MessageVO.class);
                            Set<String> names = (Set<String>) commUtil.json2Object(msgFromServer.getContent(),Set.class);
                            System.out.println("在线好友为："+names);
                            //加载好友列表界面，将登陆界面不可见
                            frame.setVisible(false);
                            //跳转到好友列表界面需要传递用户名，与服务器建立的连接，所有在线好友的信息
                            new FriendList(userName,connect2Service,names);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                }else {
                    JOptionPane.showMessageDialog(null,"登录失败", "错误信息",JOptionPane.ERROR_MESSAGE);
                    // 保留当前登录界面
                }
            }
        });
    }

    public static void main(String[] args) {
        new userLogin();
    }
}
