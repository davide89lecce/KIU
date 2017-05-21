package com.gambino_serra.KIU.chat;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.listener.ChildListenerAdapter;
import com.gambino_serra.KIU.chat.listener.ValueListenerAdapter;
import com.gambino_serra.KIU.chat.misc.MessageAdapter;
import com.gambino_serra.KIU.chat.model.ChatStatus;
import com.gambino_serra.KIU.chat.model.Message;
import com.gambino_serra.KIU.chat.model.MessageEntity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;


/**
 * La seguente classe modella il ListFragment nel quale vengono visualizzati i messaggi
 * di una conversazione.
 */
public class MessagesViewFragment extends Fragment {

    /**
     * L'attributo TAG modella il TAG della classe per il sistema di
     * Logging.
     */
    private static final String TAG = "MessagesFragment";

    /**
     * L'attributo usersList contiene il testo di tutti i messaggi della conversazione.
     * la struttura dati è separata in quanto i valori contenuti sono visualizzati
     * mediante il comportamento di default dell'ArrayAdapter.
     */
    //final private ArrayList<String> dataList = new ArrayList<>();

    /**
     * l'attributo adapter contiene il riferimento all'adapter della ListView
     */
    private MessageAdapter adapter;

    /**
     * l'attributo messageList contiene tutte le informazioni presenti dei messaggi
     * presenti nella conversazioni.
     */
    final private ArrayList<MessageEntity> messageList = new ArrayList<>();

    /**
     * L'attributo userID contiene l'UID dell'utente connesso.
     */
    private String myID;

    /**
     * L'attributo userID contiene l'UID dell'utente controparte nella conversazione.
     */
    private String otherID;

    /**
     * l'attributo contiene il riferimento allo stato della conversazione relato
     * all'utente attualmente connesso.
     */
    private DatabaseReference myChatStatusRef;

    /**
     * l'attributo contiene il riferimento alla conversazione.
     */
    private DatabaseReference conversationRef;

    /**
     * L'attributo contiene il riferimento al listener, che viene messo in ascolto
     * dell'invio del primo messaggio, qualora si abbia aperto una conversazione vuota.
     */
    private FirstMessageEventListener messageEventListener;


    /**
     * l'attributo contiene la key del nodo della conversazione corrente
     */
    private String convname;

    /**
     * l'attributo contiene true se nella conversazione sono presenti dei messaggi,
     * false altrimenti.
     */
    boolean messaging;

    //nuovi attributi
    private RecyclerView recyclerView;
    private MessageAdapter mAdapter;

    /**
     * attributi deprecati.
     */
    private static final String MY_PREFERENCES = "kiuPreferences";
    private static final String LOGGED_USER = "logged_user";



    /**
     * costruttore di default de fragment
     */
    public MessagesViewFragment() {
        // Required empty public constructor
    }

    /**
     * metodo onCreateView()
     * @param inflater inflater
     * @param container contenitore
     * @param savedInstanceState bundle
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.messages_fragment, container, false);
    }

    /**
     * Il metodo onActivityCreated viene invocato alla creazione del Fragment.
     * Nel metodo vengono eseguite le seguenti operazioni:
     * - impostazione dello UID dell'utente corrente e della sua controparte.
     * - impostanti i tutti i riferimenti al database necessari al fragment.
     * - impostato il listener per la lettura dello stato della conversazione
     *   relato all'utente corrente.
     *
     * @param savedInstanceState bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        otherID = getArguments().getString("counterpartID");

        SharedPreferences sharedPref = getActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        myID = sharedPref.getString(LOGGED_USER,"").toString();

        myChatStatusRef = FirebaseDatabase.getInstance()
                .getReference("/chat/chatstatus/" +myID+ "/" +otherID);
        myChatStatusRef.addListenerForSingleValueEvent(new ValueListenerAdapter() {

            /**
             * Il metodo onDataChange() richiede le informazioni sullo stato
             * della conversazione:
             * - imposta alcuni attributi del Fragment, tra cui l'adapter
             *   della lista;
             * - Se non vi sono messaggi nella conversazione, imposta un nuovo
             * listener per l'attributo myChatStatusRef, il cui scopo è attendere
             * la ricezione di un primo messaggio.
             * - Se vi sono dei messaggi nella conversazione, imposta un nuovo
             * stato della Chat in cui tutti i messaggi risultano letti, e
             * successivamente un listener per la lettura dei messaggi.
             * @param dataSnapshot snapshot
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ChatStatus status = dataSnapshot.getValue(ChatStatus.class);
                convname = status.convname;
                messaging = status.messaging;

                recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);

                mAdapter = new MessageAdapter(messageList,myID);

                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);

                conversationRef = FirebaseDatabase.getInstance().getReference("/chat/"+convname);

                if(!status.messaging){
                    messageEventListener = new FirstMessageEventListener();
                    myChatStatusRef.addChildEventListener(messageEventListener);
                }else{
                    oldifyMessages();
                    conversationRef.addChildEventListener( new MessageManagerEventListener());
                }
            }
        });

    }

    /**
     * Il metodo messageSwitchListener() elimina il listener sullo stato della conversazione
     * ed aggiunge il listener per la lettura dei messaggi.
     */
    private void messageSwitchListener(){
        myChatStatusRef.removeEventListener(messageEventListener);
        conversationRef.addChildEventListener( new MessageManagerEventListener());
    }

    /**
     * metodo onStart()
     */
    @Override
    public void onStart() {
        super.onStart();

    }

    /**
     * la classe implementa il listener per la lettura dei messaggi della conversazione
     */
    class MessageManagerEventListener extends ChildListenerAdapter {

        /**
         * costruttore di default
         */
        MessageManagerEventListener() {

        }

        /**
         * Il metodo onChildAdded() prende le informazioni di un messaggio,
         * popola le opportune strutture dati e avverte l'adapter del cambiamento.
         *
         * @param dataSnapshot snapshot del messaggio
         * @param s s
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message msg = dataSnapshot.getValue(Message.class);
            String key = dataSnapshot.getKey();
            messageList.add(new MessageEntity(key, msg));
            mAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
        }

    }

    /**
     * La classe implementa il listener per l'ascolto del cambio di stato nella
     * conversazione, nella fattispecie l'inserimento del primo messaggio.
     */
    class FirstMessageEventListener extends ChildListenerAdapter{

        /**
         * costruttore di default
         */
        FirstMessageEventListener(){

        }

        /**
         * il metodo onChildChanged() ricevuto il nuovo stato della conversazione,
         * invoca il metodo messageSwitchListener.
         *
         * @param dataSnapshot stato della conversazione
         * @param s s
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i(TAG, " onChildChanged() method invocation");
            messageSwitchListener();
        }

    }

    /**
     * Il metodo imposta lo stato della conversazione impostando lo stato dei
     * nuovi messaggi a false.
     */
    private void oldifyMessages(){
        myChatStatusRef.child("newmessage").setValue(false);
    }



}