package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gambino_serra.KIU.R;
import com.gambino_serra.KIU.chat.MessagesActivity;
import com.gambino_serra.KIU.chat.listener.ValueListenerAdapter;
import com.gambino_serra.KIU.chat.model.UserCard;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica richiesta di coda (lato Kiuer).
 */
public class HelperDetails extends DialogFragment {

    TextView title;
    Context context;
   // private DatabaseReference mDatabase;
    private String my_uid = "";
    private String other_uid = "";

    public HelperDetails() {
    }

    /**
     * onCreate della Dialog e Set dello stato dei bottoni nella UI(Button).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        context = getActivity();
        title = new TextView(context);
        title.setText(R.string.detail_helper_title);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25F);
        builder.setCustomTitle(title);
        builder.setView(inflater.inflate(R.layout.fragment_helper_details, null ))

                .setPositiveButton(R.string.chiudi_coda, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        //Se la coda è terminata avvia la Dialog relativa alla chiusura della coda altrimenti avvisa l'utente che non è possibile chiuderla
                        if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_terminated))) {

                            DialogFragment newFragment = new KiuerCloseQueue();
                            newFragment.setArguments(bundle);
                            newFragment.show(getFragmentManager(), "KiuerCloseQueue");

                            dialog.dismiss(); // dismette positivo o neutrale

                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.queue_not_finished, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel(); // dismette con rifiuto
                    }
                });

        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // mDatabase = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/");
    }

    /**
     * Il metodo inizializza la Dialog con i dati ricevuti dal Bundle
     */
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        TextView text = (TextView) this.getDialog().findViewById(R.id.dettagli2);
        TextView nome = (TextView) this.getDialog().findViewById(R.id.text_nome_1h);
        Button contatta = (Button) this.getDialog().findViewById(R.id.contatta_helper);
        nome.setText(bundle.get("nome").toString());
        text.setText(bundle.get("text").toString());

        my_uid = bundle.getString("uid_kiuer");
        other_uid = bundle.getString("uid_helper");

        contatta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getUserInfo();
            }
        });
    }

    /**
     * Il metodo permette di ricevere le informazioni dell'utente Helper.
     */
    /*
    private void getUserInfo() {
        mDatabase = FirebaseDatabase.getInstance().getReference("/chat/cards/" + other_uid);
        mDatabase.addListenerForSingleValueEvent(new UserInfoListener());
    }
    */

    /**
     * Il metodo avvia la conversazione con l'altro utente (Helper).
     *
     * @param card
     */
    private void openConversation(UserCard card) {
        Intent in = new Intent(getActivity().getApplicationContext(), MessagesActivity.class);
        in.putExtra("counterpartID", card.userID);
        in.putExtra("counterpartName", card.user_name);
        in.putExtra("convname", other_uid + my_uid);
        startActivity(in);
    }

    /**
     * La classe modella un listener per l'ascolto delle
     * informazione dell'utente.
     */
    class UserInfoListener extends ValueListenerAdapter {

        public UserInfoListener() {
        }

        /**
         * Il metodo riceve i dati utente e ne avvia la gestione.
         *
         * @param dataSnapshot info dati utente
         */
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserCard card = dataSnapshot.getValue(UserCard.class);
            openConversation(card);
        }
    }
}
