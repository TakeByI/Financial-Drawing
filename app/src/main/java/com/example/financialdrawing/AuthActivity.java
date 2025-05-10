package com.example.financialdrawing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    Button nextActivityButton;
    Button signUpButton;
    Button signInButton;
    Button signOutButton;

    EditText email;
    EditText password;

    TextView ifPasswordOrEmailEmptyTextView;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.d("MY LOG", "User already signed in: " + auth.getCurrentUser().getEmail());
            // Если пользователь уже авторизован, сразу переходим на MainActivity
            startActivity(new Intent(AuthActivity.this, MainActivity.class));
            finish(); // Закрываем AuthActivity
            return; // Прерываем выполнение onCreate, так как переход уже сделан
        } else {
            Log.d("MY LOG", "No user signed in");
        }

        nextActivityButton = findViewById(R.id.buttonNextActivity);
        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInButton);
        signOutButton = findViewById(R.id.signOutButton);

        ifPasswordOrEmailEmptyTextView = findViewById(R.id.ifPasswordOrEmailEmptyTextView);

        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);


        if (auth.getCurrentUser() != null) {
            Log.d("MY LOG", auth.getCurrentUser().getEmail());
        } else {
            Log.d("MY LOG", "Email - null");
        }


        signUpButton.setOnClickListener(v -> {
            signUp(auth, email.getText().toString(), password.getText().toString());

        });

        signInButton.setOnClickListener(v -> {
            signIn(auth, email.getText().toString(), password.getText().toString());

        });

        signOutButton.setOnClickListener(v -> {
            signOut(auth);
        });

        nextActivityButton.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        });
    }

    private void signUp(FirebaseAuth auth, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Log.d("My log", "EMAIL AND PASSWORD CANNOT BE EMPTY");
            ifPasswordOrEmailEmptyTextView.setText("Email и пароль не могут быть пустыми!");
            ifPasswordOrEmailEmptyTextView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        ifPasswordOrEmailEmptyTextView.setVisibility(View.GONE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {
                        if (task.isSuccessful()) {
                            Log.d("MyLog", "Sign Up successful!");
                            Toast.makeText(getApplicationContext(), "Sign Up successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AuthActivity.this, MainActivity.class));
                            finish(); // Закрываем AuthActivity, чтобы нельзя было вернуться назад
                        } else {
                            Log.d("MyLog", "Sign Up failure!");
                            Toast.makeText(getApplicationContext(), "Sign Up failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                })
                .addOnFailureListener(e ->  {
                        Log.d("My log", e.getMessage() != null ? e.getMessage() : "Sign up ERROR");
                        ifPasswordOrEmailEmptyTextView.setText(e.getMessage());
                        ifPasswordOrEmailEmptyTextView.setVisibility(View.VISIBLE);
                    }
                );
    }

    private void signIn(FirebaseAuth auth, String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Log.d("My log", "EMAIL AND PASSWORD CANNOT BE EMPTY");
            ifPasswordOrEmailEmptyTextView.setText("Email и пароль не могут быть пустыми!");
            ifPasswordOrEmailEmptyTextView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        ifPasswordOrEmailEmptyTextView.setVisibility(View.GONE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task ->  {
                    if (task.isSuccessful()) {
                        Log.d("MyLog", "Sign In successful!");
                        Toast.makeText(getApplicationContext(), "Sign In successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AuthActivity.this, MainActivity.class));
                        finish(); // Закрываем AuthActivity, чтобы нельзя было вернуться назад
                    } else {
                        Log.d("MyLog", "Sign In failure!");
                        Toast.makeText(getApplicationContext(), "Sign In failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->  {
                    Log.d("My log", e.getMessage() != null ? e.getMessage() : "Sign In ERROR");
                    ifPasswordOrEmailEmptyTextView.setText(e.getMessage());
                    ifPasswordOrEmailEmptyTextView.setVisibility(View.VISIBLE);
                });


    }

    private void signOut(FirebaseAuth auth) {
        auth.signOut();
    }
}