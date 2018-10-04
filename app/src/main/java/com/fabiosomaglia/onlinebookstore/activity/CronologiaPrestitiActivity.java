package com.fabiosomaglia.onlinebookstore.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.fabiosomaglia.onlinebookstore.R;
import com.fabiosomaglia.onlinebookstore.TabPagerAdapter;
import com.fabiosomaglia.onlinebookstore.UserSessionManager;

import java.util.HashMap;

public class CronologiaPrestitiActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    UserSessionManager session = LoginActivity.getSessionLogin();
    String name, surname, email, username;
    DrawerLayout drawer;
    NavigationView navigationView;
    final String TAG = this.getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cronologia_prestiti);
        Toolbar toolbar = findViewById(R.id.toolbar_cronologia);
        setSupportActionBar(toolbar);


        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager pager = findViewById(R.id.viewpager_tabs);

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(tabPagerAdapter);
        tabLayout.setupWithViewPager(pager);


        // Gestisce l'apertura e la chiusura del Navigation Drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Prende le informazioni dalla sessione dell'utente
        HashMap<String, String> user = session.getUserDetails();
        name = user.get(UserSessionManager.KEY_NAME);
        surname = user.get(UserSessionManager.KEY_SURNAME);
        email = user.get(UserSessionManager.KEY_EMAIL);
        username = user.get(UserSessionManager.KEY_USERNAME);

        // Inserisco le informazioni dell'utente nell'header del Navigation Drawer
        navigationView = findViewById(R.id.nav_view_crono);
        View hView = navigationView.getHeaderView(0);
        TextView headerName = hView.findViewById(R.id.tv_user_name_header);
        headerName.setText(name);
        TextView headerSurname = hView.findViewById(R.id.tv_user_surname_header);
        headerSurname.setText(surname);
        TextView headerEmail = hView.findViewById(R.id.tv_user_email_header);
        headerEmail.setText(email);
        navigationView.setNavigationItemSelectedListener(this);

        // Controlla se l'utente è registrato
        if (session.checkLogin())
            finish();

    }


    boolean twice; // Ho creato questa variabile per controllare se il tasto indietro viene cliccato 2 volte di fila

    @Override
    public void onBackPressed() {
        // Se il drawer era aperto allora semplicemente lo chiude
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // Se il drawer era chiuso controllo se il tasto indietro viene cliccato 2 volte di fila
            Log.d(TAG, "click");

            if (twice == true) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // chiude tutto cio che era aperto nell'app
                startActivity(intent);
                finish();
                System.exit(0); // chiude l'app senza far uscire errori
            }

            Log.d(TAG, "twice: " + twice);
            Toast.makeText(this, "Premi di nuovo per chiudere l'app", Toast.LENGTH_SHORT).show();
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

    private void displaySelectedScreen(int itemId) {

        Intent intent = new Intent(getApplicationContext(), NavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //per eliminare la transizione

        // Visualizzo la schermata selezionata
        switch (itemId) {
            case R.id.nav_dashboard:
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
                break;
            case R.id.nav_prenotaLibro:
                intent.putExtra("view", "prenota"); // Passo al NavigationDrawerActivity il fragment selezionato e da visualizzare
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
                break;
            case R.id.nav_restituisciLibro:
                intent.putExtra("view", "restituisci");
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
                break;
            case R.id.nav_cronologiaPrestiti:
                Intent cronologia = new Intent(getApplicationContext(), CronologiaPrestitiActivity.class);
                cronologia.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                cronologia.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //per eliminare la transizione
                startActivityForResult(cronologia, 0);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
                break;
            case R.id.nav_logout:
                AlertDialog.Builder prenotaDialog = new AlertDialog.Builder(this);
                prenotaDialog.setMessage("Sei sicuro della tua scelta?");
                prenotaDialog.setCancelable(false);
                prenotaDialog.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancello la sessione utente e redirect alla MainActivity
                        session.logoutUser();
                    }
                })
                        .setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = prenotaDialog.create();
                alert.setTitle("Logout");
                alert.show();

                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Richiamo il metodo displaySelectedScreen() e gli passo l'id dell'item selezionato
        displaySelectedScreen(item.getItemId());
        return true;
    }
}
