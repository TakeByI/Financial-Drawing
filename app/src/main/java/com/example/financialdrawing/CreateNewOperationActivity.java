package com.example.financialdrawing;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.financialdrawing.DataSamples.Operation;
import com.example.financialdrawing.constants.FirebaseConstants;
import com.example.financialdrawing.databinding.ActivityCreateNewOperationBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class CreateNewOperationActivity extends AppCompatActivity {

    private ActivityCreateNewOperationBinding binding;
    private Calendar calendar = Calendar.getInstance();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;

    private RadioGroup typeRadioGroup;
    private RadioButton incomeRadioButton;

    private AutoCompleteTextView categoryAutoCompleteTextView;

    private TextView chosenDateTextView;

    private Button chooseDateButton;
    private Button saveOperationButton;

    private EditText titleOperationEditText;
    private EditText amountEditText;
    private EditText descriptionEditText;


    // Массив значений категорий
    private final static String[] categoriesExpense = {"Здоровье", "Досуг", "Дом", "Общепит", "Образование", "Подарки", "Продукты", "Семья", "Спорт", "Транспорт", "Другое"};
    private final static String[] categoriesIncome = {"Зарплата", "Подарок", "Вклад", "Другое"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNewOperationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // Установка корневого View

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "null";

        //радио элементы
        typeRadioGroup = binding.typeRadioGroup;
        incomeRadioButton = binding.incomeRadioButton;

        //autoCompleteTextView
        categoryAutoCompleteTextView = binding.typeAutoComplete;

        //кнопки
        chooseDateButton = binding.chooseDateButton;
        saveOperationButton = binding.saveOperationButton;

        //textView
        chosenDateTextView = binding.chosenDateTextView;

        //editText
        titleOperationEditText = binding.titleOperationEditText;
        amountEditText = binding.amountEditText;
        descriptionEditText = binding.descriptionEditText;

        // Адаптеры
        ArrayAdapter<String> adapterIncome = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoriesIncome
        );
        ArrayAdapter<String> adapterExpense = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoriesExpense
        );
        //пока хотя бы раз не нажали на радиокнопку, не будут отображаться категории, поэтому базово будут отображаться категории доходов
        categoryAutoCompleteTextView.setAdapter(adapterIncome);


        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Форматируем дату в строку (дд.мм.гггг)
        String formattedDate = String.format(Locale.getDefault(),
                "%02d.%02d.%d", day, month + 1, year);
        calendar.set(year, month, day);
        // Устанавливаем текст в TextView
        chosenDateTextView.setText(formattedDate);


        //при изменении нажатия на кнопки радиогруппы
        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // checkedId — ID выбранной RadioButton
            if (checkedId == R.id.incomeRadioButton) {
                Log.d("My Log RadioGroup", "Выбран доход");
                categoryAutoCompleteTextView.setAdapter(adapterIncome);
            } else if (checkedId == R.id.expenseRadioButton) {
                Log.d("My Log RadioGroup", "Выбран расход");
                categoryAutoCompleteTextView.setAdapter(adapterExpense);
            }
        });


        // 3. Показываем все варианты при клике на списочек с категориями
        categoryAutoCompleteTextView.setOnClickListener(v -> {
            categoryAutoCompleteTextView.showDropDown();
        });



        chooseDateButton.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        saveOperationButton.setOnClickListener(v -> {
            saveOperationToFirebase();
        });



        //проверки для валидации поля ввода с суммой
        amountEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    amountEditText.removeTextChangedListener(this);

                    // Удаляем все символы кроме цифр и точки
                    String cleanString = s.toString().replaceAll("[^\\d.]", "");

                    // Проверяем количество точек
                    int dotCount = cleanString.length() - cleanString.replace(".", "").length();

                    if (dotCount > 1) {
                        // Если больше одной точки, оставляем только первую
                        int dotIndex = cleanString.indexOf(".");
                        cleanString = cleanString.substring(0, dotIndex + 1) +
                                cleanString.substring(dotIndex + 1).replace(".", "");
                    }

                    // Ограничиваем до 2 знаков после точки
                    if (cleanString.contains(".")) {
                        String[] parts = cleanString.split("\\.");
                        if (parts.length > 1 && parts[1].length() > 2) {
                            cleanString = parts[0] + "." + parts[1].substring(0, 2);
                        }
                    }

                    current = cleanString;
                    amountEditText.setText(cleanString);
                    amountEditText.setSelection(cleanString.length());

                    amountEditText.addTextChangedListener(this);
                }
            }
        });
    }

    //отображение окна выбора даты и сохранение даты в textview
    private void showDatePickerDialog() {
        // Получаем текущую дату для начальных значений
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Создаем DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Обработка выбранной даты
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear);
                    chosenDateTextView.setText(formattedDate);
                },
                year, month, day);

        // Дополнительные настройки (необязательно)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Макс. дата - сегодня
        // datePickerDialog.getDatePicker().setMinDate(...); // Можно установить минимальную дату

        datePickerDialog.show();
    }

    private void saveOperationToFirebase() {
        String title = titleOperationEditText.getText().toString();
        boolean isIncome = incomeRadioButton.isChecked();
        String category = categoryAutoCompleteTextView.getText().toString();
        String amount = amountEditText.getText().toString();
        Timestamp createdAt = new Timestamp(calendar.getTime());
        String description = descriptionEditText.getText().toString();

        // Валидация полей
        if (title.isEmpty()) {
            titleOperationEditText.setError("Введите название операции");
            titleOperationEditText.requestFocus();
            return;
        }

        if (amount.isEmpty()) {
            amountEditText.setError("Введите сумму");
            amountEditText.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            categoryAutoCompleteTextView.setError("Выберите категорию");
            categoryAutoCompleteTextView.requestFocus();
            return;
        }

        try {
            // Проверяем, что сумма - корректное число
            double amountValue = Double.parseDouble(amount);
            if (amountValue <= 0) {
                amountEditText.setError("Сумма должна быть больше нуля");
                amountEditText.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            amountEditText.setError("Некорректный формат суммы");
            amountEditText.requestFocus();
            return;
        }

        Operation operation = new Operation(userId,
                title,
                isIncome,
                category,
                amount,
                createdAt,
                description
                );

        firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .add(operation)
                .addOnSuccessListener(documentReference -> {
                    Log.d(FirebaseConstants.LOG_FIRESTORE, "Операция сохранена с ID: " + documentReference.getId());
                    finish(); // Закрываем активити после сохранения
                })
                .addOnFailureListener(e -> {
                    Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка сохранения операции", e);
                });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Очистка Binding для избежания утечек памяти
    }
}