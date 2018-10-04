package com.fabiosomaglia.onlinebookstore.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.fabiosomaglia.onlinebookstore.R;
import com.fabiosomaglia.onlinebookstore.UserSessionManager;
import com.fabiosomaglia.onlinebookstore.model.User;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginActivity extends AppCompatActivity {
    EditText username, password;
    Button btnLogin;
    JSONObject jsonLoginData = new JSONObject();
    private static UserSessionManager session;
    Gson gson = new Gson();
    User userInfo;
    final String TAG = this.getClass().getName();

    public static String urlLogin = "http://10.0.2.2:8080"; // per l'emulatore
    //public static String urlLogin = "http://192.168.1.72:8080"; // per device da terminale scrivere "ifconfig" sta vicino ad inet


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.txtUser);
        password = findViewById(R.id.txtPass);
        btnLogin = findViewById(R.id.button_login);

        // Crea la freccia in alto a sinistra nella nostra app che ci permette di ritornare indietro nella main activity
        // La dichiarazione dell'attività parent è stata fatta nel manifest con "android:parentActivityName"
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Creo una sessione utente
        session = new UserSessionManager(getApplicationContext());

        // Quando clicco il bottone di login:
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // execute method invokes doInBackground() where we open a Http URL connection using the given Servlet URL
                // and get output response from InputStream and return it.
                try {
                    // Getting Username and Password tipped by the user
                    jsonLoginData.put("action", "login");
                    jsonLoginData.put("username", username.getText().toString());
                    jsonLoginData.put("password", password.getText().toString());
                    new Login().execute(urlLogin, jsonLoginData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Permette alle varie classi di accedere alla sessione utente
     *
     * @return la sessione dell'utente loggato
     */
    public static UserSessionManager getSessionLogin() {
        return session;
    }

    private class Login extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i("TAG", params[0]);
            Log.i("TAG", params[1]);

            String data = "";
            HttpURLConnection httpURLConnection = null;

            try {
                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);

                // Qui invio il json
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();

                // Qui ricevo il json
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

            userInfo = gson.fromJson(data, User.class);
            return data;
        }

        // Controllo se l'utente è registrato
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result);

            if (result.contains("failure")) { // Se l'utente non esiste
                Toast.makeText(LoginActivity.this, "Username e/o password non corretti", Toast.LENGTH_SHORT).show();
                // In questo modo cancello ciò che c'è scritto nel campo password
                password.setText("");
            } else {
                session.createUserLoginSession(userInfo.getName(),
                        userInfo.getEmail(),
                        userInfo.getSurname(),
                        userInfo.getUsername());

                // Creo la nuova activity perche il login è avvenuto con successo
                Intent loginSuccessful = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
                loginSuccessful.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginSuccessful);
                finish();
            }
        }
    }


    boolean twice; // Ho creato questa variabile per controllare se il tasto indietro viene cliccato 2 volte di fila

    @Override
    public void onBackPressed() {
        // Se clicco due volte il tasto indietro di android l'app si chiude
        Log.d(TAG, "click");

        if (twice) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // chiude tutto cio che era aperto nell'app
            startActivity(intent);
            finish();
            System.exit(0); // chiude l'app senza far uscire errori
        }

        Log.d(TAG, "twice: " + twice);
        Toast.makeText(LoginActivity.this, "Premi di nuovo per chiudere l'app", Toast.LENGTH_SHORT).show();
        // Ora se l'utente clicca nuovamente il tasto indietro chiudo l'app (2000 vuol dire 2 secondi)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
                Log.d(TAG, "twice: " + twice);
            }
        }, 2000);
        twice = true;
    }
}



