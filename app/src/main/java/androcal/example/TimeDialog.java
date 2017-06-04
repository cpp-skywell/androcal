package androcal.example;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeDialog extends DialogFragment {
    EditText eventTime;

    public TimeDialog() {
    }

    public void setEventTime(EditText eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR);
        int mins = calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventTime.setText(hourOfDay + ":" + minute);
            }
        }, hours, mins, false);
    }
}
