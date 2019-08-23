package com.bittech.client.service;

import com.bittech.client.dao.AccountDao;
import com.bittech.client.entity.User;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class userReg {
    private JPanel RegPanel;
    private JTextField usernameRegtext;
    private JPasswordField passwordRegtext;
    private JTextField briefRegtext;
    private JButton commitBtn;

    private AccountDao accountDao = new AccountDao();

    public userReg() {
        JFrame frame = new JFrame("用户注册");
        frame.setContentPane(RegPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        // 点击提交按钮触发此方法
        commitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1.获取界面上三个控件的内容
                String userName  = usernameRegtext.getText();
                String password = String.valueOf(
                        passwordRegtext.getPassword());
                String brief = briefRegtext.getText();
                // 2.调用dao方法将信息持久化到数据库
                User user = new User();
                user.setUserName(userName);
                user.setPassword(password);
                user.setBrief(brief);
                System.out.println(user);
                if (accountDao.userReg(user)) {
                    // 弹出提示框提示用户信息注册成功
                    // 返回登录界面
                    JOptionPane.showMessageDialog(null,
                            "注册成功!","成功信息",
                            JOptionPane.INFORMATION_MESSAGE);
                    frame.setVisible(false);
                }else {
                    // 弹出提示框告知用户注册失败
                    // 保留当前注册页面
                    JOptionPane.showMessageDialog(null, "注册失败!","错误信息",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
