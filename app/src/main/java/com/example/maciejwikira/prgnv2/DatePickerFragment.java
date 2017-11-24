package com.example.maciejwikira.prgnv2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

// Fragment obsługujący wybór daty przez użytkownika
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Stworzenie nowej instancji DatePickerDialog i zwrócenie go
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        // Pobierz uchwyt do pola daty w interfejsie użytkownika
        Bundle arg = getArguments();
        int id = arg.getInt("Field_ID");
        EditText field = (EditText) getActivity().findViewById(id);

        // Deklaracja zmiennych
        String monthText;
        String dayText;

        // Formatowanie miesiąca
        if(month + 1 < 10 ){
            monthText = "-0" + Integer.toString(month + 1);
        }else{
            monthText = "-" + Integer.toString(month + 1);
        }

        // Formatowanie dnia
        if(day < 10){
            dayText = "-0" + Integer.toString(day);
        }else{
            dayText = "-" + Integer.toString(day);
        }

        // Ustawienie zawartości pola daty w interfejsie użytkownika
        field.setText(Integer.toString(year) + monthText + dayText);
    }
}