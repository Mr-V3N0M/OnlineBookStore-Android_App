package com.fabiosomaglia.onlinebookstore.fragments;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.fabiosomaglia.onlinebookstore.R;
import com.fabiosomaglia.onlinebookstore.UserSessionManager;
import com.fabiosomaglia.onlinebookstore.activity.LoginActivity;
import com.fabiosomaglia.onlinebookstore.model.Book;
import com.fabiosomaglia.onlinebookstore.model.Booking;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class RiepilogoPrenotazioneFragment extends Fragment {

    String username;
    JSONObject json = new JSONObject();
    Gson gson = new Gson();
    UserSessionManager session = LoginActivity.getSessionLogin();
    String urlLogin = LoginActivity.urlLogin;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_riepilogo_prenotazione, container, false);

        // Prende le informazioni dell'utente dalla sessione
        HashMap<String, String> user = session.getUserDetails();
        // Prendo lo Username che mi servira' nella servlet
        username = user.get(UserSessionManager.KEY_USERNAME);

        json.put("action", "riepilogoPrenotazione");
        json.put("Username", username);
        new RiepilogoOrdine().execute(urlLogin, json.toString());

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Riepilogo ordine");
    }


    private class RiepilogoOrdine extends AsyncTask<String, Void, String> {

        String data = "";
        HttpURLConnection httpURLConnection = null;

        @Override
        protected String doInBackground(String... params) {

            Log.i("TAG", params[0]);
            Log.i("TAG", params[1]);

            try {
                httpURLConnection = (HttpURLConnection) new URL(urlLogin).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setDoOutput(true);

                // Here we send the json
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(json.toString());
                wr.flush();
                wr.close();

                // Here we receive the json
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();

                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            Log.i("TAG", data);

            JSONParser parser = new JSONParser();
            Booking riepilogo = null;
            gson.toJson(data);

            try {

                // Trasformo la stringa "data" in un jsonObject in modo da estrarne l'id del libro e l'utente che lo ha richiesto
                Object obj = parser.parse(data);
                JSONObject contenutoRiepilogo = (JSONObject) obj;
                Book book = gson.fromJson(contenutoRiepilogo.get("book").toString(), Book.class);

                // Ora che ho estrapolato la data, inserisco il contenuto del json nella classe Booking
                riepilogo = new Booking(Integer.parseInt(contenutoRiepilogo.get("bookingId").toString()), book, contenutoRiepilogo.get("userId").toString(), null, null);


                TextView nameTextView = view.findViewById(R.id.txt_titolo);
                nameTextView.setText(riepilogo.getBook().getTitle());
                TextView authorTextView = view.findViewById(R.id.txt_autore);
                authorTextView.setText(riepilogo.getBook().getAuthor());
                TextView publisherTextView = view.findViewById(R.id.txt_editore);
                publisherTextView.setText(riepilogo.getBook().getPublisher());
                TextView idTextView = view.findViewById(R.id.txt_id_libro);
                idTextView.setText(Integer.toString(riepilogo.getBook().getBookId()));
                TextView prestitoTextView = view.findViewById(R.id.txt_id_prestito);
                prestitoTextView.setText(Integer.toString(riepilogo.getBookingId()));
                TextView inizio_prestitoTextView = view.findViewById(R.id.txt_inizio_prestito);
                inizio_prestitoTextView.setText(contenutoRiepilogo.get("startBooking").toString());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}