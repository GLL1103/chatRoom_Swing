package com.bittech.vo;

import lombok.Data;

/*
服务器与客户端通信载体
 */
@Data
public class MessageVO {
    //告知服务器要进行的操作
    // 1：新用户注册 2：私聊 3：群聊
    private Integer type;

    //服务器与客户端聊天具体内容
    private String content;

    //聊天信息发送的目标客户端名称
    private String toName;
}
