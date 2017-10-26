package com.example.maciejwikira.prgnv2;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Bundle arg = getArguments();
        int id = arg.getInt("Field_ID");
        EditText field = (EditText) getActivity().findViewById(id);

        String monthText;
        String dayText;

        if(month + 1 < 10 ){
            monthText = "-0" + Integer.toString(month + 1);
        }else{
            monthText = "-" + Integer.toString(month + 1);
        }

        if(day < 10){
            dayText = "-0" + Integer.toString(day);
        }else{
            dayText = "-" + Integer.toString(day);
        }

        field.setText(Integer.toString(year) + monthText + dayText);
    }
}