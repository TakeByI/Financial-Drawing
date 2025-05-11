package com.example.financialdrawing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialdrawing.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    TextView emailTextView;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(v -> {
                Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }

        );
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //ТУТ ВЗАИМОДЕЙСТВУЕМ С ВЕРХНЕЙ ПАНЕЛЬКОЙ С ИНФОЙ ОБ АККАУНТЕ
        View headerView = navigationView.getHeaderView(0);
        emailTextView = headerView.findViewById(R.id.textViewEmail);
        if (firebaseAuth.getCurrentUser() != null && emailTextView != null) {
            emailTextView.setText(firebaseAuth.getCurrentUser().getEmail());
        }
        Log.d("My Log", firebaseAuth.getCurrentUser().getDisplayName() != null ? firebaseAuth.getCurrentUser().getDisplayName() : "null");
        Log.d("My Log", firebaseAuth.getCurrentUser().getPhoneNumber() != null ? firebaseAuth.getCurrentUser().getPhoneNumber() : "null");
        //Log.d("My Log", firebaseAuth.getCurrentUser().getPhotoUrl().toString().isEmpty() ? firebaseAuth.getCurrentUser().getPhotoUrl().toString() : "null");

        //КНОПКА ДЛЯ ВЫХОДА ИЗ АККАУНТА В МЕНЮШКЕ
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                firebaseAuth.signOut();
                if (firebaseAuth.getCurrentUser() == null) {
                    // Переход на экран входа
                    startActivity(new Intent(MainActivity.this, AuthActivity.class));
                    finish();
                    return true;
                }
                else {
                    // Ошибка выхода (крайне редкий случай)
                    Toast.makeText(getApplicationContext(), "Ошибка выхода", Toast.LENGTH_SHORT).show();
                }
            }
            // Остальная логика навигации
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}