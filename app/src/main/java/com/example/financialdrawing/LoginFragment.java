package com.example.financialdrawing;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {
    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private TextView errorTextView, goToRegisterTextView;
    private Button signInButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        auth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        errorTextView = view.findViewById(R.id.errorTextView);
        goToRegisterTextView = view.findViewById(R.id.goToRegisterTextView);

        signInButton = view.findViewById(R.id.signInButton);

        // Кнопка входа
        signInButton.setOnClickListener(v -> signIn());

        // Переход к регистрации
        goToRegisterTextView.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((AuthActivity) getActivity()).loadFragment(new RegisterFragment());
            }
        });

        return view;
    }






    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Log.d("My log", "EMAIL AND PASSWORD CANNOT BE EMPTY");
            Toast.makeText(getContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            errorTextView.setText("Email and password cannot be empty!");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }
        errorTextView.setVisibility(View.GONE);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        if (getActivity() != null) getActivity().finish();
                    } else {
                        Log.d("MyLog", "Sign In failure!");
                        Toast.makeText(getContext(), "Sign In failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        errorTextView.setText(task.getException() != null ? task.getException().getMessage() : "Sign In failed");
                        errorTextView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->  {
                    Log.d("My log", e.getMessage() != null ? e.getMessage() : "Sign In ERROR");
                    errorTextView.setText(e.getMessage());
                    errorTextView.setVisibility(View.VISIBLE);
                });
    }
}