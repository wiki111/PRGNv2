package com.example.maciejwikira.prgnv2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

// Fragment wyświetla kalendarz.
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    // Interfejs deklarujący metodę, pozwalającą na przekazanie
    // wybranej daty do odpowiedniego elementu interfejsu użytkownika.
    public interface onDateSetListener {
        void dateSet(String date, String field);
    }

    onDateSetListener dateSetListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            dateSetListener = (onDateSetListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement onDateSetListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Domyślnie ustawiana jest bieżąca data.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        String monthText;
        String dayText;

        // Formatowanie miesiąca.
        if(month + 1 < 10 ){
            monthText = "-0" + Integer.toString(month + 1);
        }else{
            monthText = "-" + Integer.toString(month + 1);
        }

        // Formatowanie dnia.
        if(day < 10){
            dayText = "-0" + Integer.toString(day);
        }else{
            dayText = "-" + Integer.toString(day);
        }

        // Ustawienie zawartości pola daty w interfejsie użytkownika.
        dateSetListener.dateSet(Integer.toString(year) + monthText + dayText, getArguments().getString("Field"));
    }
}