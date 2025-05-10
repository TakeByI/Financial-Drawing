package com.example.financialdrawing.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financialdrawing.AuthActivity;
import com.example.financialdrawing.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        auth = FirebaseAuth.getInstance();
        Toast.makeText(getContext(), auth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();

        binding.buttonLogOut.setOnClickListener(v -> {
            auth.signOut();

            if (auth.getCurrentUser() == null) {
                // Выход успешен
                Toast.makeText(getContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

                // Переход на экран авторизации
                Intent intent = new Intent(getContext(), AuthActivity.class);
                startActivity(intent);
                requireActivity().finish(); // Закрываем текущую активность, чтобы нельзя было вернуться назад
            } else {
                // Ошибка выхода (крайне редкий случай)
                Toast.makeText(getContext(), "Ошибка выхода", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}