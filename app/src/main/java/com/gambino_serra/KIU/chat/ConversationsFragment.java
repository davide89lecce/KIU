package com.gambino_serra.KIU.chat;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.listener.ChildListenerAdapter;
import com.gambino_serra.KIU.chat.listener.ValueListenerAdapter;
import com.gambino_serra.KIU.chat.misc.ConversationInfo;
import com.gambino_serra.KIU.chat.model.ChatStatus;
import com.gambino_serra.KIU.chat.model.Message;
import com.gambino_serra.KIU.chat.model.UserCard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * La seguente classe modella il ListFragment nel quale vengono visualizzate tutte
 * le conversazioni.
 */
public class ConversationsFragment extends ListFragment
        implements AdapterView.OnItemClickListener {

    private static final String MY_PREFERENCES = "kiuPreferences";
    private static final String LOGGED_USER = "logged_user";

    /**
     * L'attributo modella il TAG della classe per il sistema di
     * Logging.
     */
    private static final String TAG = "ConversationsFragment";


    /**
     * L'attributo contiene i nomi di tutti gli utenti con
     * i quali e' instaurata una conversazione. La struttura dati è separata
     * in quanto i valori contenuti sono visualizzati mediante il
     * comportamento di default dell'ArrayAdapter.
     */
    final private ArrayList<String> usersList = new ArrayList<>();

    /**
     * L'attributo contiene tutte le informazioni su ogni conversazione.
     */
    final private ArrayList<ConversationInfo> infoList = new ArrayList<>();

    /**
     * L'attributo contiene i tutti i riferimenti di tipo DatabaseReference
     * utilizzati.
     */
    final private ArrayList<DatabaseReference> refs = new ArrayList();

    /**
     * L'attributo contiene il riferimento alle conversazioni dell'utente
     * presenti nel database di FireBase.
     */
    private DatabaseReference mConversations;

    /**
     * L'attributo contiene l'UID dell'utente connesso.
     */
    private String userID;

    /**
     * l'attributo contiene il riferimento all'Adapter della ListView.
     */
    private ConversationAdapter adapter;
    /**
     * L'attributo e' un riferimento all'Activity contenitore con upcasting all'interfaccia per la
     * comunicazione IFC.
     */
    private ConversationFragmentCommunicatorInterface communicator;

    public ConversationsFragment() {
    }

    /**
     * Il metodo crea il riferimento per la comunicazione IFC.
     *
     * @param activity Activity contenitore
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            communicator = (ConversationFragmentCommunicatorInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NotificationFragmentCommunicatorInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.conversations_fragment, container, false);
    }

    /**
     * Il metodo viene invocato a seguito della selezione di una converazione
     * passando le informazione all'Activity affinchè possa essere creata
     * l'Activity MessagesActivity per la lettura/ricezione e l'invio dei messaggi.
     *
     * @param parent   parent
     * @param view     view
     * @param position posizione dell'elemento selezionato
     * @param id       id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ConversationInfo info = infoList.get(position);
        communicator.getClickedConversationInfo(info.conversationName, info.counterpartID, info.counterpartName);
    }

    /**
     * Il metodo viene invocato alla creazione del Fragment.
     * Nel metodo vengono creati e associati l'Adapter ed il Listener della
     * lista, e impostati gli attributi del Fragment corrispondenti ad informazioni di stato
     * ed ai riferimenti al database della chat.
     *
     * @param savedInstanceState bundle
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ConversationAdapter();
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        userID = sharedPref.getString(LOGGED_USER, "").toString();
        mConversations = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/" + userID);
        mConversations.addChildEventListener(new ConversationManagerEventListener());
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Interfaccia per la comunicazione IFC
     */
    public interface ConversationFragmentCommunicatorInterface {

        /**
         * Fornisce All'activity tutte le informazioni essenziali della conversazione
         * selezionata dall'utente.
         *
         * @param counterpart ID utente controparte nella conversazione
         * @param nome        nome controparte nella conversazione
         */
        void getClickedConversationInfo(String conversationName, String counterpart, String nome);
    }

    /**
     * La classe implementa il Listener per l'ascolto delle conversazioni
     * dell'utente.
     */
    class ConversationManagerEventListener extends ChildListenerAdapter {

        ConversationManagerEventListener() {
        }

        /**
         * Il metodo, ricevute le informazioni su una conversazione, verifica che la conversazione sia attiva o
         * meno.
         * Se la conversazione risulta attiva viene predisposto lo spazio
         * delle strutture dati per contenerne le informazioni, vengono creati
         * dei riferimenti con gli opportuni Listener per l'ascolto delle
         * informazioni sul conversante e sull'ultimo messaggio.
         *
         * @param dataSnapshot informazioni sulla conversazione
         * @param s            stringa sulle informazioni precedenti
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            ChatStatus status = dataSnapshot.getValue(ChatStatus.class);
            String str = dataSnapshot.getKey();

            if (true == status.messaging) {

                infoList.add(new ConversationInfo());
                usersList.add("");
                int row = infoList.size() - 1;
                ConversationInfo info = new ConversationInfo();
                info.counterpartID = str;
                info.conversationLastMessage = new Message("", "", "", true);
                info.conversationName = status.convname;
                infoList.set(row, info);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/chat/cards/" + str);
                ref.addListenerForSingleValueEvent(new UserInfoListener(row));
                refs.add(ref);

                ref = FirebaseDatabase.getInstance().getReference("/chat/" + status.convname);
                ref.limitToLast(1).addChildEventListener(new LastMessageListener(row));
                refs.add(ref);
            }
        }

    }

    /**
     * La classe implementa il Listener per l'ascolto delle informazioni di un utente con
     * il quale si sta intrattenendo una conversazione.
     */
    class UserInfoListener extends ValueListenerAdapter {

        /**
         * l'attributo rappresenta l'indice della posizione nella quale si
         * devono inserire le informazioni dell'utente per le quali il
         * Listener è in ascolto.
         */
        private int row;

        public UserInfoListener(int row) {
            this.row = row;
        }

        /**
         * Il metodo, ricevute le informazioni utente le
         * inserisce nelle varie strutture dati in accordo con
         * l'indice contenuto nell'attributo row.
         *
         * @param dataSnapshot dati utente
         */
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserCard card = dataSnapshot.getValue(UserCard.class);
            usersList.set(row, card.user_name);
            ConversationInfo info = infoList.get(row);
            info.counterpartName = card.user_name;
            infoList.set(row, info);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * La classe implementa il Listener per l'ascolto dell'ultimo
     * messaggio all'interno di una conversazione.
     */
    class LastMessageListener extends ChildListenerAdapter {

        /**
         * l'attributo rappresenta l'indice della posizione nella quale si
         * deve inserire l'ultimo messaggio una volta ricevuto.
         */
        private int row;

        LastMessageListener(int row) {
            this.row = row;
        }

        /**
         * Il metodo, ricevuto l'ultimo messaggio lo inserisce
         * nell'opportuna struttura dati in accordo con l'indice contenuto
         * nell'attributo row.
         *
         * @param dataSnapshot dati utente
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);
            ConversationInfo info = infoList.get(row);
            info.conversationLastMessage = message;
            infoList.set(row, info);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * La classe modella l'Adapter per la ListView contente le conversazioni.
     */
    public class ConversationAdapter extends ArrayAdapter<String> {

        /**
         * Il costruttore invoca il costruttore della classe base ArrayAdapter
         * e utilizza come parametri il riferimento all'Activity corrente,
         * l'identificativo del Layout degli elementi/conversazioni della lista,
         * l'identificativo, all'interno del Layout del parametro precedente, della
         * TextView da far valorizzare dal comportamento di default dell'Adapter e
         * la lista contenente le informazioni con cui valorizzare il campo per ogni
         * singolo elemento della lista.
         */
        ConversationAdapter() {
            super(getActivity(), R.layout.conversations_item, R.id.textView_name, usersList);
        }

        /**
         * Il metodo e' un metodo di cui viene fatto l'override allo
         * scopo di inserire ulteriore contenuto, oltre alla stringa di default,
         * all'interno dell'elemento della lista.
         *
         * Nel metodo vengono eseguite le seguenti operazioni:
         * - creato il riferimento di tipo ImageView e valorizzato con l'indirizzo
         * al quale è presente la foto dell'utente.
         * - creato il riferimento di tipo TextView e valorizzato con un primo
         * 'ultimo messagio'.
         * - Effettuati alcuni controlli sul messaggio, nella fattispecie:
         * - se il messaggio non è mai stato letto e il destinatario è l'utente
         * corrente, allora si imposta il grassetto, altrimenti si imposta
         * l'assenza di decorazioni.
         *
         * @param position    posizione
         * @param ConvertView view
         * @param parent      view genitore
         * @return view valorizzata
         */
        @Override
        public View getView(int position, View ConvertView, ViewGroup parent) {
            View row = super.getView(position, ConvertView, parent);

            ConversationInfo info = infoList.get(position);

            ImageView photo = (ImageView) row.findViewById(R.id.ImageView_userphoto);
            photo.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_person_white));

            TextView lsmsg = (TextView) row.findViewById(R.id.textView_last_message);
            Message message = info.conversationLastMessage;
            if ((message.readed == false) && !message.senderUID.equals(userID)) {
                lsmsg.setTypeface(null, Typeface.BOLD_ITALIC);
            } else {
                lsmsg.setTypeface(null, Typeface.NORMAL);
            }
            lsmsg.setText(message.message);
            return row;
        }
    }
}