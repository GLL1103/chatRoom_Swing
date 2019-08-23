package com.bittech.client.service;

import com.bittech.server.Connect2Service;
import com.bittech.util.commUtil;
import com.bittech.vo.MessageVO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CreateGroupGUI {
    private JPanel createGroupPanel;
    private JPanel checkBoxPanel;
    private JTextField groupNameText;
    private JButton confromBtn;

    private String myName;
    private Set<String> friends;
    private Connect2Service connect2Service;
    private FriendList friendList;

    public CreateGroupGUI(Set<String> friends, String myName, Connect2Service connect2Service,FriendList friendList) {
        this.myName = myName;
        this.friends = friends;
        this.connect2Service = connect2Service;
        this.friends = friends;

        JFrame frame = new JFrame("创建群组");
        frame.setContentPane(createGroupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //1.动态的添加CheckBox
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel,BoxLayout.Y_AXIS));
        Iterator<String> iterable = friends.iterator();
        while(iterable.hasNext()) {
            String friendName = iterable.next();
            JCheckBox checkBox = new JCheckBox(friendName);
            checkBoxPanel.add(checkBox);
        }
        checkBoxPanel.revalidate();


        confromBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //获取群名
                String groupName = groupNameText.getText();
                //获取选中的好友名称
                Set<String> selectedFriends = new HashSet<>();
                //获取CheckBoxPanel下的所有组件
                Component[] components = checkBoxPanel.getComponents();
                for(Component component : components) {
                    //向下转型
                    JCheckBox checkBox = (JCheckBox) component;
                    //若是选中好友，添加进群组好友列表
                    if(checkBox.isSelected()) {
                        selectedFriends.add(checkBox.getText());
                    }
                }
                //将自己添加进群组
                selectedFriends.add(myName);

                //将群名称与选择的好友发送到服务端
                //type:3
                //content:groupName
                //toName:[]
                MessageVO messageVO = new MessageVO();
                messageVO.setType(3);
                messageVO.setContent(groupName);
                messageVO.setToName(commUtil.object2json(selectedFriends));
                try {
                    PrintStream out = new PrintStream(connect2Service.getOut(),true,"UTF-8");
                    out.println(commUtil.object2json(messageVO));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                frame.setVisible(false);

                //返回好友列表界面，展示当前群名
                friendList.addGroupInfo(groupName,selectedFriends);
                friendList.reloadGroupList();
            }
        });
    }
}
