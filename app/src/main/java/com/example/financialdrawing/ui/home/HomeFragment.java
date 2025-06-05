package com.example.financialdrawing.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialdrawing.AuthActivity;
import com.example.financialdrawing.CreateNewOperationActivity;
import com.example.financialdrawing.DataSamples.Operation;
import com.example.financialdrawing.OperationsAdapter;
import com.example.financialdrawing.R;
import com.example.financialdrawing.constants.FirebaseConstants;
import com.example.financialdrawing.databinding.FragmentHomeBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    private TextView balanceTextView;

    private Button createNewOperationButton;
    private Button testButton;

    private String userId;

    private Spinner typeSpinner;
    private Spinner periodSpinner;

    private RecyclerView operationRecyclerView;
    private OperationsAdapter adapterRecyclerView;

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

        createNewOperationButton = binding.createNewOperationButton;

        typeSpinner = binding.typeSpinner;
        periodSpinner = binding.periodSpinner;

        operationRecyclerView = binding.operationRecyclerView;
        operationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterRecyclerView = new OperationsAdapter();
        operationRecyclerView.setAdapter(adapterRecyclerView);


        userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "null";

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


        ArrayAdapter<CharSequence> adapterTypeSpinner = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.type_options,  // Массив: "Все операции", "Доходы", "Расходы"
                android.R.layout.simple_spinner_item
        );
        adapterTypeSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapterTypeSpinner);


        ArrayAdapter<CharSequence> adapterPeriodSpinner = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.period_options,  // Массив: "День", "Неделя", "Месяц", "Год"
                android.R.layout.simple_spinner_item
        );
        adapterPeriodSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(adapterPeriodSpinner);




        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getFilteredOperations(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getFilteredOperations(0);
            }
        });




        getOperationsFromFirebaseWithListener();




        binding.testButton.setOnClickListener(v -> {
            /*Toast.makeText(requireContext(), "ТЕСТ НАЖИМАЕТСЯ", Toast.LENGTH_SHORT).show();
            firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                    .whereEqualTo("uid", userId)
                    .whereEqualTo("income", true)  // Сначала все where
                    .orderBy("createdAt", Query.Direction.DESCENDING)  // Потом orderBy
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FilterDebug", "Найдено документов: " + task.getResult().size());
                            for (DocumentSnapshot doc : task.getResult()) {
                                Log.d("FilterDebug", "Doc: " + doc.getData());
                            }
                        } else {
                            Log.e("FilterDebug", "Ошибка: ", task.getException());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FilterDebug", "Полная ошибка:", e);
                    });*/

        });


        return root;
    }


    private void getOperationsFromFirebaseWithListener() {
        firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .whereEqualTo("uid", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // можно включить при необходимости
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка считывания операций из Firestore", e);
                        Toast.makeText(requireContext(),"Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Operation> operations = querySnapshot.toObjects(Operation.class);
                        adapterRecyclerView.setOperations(operations);
                    }
                });
    }


    private void getFilteredOperations(int filterType) {
        Log.d("FilterDebug", "UserID: " + userId + ", FilterType: " + filterType);
        Query query = firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .whereEqualTo("uid", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Добавляем фильтр по типу если нужно
        if (filterType == 1) { // Доходы
            query = query.whereEqualTo("income", true);
        } else if (filterType == 2) { // Расходы
            query = query.whereEqualTo("income", false);
        }

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка фильтрации операций", e);
                return;
            }
            if (querySnapshot != null) {
                List<Operation> operations = querySnapshot.toObjects(Operation.class);
                adapterRecyclerView.setOperations(operations);
            }
        });
        /*
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FilterDebug", "Query results count: " + task.getResult().size());
                for (DocumentSnapshot doc : task.getResult()) {
                    Log.d("FilterDebug", "Doc: " + doc.getData());
                }
            } else {
                Log.e("FilterDebug", "Error: ", task.getException());
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}