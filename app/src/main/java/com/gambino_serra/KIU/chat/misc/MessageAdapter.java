package com.gambino_serra.KIU.chat.misc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.model.MessageEntity;

import java.util.List;

/**
 * La classe modella l'ambiente di visualizzazione della chat.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private List<MessageEntity> messageList;
    private String userID;

    public MessageAdapter(List<MessageEntity> messageList, String userID) {
        this.messageList = messageList;
        this.userID = userID;
    }

    /**
     * Il metodo gestisce il posizionamento dei messaggi attraverso
     * l'instanziazione del ViewHolder.
     *
     * @param parent
     * @param viewType
     * @return ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;
        MyViewHolder holder;
        switch (viewType) {
            case RIGHT:
                view = inflater.inflate(R.layout.message_item_right, parent, false);
                break;
            case LEFT:
                view = inflater.inflate(R.layout.message_item_left, parent, false);
                break;
            default:
                break;
        }
        holder = new MyViewHolder(view);
        return holder;


    }

    /**
     * Il metodo definisce il tipo di View in funzione del mittente.
     *
     * @param position
     * @return tipo della vista
     */
    @Override
    public int getItemViewType(int position) {
        int value = -1;
        if (messageList.get(position).message.senderUID.equals(userID)) {
            value = RIGHT;
        } else {
            value = LEFT;
        }
        return value;
    }

    /**
     * Il metodo gestisce l'inserimento del messaggio all'interno di un ViewHolder.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MessageEntity message = messageList.get(position);
        holder.message.setText(message.message.message);
        holder.time.setText(message.message.timestamp);
    }

    /**
     * Il metodo ritorna il numero di messaggi scambiati nella chat
     * tra due interlocutori.
     *
     * @return numero messaggi
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * La classe modella un oggetto della vista e dei suo metadati all'interno del RecyclerView.
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView message, time;

        public MyViewHolder(View view) {
            super(view);
            message = (TextView) view.findViewById(R.id.textView_messageArea);
            time = (TextView) view.findViewById(R.id.textView_sendingTime);
        }
    }
}