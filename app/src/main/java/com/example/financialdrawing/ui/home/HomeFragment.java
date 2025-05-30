package com.example.financialdrawing.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financialdrawing.AuthActivity;
import com.example.financialdrawing.CreateNewOperationActivity;
import com.example.financialdrawing.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    private TextView balanceTextView;

    private Button createNewOperationButton;

    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        balanceTextView = binding.balanceTextView;

        userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "null";

        createNewOperationButton = binding.createNewOperationButton;



        Toast.makeText(getContext(), firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();

        //повесили на balanceTextView постоянный listener, который обновляет баланс на экране при его изменении в firestore
        balanceTextView.setText("Баланс: ");
        firebaseFirestore.collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        balanceTextView.setText("Ошибка загрузки");
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String balance = documentSnapshot.getString("balance");
                        balanceTextView.setText("Баланс: " + (balance != null ? balance : "0") + " ₽");
                    }
                });





        createNewOperationButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), CreateNewOperationActivity.class));
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}