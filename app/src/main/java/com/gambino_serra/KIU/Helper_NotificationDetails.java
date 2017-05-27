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
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica richiesta di coda pendente (lato Helper).
 */
public class Helper_NotificationDetails extends DialogFragment {

    TextView title;
    Context context;

    public Helper_NotificationDetails() {}

    /**
     * onCreate della Dialog e Set dello stato dei bottoni nella UI(Button).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        ((Helper_Notification) getActivity()).updateRichieste();
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

                        String url = "http://www.davideantonio2.altervista.org/helper_accettaRichiesta.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("Richiesta","Richiesta accettata");
                            }

                        }, new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                               Log.d("errore", "Problemi di connessione");
                            }
                        })
                        {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("ID_richiesta", bundle.get("ID").toString());
                                return params;
                            }
                        };
                        Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                        dialog.dismiss();
                        ((Helper_Notification) getActivity()).onResume();
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

                        String url = "http://www.davideantonio2.altervista.org/helper_rifiutaRichiesta.php";
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
                        Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                        dialog.cancel();
                        ((Helper_Notification) getActivity()).onResume();
                    }
                });
        return builder.create();
    }

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