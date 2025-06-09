package com.example.financialdrawing.ui.home;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialdrawing.CreateNewOperationActivity;
import com.example.financialdrawing.DataSamples.Operation;
import com.example.financialdrawing.OperationDetailsActivity;
import com.example.financialdrawing.OperationsAdapter;
import com.example.financialdrawing.R;
import com.example.financialdrawing.constants.FirebaseConstants;
import com.example.financialdrawing.databinding.FragmentHomeBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private TextView balanceTextView;
    private TextView chosenPeriodTextView;

    private Button createNewOperationButton;
    private Button testButton;
    private Button customPeriodButton;

    private String userId;

    private Spinner typeSpinner;
    private Spinner periodSpinner;

    private RecyclerView operationRecyclerView;
    private OperationsAdapter adapterRecyclerView;

    private Timestamp customStartDate;
    private Timestamp customEndDate;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        balanceTextView = binding.balanceTextView;
        chosenPeriodTextView = binding.chosenPeriodTextView;

        createNewOperationButton = binding.createNewOperationButton;
        customPeriodButton = binding.customPeriodButton;

        typeSpinner = binding.typeSpinner;
        periodSpinner = binding.periodSpinner;

        operationRecyclerView = binding.operationRecyclerView;
        operationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterRecyclerView = new OperationsAdapter();
        adapterRecyclerView.setOnOperationClickListener(operationId -> {
            Intent intent = new Intent(getActivity(), OperationDetailsActivity.class);
            intent.putExtra("OPERATION_ID", operationId);
            startActivity(intent);
        });
        operationRecyclerView.setAdapter(adapterRecyclerView);

        //получение айди текущего юзера
        userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "null";

        chosenPeriodTextView.setText("Период: все время");

        Toast.makeText(getContext(), firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();

        updateUserBalance();
        getOperationsFromFirebaseWithListener();

        //повесили на balanceTextView постоянный listener, который обновляет баланс на экране при его изменении в firestore
        balanceTextView.setText("Баланс: ");
        firebaseFirestore.collection(FirebaseConstants.COLLECTION_USERS)
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        balanceTextView.setText("Ошибка загрузки");
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String balanceStr = documentSnapshot.getString("balance");
                        balanceTextView.setText("Баланс: " + (balanceStr != null ? balanceStr : "0") + " ₽");

                        // Проверяем, отрицательный ли баланс
                        try {
                            BigDecimal balance = new BigDecimal(balanceStr != null ? balanceStr : "0");
                            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                                // Красный цвет для отрицательного баланса
                                balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                            } else {
                                // Стандартный цвет (можно задать свой или использовать theme)
                                balanceTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.primary_text_dark));
                            }
                        } catch (NumberFormatException e) {
                            Log.e("BalanceFormat", "Ошибка формата баланса", e);
                        }
                    }
                });

        //запуск активности для создания новой операции по кнопке
        createNewOperationButton.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(getContext(), CreateNewOperationActivity.class),
                    1001 // Request code
            );
        });

        //инициализация спиннеров для фильтрации необходимыми значениями
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

        //обработка выбора типа операции в спиннере фильтрации по типу операции
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int periodPosition = periodSpinner.getSelectedItemPosition();
                getFilteredOperations(position, periodPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                int periodPosition = periodSpinner.getSelectedItemPosition();
                getFilteredOperations(0, periodPosition);
            }
        });

        //обработка выбора периода в спиннере фильтрации по периоду
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int typePosition = typeSpinner.getSelectedItemPosition();
                getFilteredOperations(typePosition, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getFilteredOperations(0, 0);
            }
        });

        //вызов метода выбора кастомного периода по кнопке
        customPeriodButton.setOnClickListener(v -> showDateRangePickerDialog());


        //КНОПКА ДЛЯ ТЕСТИРОВАНИЯ
        binding.testButton.setOnClickListener(v -> {

        });


        return root;
    }




    //Метод для получения отфильтрованных операций
    private void getFilteredOperations(int filterType, int periodType) {
        // Если выбран кастомный период (спиннер на "Все время" и есть customStartDate)
        if (periodType == 0 && customStartDate != null && customEndDate != null) {
            loadCustomPeriodOperations();
            return;
        }

        // Сбрасываем кастомный период при выборе стандартного
        if (periodType != 0) {
            customStartDate = null;
            customEndDate = null;

            // Обновляем текст выбранного периода
            String[] periodOptions = getResources().getStringArray(R.array.period_options);
            chosenPeriodTextView.setText("Период: " + periodOptions[periodType]);
        }


        Log.d(FirebaseConstants.LOG_FIRESTORE, "UserID: " + userId + ", FilterType: " + filterType + ", Period: " + periodType);

        Calendar calendar = Calendar.getInstance();
        Timestamp startDate;
        Timestamp endDate = new Timestamp(new Date()); // Текущая дата и время

        switch (periodType) {
            case 1: // День
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = new Timestamp(calendar.getTime());
                break;
            case 2: // Неделя
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = new Timestamp(calendar.getTime());
                break;
            case 3: // Месяц
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = new Timestamp(calendar.getTime());
                break;
            case 4: // Год
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = new Timestamp(calendar.getTime());
                break;
            default: // Все время
                calendar.set(1970, Calendar.JANUARY, 1);
                startDate = new Timestamp(calendar.getTime());

        }

        Query query = firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .whereEqualTo("uid", userId)
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .orderBy("createdAt", Query.Direction.DESCENDING);

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
                // Передаем DocumentSnapshot вместо преобразования в объекты Operation
                adapterRecyclerView.setOperations(querySnapshot.getDocuments());
            }
        });


    }

    //метод для выбора кастомного периода
    private void showDateRangePickerDialog() {
        // Получаем текущую дату
        Calendar today = Calendar.getInstance();

        // Создаем DatePickerDialog для начальной даты
        DatePickerDialog startDatePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(year, month, dayOfMonth, 0, 0, 0);
                    customStartDate = new Timestamp(startCal.getTime());

                    // После выбора начальной даты показываем диалог для конечной даты
                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            requireContext(),
                            (view2, year2, month2, dayOfMonth2) -> {
                                Calendar endCal = Calendar.getInstance();
                                endCal.set(year2, month2, dayOfMonth2, 23, 59, 59);
                                customEndDate = new Timestamp(endCal.getTime());

                                // Форматируем даты для отображения
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                                String periodText = "Период: " + sdf.format(customStartDate.toDate()) +
                                        " - " + sdf.format(customEndDate.toDate());
                                chosenPeriodTextView.setText(periodText);

                                // Сбрасываем выбор в спиннере периодов
                                periodSpinner.setSelection(0);

                                // Загружаем операции за выбранный период
                                loadCustomPeriodOperations();
                            },
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH)
                    );

                    // Устанавливаем максимальную дату как сегодня
                    endDatePicker.getDatePicker().setMaxDate(today.getTimeInMillis());

                    // Устанавливаем минимальную дату как выбранную начальную дату
                    endDatePicker.getDatePicker().setMinDate(startCal.getTimeInMillis());

                    endDatePicker.show();
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        // Устанавливаем максимальную дату как сегодня для начальной даты
        startDatePicker.getDatePicker().setMaxDate(today.getTimeInMillis());

        startDatePicker.show();
    }

    //Метод для загрузки операций за кастомный период
    private void loadCustomPeriodOperations() {
        if (customStartDate == null || customEndDate == null) {
            return;
        }

        Query query = firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .whereEqualTo("uid", userId)
                .whereGreaterThanOrEqualTo("createdAt", customStartDate)
                .whereLessThanOrEqualTo("createdAt", customEndDate)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        int typePosition = typeSpinner.getSelectedItemPosition();
        if (typePosition == 1) { // Доходы
            query = query.whereEqualTo("income", true);
        } else if (typePosition == 2) { // Расходы
            query = query.whereEqualTo("income", false);
        }

        query.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка загрузки операций", e);
                return;
            }
            if (querySnapshot != null) {
                adapterRecyclerView.setOperations(querySnapshot.getDocuments());
            }
        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void updateUserBalance() {
        firebaseFirestore.collection(FirebaseConstants.COLLECTION_OPERATIONS)
                .whereEqualTo("uid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        BigDecimal balance = BigDecimal.ZERO;

                        for (DocumentSnapshot document : task.getResult()) {
                            Operation operation = document.toObject(Operation.class);
                            if (operation != null) {
                                BigDecimal amount = new BigDecimal(operation.getAmount());
                                if (operation.isIncome()) {
                                    balance = balance.add(amount);
                                } else {
                                    balance = balance.subtract(amount);
                                }
                            }
                        }

                        // Обновляем баланс в Firestore
                        firebaseFirestore.collection(FirebaseConstants.COLLECTION_USERS)
                                .document(userId)
                                .update("balance", balance.toString())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(FirebaseConstants.LOG_FIRESTORE, "Баланс успешно обновлен");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка обновления баланса", e);
                                });
                    } else {
                        Log.e(FirebaseConstants.LOG_FIRESTORE, "Ошибка получения операций", task.getException());
                    }
                });
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
                        adapterRecyclerView.setOperations(querySnapshot.getDocuments());
                        updateUserBalance();
                    }
                });
    }

    public void refreshData() {
        getOperationsFromFirebaseWithListener();
        updateUserBalance();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Обновляем данные при возврате из CreateNewOperationActivity
            refreshData();
        }
    }

}