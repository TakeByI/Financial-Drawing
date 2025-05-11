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

public class RegisterFragment extends Fragment {
    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;
    private TextView errorTextView, goToLoginTextView;
    Button signUpButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        auth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);

        errorTextView = view.findViewById(R.id.errorTextView);
        goToLoginTextView = view.findViewById(R.id.goToLoginTextView);

        signUpButton = view.findViewById(R.id.signUpButton);

        // Кнопка регистрации
        signUpButton.setOnClickListener(v -> signUp());

        // Переход к авторизации
        goToLoginTextView.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
            }
        });

        return view;
    }

    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Log.d("My log", "EMAIL AND PASSWORD CANNOT BE EMPTY");
            errorTextView.setText("Email and password cannot be empty!");
            errorTextView.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        errorTextView.setVisibility(View.GONE);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("MyLog", "Sign Up successful!");
                        Toast.makeText(getContext(), "Sign Up successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        if (getActivity() != null) getActivity().finish();
                    } else {
                        Log.d("MyLog", "Sign Up failure!");
                        Toast.makeText(getContext(), "Sign Up failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        errorTextView.setText(task.getException() != null ? task.getException().getMessage() : "Sign Up failed");
                        errorTextView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->  {
                            Log.d("My log", e.getMessage() != null ? e.getMessage() : "Sign up ERROR");
                            errorTextView.setText(e.getMessage());
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                );
    }
}