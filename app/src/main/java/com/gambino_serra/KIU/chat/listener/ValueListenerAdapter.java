package com.gambino_serra.KIU.chat.listener;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * La classe implementa l'interfaccia ValueEventListener allo scopo di modellare il pattern Adapter.
 */
public abstract class ValueListenerAdapter implements ValueEventListener {

    private final String TAG = "ValueListenerAdapter";

    public ValueListenerAdapter(){

    }

    /**
     * Il metodo onDataChange() viene invocato con un nuovo snapshot dei dati
     * sui cui si è in ascolto.
     *
     * @param dataSnapshot
     */
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        Log.i(TAG, "onDataChange() method invoked");
    }

    /**
     * Il metodo onCancelled() viene invocato quando il Listener fallisce sul
     * server o quando viene rimosso per motivi di sicurezza (il Listener).
     *
     * @param databaseError
     */
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.i(TAG, "onCancelled() method invoked");
    }
}
