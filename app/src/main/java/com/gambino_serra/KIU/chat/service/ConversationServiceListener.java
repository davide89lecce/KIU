package com.gambino_serra.KIU.chat.service;

import com.gambino_serra.KIU.chat.listener.ChildListenerAdapter;
import com.gambino_serra.KIU.chat.model.ChatStatus;
import com.google.firebase.database.DataSnapshot;

/**
 * La classe modella l'ascolto delle conversazioni.
 */
public class ConversationServiceListener extends ChildListenerAdapter {

    private ChatService service;

    public ConversationServiceListener(ChatService service) {
        this.service = service;
    }

    /**
     * Il metodo viene invocato quando un nuovo messaggio viene rilevato,
     * avissando il servizio.
     *
     * @param dataSnapshot
     * @param s
     */
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        ChatStatus status = dataSnapshot.getValue(ChatStatus.class);
        if (status.newmessage == true) {
            service.handleNewMessage(status.convname);
        }
    }

    /**
     * Il metodo viene invocato quando un nuovo messaggio viene rilevato in una nuova conversazione,
     * avissando il servizio.
     *
     * @param dataSnapshot
     * @param s
     */
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        ChatStatus status = dataSnapshot.getValue(ChatStatus.class);
        if (status.newmessage == true) {
            service.handleNewMessage(status.convname);
        }
    }

}
