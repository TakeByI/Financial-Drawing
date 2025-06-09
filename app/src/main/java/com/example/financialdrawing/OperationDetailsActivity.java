package com.example.financialdrawing;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financialdrawing.DataSamples.Operation;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OperationDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private String operationId;
    private Operation currentOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation_details);

        firebaseFirestore = FirebaseFirestore.getInstance();

        // Получаем ID операции из Intent
        operationId = getIntent().getStringExtra("OPERATION_ID");

        ImageButton editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> {
            if (currentOperation != null) {
                openEditActivity();
            }
        });

        if (operationId != null) {
            loadOperationDetails(operationId);
        }

    }

    private void openEditActivity() {
        Intent intent = new Intent(this, EditOperationActivity.class);
        intent.putExtra("OPERATION_ID", operationId);
        //intent.putExtra("OPERATION_DATA", currentOperation);
        startActivityForResult(intent, 1002);
    }

    private void loadOperationDetails(String operationId) {
        firebaseFirestore.collection("operations")
                .document(operationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentOperation = documentSnapshot.toObject(Operation.class);
                        if (currentOperation != null) {
                            displayOperationDetails(currentOperation);
                        }
                    }
                });
    }

    private void displayOperationDetails(Operation operation) {
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView amountTextView = findViewById(R.id.amountTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        TextView typeTextView = findViewById(R.id.typeTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        titleTextView.setText(operation.getTitle());
        amountTextView.setText(operation.getAmount() + " ₽");
        dateTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(operation.getCreatedAt().toDate()));
        categoryTextView.setText(operation.getCategory());
        typeTextView.setText(operation.isIncome() ? "Доход" : "Расход");
        descriptionTextView.setText(operation.getDescription());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 && resultCode == RESULT_OK) {
            // Обновляем данные после редактирования
            if (operationId != null) {
                loadOperationDetails(operationId);
            }
        }
    }
}