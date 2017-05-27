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
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica coda assegnata all'helper (lato Helper).
 */
public class Kiuer_Details extends DialogFragment {

    TextView title;
    Context context;

    public Kiuer_Details() {}

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
            }
        else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_in_progress))) {
            btnAlertDialog = getResources().getString(R.string.termina_coda);
            }
        else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_not_started))) {
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
                        if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_terminated))) {}
                        else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_not_started))) {
                            String url = "http://www.davideantonio2.altervista.org/helper_inizioCoda.php";
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.d("volley","inizio coda helper");
                                    }

                                }, ((Helper_Home) getActivity())) {

                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("ID_richiesta", bundle.get("ID").toString());
                                    return params;
                                    }
                            };
                            Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                            }
                        else if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_in_progress))) {
                            String url = "http://www.davideantonio2.altervista.org/helper_fineCoda.php";
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    Log.d("volley","fine coda helper");
                                    }

                            }, ((Helper_Home) getActivity()))
                            {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("ID_richiesta", bundle.get("ID").toString());
                                    return params;
                                    }
                            };
                            Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                            DialogFragment newFragment = new Helper_CloseQueue();
                            newFragment.setArguments(bundle);
                            newFragment.show(getFragmentManager(), "Helper_CloseQueue");
                        }

                        dialog.dismiss(); // dismette positivo o neutrale
                        ((Helper_Home) getActivity()).onResume();
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

        contatta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telefono = "1234567890";
                //Bundle bundle = getArguments();
                //telefono = bundle.get("telefono").toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

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

    }
}