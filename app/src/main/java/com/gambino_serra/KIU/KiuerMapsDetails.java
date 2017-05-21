package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.gambino_serra.KIU.R;

import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella l'interazione per l'inoltro della richiesta di coda.
 */
public class KiuerMapsDetails extends DialogFragment {

    public TextView ora_richiesta;

    public KiuerMapsDetails() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.fragment_kiuer_details_maps, null))

                .setTitle(getResources().getString(R.string.send_request).toUpperCase() + " " + bundle.get("cognome").toString().toUpperCase() + " " + bundle.get("nome").toString().toUpperCase())

                .setPositiveButton(R.string.invia_richiesta, new DialogInterface.OnClickListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        if (!bundle.get("ora_richiesta").equals(getResources().getString(R.string.set_request_time))) {
                            //Registra nel database di altervista la richiesta inviata dal Kiuer
                            String url = "http://www.kiu.altervista.org/invia_richiesta.php";

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, ((KiuerMaps) getActivity()), ((KiuerMaps) getActivity())) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("email_kiuer", bundle.get("email_kiuer").toString());
                                    params.put("email_helper", bundle.get("email_helper").toString());
                                    params.put("orario", bundle.get("ora_richiesta").toString());
                                    params.put("coordinate_latitudine", bundle.get("coordinate_latitudine").toString());
                                    params.put("coordinate_longitudine", bundle.get("coordinate_longitudine").toString());
                                    params.put("luogo", bundle.get("luogo").toString());
                                    return params;
                                }
                            };

                            MySingleton.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.set_request_time, Toast.LENGTH_SHORT).show();
                            DialogFragment newFragment = new KiuerMapsDetails();
                            newFragment.setArguments(bundle);
                            newFragment.show(getFragmentManager(), "KiuerMaps");
                        }

                    }
                })
                .setNeutralButton(R.string.annulla, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    /**
     * Inizializzazione dei campi che visualizzano i dettagli del Helper.
     */
    @Override
    public void onStart() {
        super.onStart();
        TextView testo = (TextView) this.getDialog().findViewById(R.id.msg_richiesta);
        RatingBar rating = (RatingBar) this.getDialog().findViewById(R.id.rating_helper);
        ora_richiesta = (TextView) this.getDialog().findViewById(R.id.ora_richiesta);
        Bundle bundle = getArguments();
        rating.setRating(bundle.getFloat("rating") / bundle.getInt("num_feedback"));
        rating.setIsIndicator(true);
        testo.setText(getResources().getString(R.string.hour_rate) + " " + bundle.get("tariffa_oraria") + "€\n" + getResources().getString(R.string.queue_done) + " " + bundle.get("code_effettuate") + "\n"
                + getResources().getString(R.string.start_availability) + " " + bundle.get("inizio_disp").toString().substring(0, 5) + "\n" + getResources().getString(R.string.end_availability) + " " + bundle.get("fine_disp").toString().substring(0, 5) + "\n");

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xBFBFBFBF);
        gd.setSize(200, 90);
        gd.setCornerRadius(10);
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setStroke(7, Color.rgb(170, 170, 170));
        ora_richiesta.setBackground(gd);

        ora_richiesta.setText("   " + bundle.get("ora_richiesta").toString() + "   ");

        ora_richiesta.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getDialog().dismiss();
                ((KiuerMaps) getActivity()).showTimePickerRichiesta();
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

}

