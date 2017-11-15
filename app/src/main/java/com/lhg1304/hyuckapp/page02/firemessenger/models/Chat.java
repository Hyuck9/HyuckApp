package com.lhg1304.hyuckapp.page02.firemessenger.models;

import java.util.Date;

import lombok.Data;

/**
 * Created by lhg1304 on 2017-11-15.
 */
@Data
public class Chat {

    private String chatId;
    private String title;
    private Date createDate;
    private TextMessage lastMessage;
    private boolean disabled;
    private int totalUnreadCount;

}
