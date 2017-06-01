package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica richiesta di coda pendente (lato Kiuer)
 */
public class Kiuer_NotificationDetails extends DialogFragment {

    public Kiuer_NotificationDetails() {}

    /**
     * onCreate della Dialog e Set dello stato dei bottoni nella UI(Button).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(getResources().getString(R.string.details_kiuer));
        builder.setView(inflater.inflate(R.layout.fragment_notification_details, null))


                //Se la richiesta è stata accettata/rifiutata dall'Helper viene salvata come notificata dal Kiuer per eliminarla dalla ListView
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        if(bundle.get("stato_richiesta").toString().equals("1") || bundle.get("stato_richiesta").toString().equals("2")) {

                            String url = "http://www.davideantonio2.altervista.org/kiuer_richiestaNotificata.php";
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.d("Richiesta","Impostata come Notificata nel DB correttamente");
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Richiesta","Errore nella comunicazione con il DB");
                                }
                            })
                            {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("ID_richiesta", bundle.get("id").toString());
                                    return params;
                                }
                            };
                            Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                        }
                            dialog.dismiss();
                            ((Kiuer_Notification) getActivity()).onResume();
                    }
                });
        return builder.create();
    }

    /**
     *  Inizializza la Dialog con i dati ricevuti dal Bundle.
     */
    @Override
    public void onStart(){
        super.onStart();
        TextView testo = (TextView) this.getDialog().findViewById(R.id.richiesta);
        TextView rating = (TextView) this.getDialog().findViewById(R.id.textView8);
        RatingBar rating_kiuer = (RatingBar) this.getDialog().findViewById(R.id.rating_kiuer);

        Bundle bundle = getArguments();
        String status;

        if(bundle.get("stato_richiesta").toString().equals("1")){
            status = getResources().getString(R.string.request_accepted);
            }
        else if(bundle.get("stato_richiesta").toString().equals("2")){
            status = getResources().getString(R.string.request_rejected);
            }
        else{
            status = getResources().getString(R.string.pending_confirmation);
            }
        testo.setText(status + "\n\n" + getResources().getString(R.string.name) + " " + bundle.get("nome").toString() + "\n\n"
                + getResources().getString(R.string.time) + " " + bundle.get("orario").toString().substring(0,5) + "\n\n"
                + getResources().getString(R.string.place) + " " + bundle.get("luogo").toString() + "\n\n"
                + getResources().getString(R.string.description) + " " + bundle.get("descrizione").toString());

        rating.setVisibility(View.GONE);
        rating_kiuer.setVisibility(View.GONE);
    }
}