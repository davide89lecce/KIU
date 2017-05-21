package com.gambino_serra.KIU.chat.listener;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

/**
 * La classe implementa l'interfaccia ChildEventListener allo scopo di modellare il pattern Adapter.
 */
public class ChildListenerAdapter implements ChildEventListener {

    private final String TAG = "ValueListenerAdapter";

    public ChildListenerAdapter() {

    }

    /**
     * Il metodo onChildAdded() viene invocato quando un nuovo figlio
     * viene aggiunto al nodo.
     *
     * @param dataSnapshot
     * @param s
     */
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "onChildAdded() method invoked");
    }

    /**
     * Il metodo onChildChanged() viene invocato quando vi sono delle
     * modifiche su di un figlio del nodo.
     *
     * @param dataSnapshot
     * @param s
     */
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "onChildChanged() method invoked");

    }

    /**
     * Il metodo onChildRemoved() viene invocato quando viene rimosso
     * un figlio del nodo.
     *
     * @param dataSnapshot
     */
    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.i(TAG, "onChildRemoved() method invoked");
    }

    /**
     * Il metodo onChildMoved viene invoato quando viene cambiata
     * la priorità in un figlio del nodo.
     *
     * @param dataSnapshot
     * @param s
     */
    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.i(TAG, "onChildMoved() method invoked");
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
