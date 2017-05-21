package com.gambino_serra.KIU.chat.misc;

import com.gambino_serra.KIU.chat.model.Message;

/**
 * La classe modella le informazioni relative ad una conversazione.
 */
public class ConversationInfo {

    public String counterpartID;

    public String counterpartName;

    public String conversationName;

    public Message conversationLastMessage;

    public ConversationInfo(){
        conversationLastMessage = new Message();
    }
}
