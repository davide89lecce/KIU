package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;
//import com.gambino_serra.KIU.chat.model.ChatStatus;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica richiesta di coda pendente (lato Helper).
 */
public class NotificationDetailsHelper extends DialogFragment {

    //private DatabaseReference mDatabase;
    TextView title;
    Context context;

    public NotificationDetailsHelper() {}

    /**
     * onCreate della Dialog e Set dello stato dei bottoni nella UI(Button).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

       // mDatabase = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/");

        final Bundle bundle = getArguments();
        ((NotificationHelper) getActivity()).updateRichieste();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        context = getActivity();
        title = new TextView(context);
        title.setText(R.string.notif_details_helper_title);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25F);
        builder.setCustomTitle(title);

        builder.setView(inflater.inflate(R.layout.fragment_notification_details, null))

                //accetta la richiesta di coda e la memorizza nel database di altervista.
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        String url = "http://www.davideantonio2.altervista.org/accetta_richiesta.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            //Se la richiesta è stata inserita correttamente crea la conversazione relativa alla chat
                            @Override
                            public void onResponse(String response) {
                                Log.d("Richiesta","Richiesta accettata");
                                //createConversation();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                               Log.d("errore", "Problemi di connessione");
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("ID_richiesta", bundle.get("ID").toString());
                                return params;
                            }
                        };
                        MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

                        dialog.dismiss();
                        ((NotificationHelper) getActivity()).onResume();
                    }
                })

                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })

                //rifiuta la richiesta di coda e la memorizza nel database di altervista.
                .setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        String url = "http://www.davideantonio2.altervista.org/rifiuta_richiesta.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                               Log.d("Richiesta","Richiesta rifiutata");
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("errore", "Problemi di connessione");
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("ID_richiesta", bundle.get("ID").toString());
                                return params;
                            }
                        };
                        MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

                        dialog.cancel();
                        ((NotificationHelper) getActivity()).onResume();
                    }
                });

        return builder.create();
    }

    /**
     * Il metodo crea la conversazione con il Kiuer.
     */
    /*
    private void createConversation(){
        Bundle bundle = getArguments();
        String my_uid = bundle.get("uid_helper").toString();
        String other_uid = bundle.get("uid_kiuer").toString();
        ChatStatus status = new ChatStatus(false,false, my_uid+other_uid);
        //mDatabase.child(my_uid).child(other_uid).setValue(status);
        mDatabase.child(other_uid).child(my_uid).setValue(status);
    }
    */

    /**
     *  Inizializza la Dialog con i dati ricevuti dal Bundle
     */
    @Override
    public void onStart(){
        super.onStart();
        TextView testo = (TextView) this.getDialog().findViewById(R.id.richiesta);
        Bundle bundle = getArguments();
        testo.setText(bundle.get("text").toString());
    }
}