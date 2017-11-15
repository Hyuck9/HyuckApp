package com.lhg1304.hyuckapp.page02.firemessenger.models;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * Created by lhg1304 on 2017-11-15.
 */
@Data
public class Message {

    private String messageId;
    private User messageUser;
    private String chatId;
    private int unreadCount;
    private Date messageDate;
    private MessageType messageType;
    private List<String> readUserList;

    public enum MessageType {
        TEXT, PHOTO
    }

}
