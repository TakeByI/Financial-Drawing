package com.example.financialdrawing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.financialdrawing.DataSamples.Operation;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditOperationActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private String operationId;
    private Operation operation;

    private EditText titleEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton;
    private AutoCompleteTextView categoryAutoCompleteTextView;
    private EditText amountEditText;
    private TextView dateTextView;
    private EditText descriptionEditText;
    private Button saveButton;

    private ArrayAdapter<String> adapterIncome;
    private ArrayAdapter<String> adapterExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_operation);

        firebaseFirestore = FirebaseFirestore.getInstance();
        operationId = getIntent().getStringExtra("OPERATION_ID");

        initViews();
        setupCategoryAdapters();
        loadOperationData();

        // Устанавливаем начальный адаптер (доход или расход)
        if (operation != null && operation.isIncome()) {
            categoryAutoCompleteTextView.setAdapter(adapterIncome);
        } else {
            categoryAutoCompleteTextView.setAdapter(adapterExpense);
        }

        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        incomeRadioButton = findViewById(R.id.incomeRadioButton);
        categoryAutoCompleteTextView = findViewById(R.id.categoryAutoCompleteTextView);
        amountEditText = findViewById(R.id.amountEditText);
        dateTextView = findViewById(R.id.dateTextView);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupCategoryAdapters() {
        // Аналогично CreateNewOperationActivity
        String[] categoriesExpense = {"Здоровье", "Досуг", "Дом", "Общепит", "Образование", "Подарки", "Продукты", "Семья", "Спорт", "Транспорт", "Другое"};
        String[] categoriesIncome = {"Зарплата", "Подарок", "Вклад", "Другое"};

        adapterIncome = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoriesIncome);
        adapterExpense = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoriesExpense);

        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.incomeRadioButton) {
                categoryAutoCompleteTextView.setAdapter(adapterIncome);
            } else {
                categoryAutoCompleteTextView.setAdapter(adapterExpense);
            }
            // Показываем список категорий при изменении типа
            categoryAutoCompleteTextView.showDropDown();
        });

        // Добавляем обработчик клика для показа списка
        categoryAutoCompleteTextView.setOnClickListener(v -> {
            categoryAutoCompleteTextView.showDropDown();
        });

        // Добавляем обработчик изменения текста
        categoryAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    categoryAutoCompleteTextView.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void populateData() {
        if (operation != null) {
            titleEditText.setText(operation.getTitle());
            if (operation.isIncome()) {
                typeRadioGroup.check(R.id.incomeRadioButton);
            } else {
                typeRadioGroup.check(R.id.expenseRadioButton);
            }
            categoryAutoCompleteTextView.setText(operation.getCategory());
            amountEditText.setText(operation.getAmount());
            dateTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(operation.getCreatedAt().toDate()));
            descriptionEditText.setText(operation.getDescription());
        }
    }

    private void saveChanges() {
        String title = titleEditText.getText().toString().trim();
        String amount = amountEditText.getText().toString().trim();
        String category = categoryAutoCompleteTextView.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Введите название");
            return;
        }

        if (amount.isEmpty()) {
            amountEditText.setError("Введите сумму");
            return;
        }

        if (category.isEmpty()) {
            categoryAutoCompleteTextView.setError("Выберите категорию");
            return;
        }

        // Валидация и сохранение изменений
        Operation updatedOperation = new Operation(
                operation.getUid(),
                titleEditText.getText().toString(),
                typeRadioGroup.getCheckedRadioButtonId() == R.id.incomeRadioButton,
                categoryAutoCompleteTextView.getText().toString(),
                amountEditText.getText().toString(),
                operation.getCreatedAt(), // Дата остается прежней
                descriptionEditText.getText().toString()
        );

        firebaseFirestore.collection("operations")
                .document(operationId)
                .set(updatedOperation)
                .addOnSuccessListener(aVoid -> {
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadOperationData() {
        if (operationId != null) {
            firebaseFirestore.collection("operations")
                    .document(operationId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            operation = documentSnapshot.toObject(Operation.class);
                            if (operation != null) {
                                populateData();
                            }
                        }
                    });
        }
    }
}