package com.pdp.exam.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class User {
    String chatId;
    State state;

    public User(String chatId, State state) {
        this.chatId = chatId;
        this.state = state;
    }
}
