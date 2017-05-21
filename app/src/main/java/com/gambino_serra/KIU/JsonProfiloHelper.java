package com.gambino_serra.KIU;

import com.google.gson.annotations.SerializedName;

/**
 * La classe mappa l'oggetto Json relativo al profilo dell'utente Helper.
 */
public class JsonProfiloHelper {

    @SerializedName("ID")
    public String id;
    @SerializedName("disp_inizio")
    public String disp_inizio;
    @SerializedName("disp_fine")
    public String disp_fine;
    @SerializedName("tariffa_oraria")
    public String tariffa_oraria;
    @SerializedName("pos_latitudine")
    public Double pos_latitudine;
    @SerializedName("pos_longitudine")
    public Double pos_longitudine;
    @SerializedName("disponibile")
    public String disponibile;
    @SerializedName("nome")
    public String nome;
    @SerializedName("rating")
    public float rating;
    @SerializedName("cont_feedback")
    public int cont_feedback;


}
