package com.lhg1304.hyuckapp.page02.firemessenger.models;

import lombok.Data;

/**
 * Created by lhg1304 on 2017-11-15.
 */

@Data
public class User {

    private String uid;

    private String email;
    private String name;
    private String profileUrl;
    private boolean selection;

    public User() { }

    public User(String uid, String email, String name, String profileUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.profileUrl = profileUrl;
    }
}
