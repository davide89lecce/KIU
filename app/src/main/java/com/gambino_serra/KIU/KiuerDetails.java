package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
//import com.gambino_serra.KIU.chat.MessagesActivity;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica coda assegnata all'helper (lato Helper).
 */
public class KiuerDetails extends DialogFragment {

   // private DatabaseReference mDatabase;
    TextView title;
    Context context;

    private String my_uid = "";
    private String other_uid ="";

    public KiuerDetails() {}

    /**
     * onCreate della Dialog e Set dei comportamenti dei Button.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        context = getActivity();
        title = new TextView(context);
        title.setText(R.string.detail_kiuer_title);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25F);
        builder.setCustomTitle(title);

        //Imposta il testo dei bottoni della Dialog
        String btnAlertDialog = "";
        if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_terminated))) {
            btnAlertDialog = getResources().getString(R.string.chiudi_coda);
        } else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_in_progress))) {
            btnAlertDialog = getResources().getString(R.string.termina_coda);
        } else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_not_started))) {
            btnAlertDialog = getResources().getString(R.string.inizia_coda);
        }

        builder.setTitle(R.string.kiuer_details);
        builder.setView(inflater.inflate(R.layout.fragment_kiuer_details, null))

                .setPositiveButton(btnAlertDialog, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        //Se la coda è terminata --> avvia la Dialog relativa alla chiusura della coda
                        //Se la coda non è avviata --> avvia la coda registrando lo stato sul DB
                        //Se la coda è avviata --> termina la coda registrando lo stato sul DB
                        if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_terminated))) {

                        } else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_not_started))) {

                            String url = "http://www.davideantonio2.altervista.org/inizio_coda_helper.php";
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("volley","inizio coda helper");
                                }
                            }, ((HelperHomeActivity) getActivity())) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("ID_richiesta", bundle.get("ID").toString());
                                    return params;
                                }
                            };
                            MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

                        } else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_in_progress))) {

                            String url = "http://www.davideantonio2.altervista.org/fine_coda_helper.php";
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("volley","fine coda helper");

                                }
                            }, ((HelperHomeActivity) getActivity())) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("ID_richiesta", bundle.get("ID").toString());
                                    return params;
                                }
                            };
                            MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                            DialogFragment newFragment = new HelperCloseQueue();
                            newFragment.setArguments(bundle);
                            newFragment.show(getFragmentManager(), "HelperCloseQueue");
                        }

                        dialog.dismiss(); // dismette positivo o neutrale
                        ((HelperHomeActivity) getActivity()).onResume();
                    }
                })
                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel(); // dismette con rifiuto
                    }
                });
        return builder.create(); //renderizza la dialog
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  mDatabase = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/");
    }

    /**
     *  Il metodo inizializza la Dialog con i dati ricevuti dal Bundle.
     */
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        TextView text = (TextView) this.getDialog().findViewById(R.id.dettagli);
        TextView nome = (TextView) this.getDialog().findViewById(R.id.text_nome_1);
        Button luogo = (Button) this.getDialog().findViewById(R.id.luogo);
        Button contatta = (Button) this.getDialog().findViewById(R.id.contatta_kiuer);
        nome.setText(bundle.get("nome").toString());
        text.setText(bundle.get("text").toString());

        luogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getArguments();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + bundle.get("latitudine").toString() + "," + bundle.get("longitudine").toString());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        contatta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getUserInfo();
            }
        });
    }

    /**
     * Il metodo permette di ricevere le informazioni dell'utente Kiuer.
     */
    /*
    private void getUserInfo(){
        mDatabase = FirebaseDatabase.getInstance().getReference("/chat/cards/" + other_uid);
        mDatabase.addListenerForSingleValueEvent(new KiuerDetails.UserInfoListener());
    }
    */

    /**
     * Il metodo avvia la conversazione con l'altro utente (Kiuer).
     *
     * @param card
     */
    /*
    private void openConversation(UserCard card) {
        Intent in = new Intent(getActivity().getApplicationContext(), MessagesActivity.class);
        in.putExtra("counterpartID", card.userID);
        in.putExtra("counterpartName", card.user_name);
        in.putExtra("convname", my_uid + other_uid);
        startActivity(in);
    }
    */

    /**
     * La classe modella un listener per l'ascolto delle
     * informazione dell'utente.
     */
    /*
    class UserInfoListener extends ValueListenerAdapter {

        public UserInfoListener(){}

        /**
         * Il metodo riceve i dati utente e ne avvia la gestione.
         *
         * @param dataSnapshot info dati utente
         */
    /*
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserCard card = dataSnapshot.getValue(UserCard.class);
           // openConversation(card);
        }
    }
    */
}