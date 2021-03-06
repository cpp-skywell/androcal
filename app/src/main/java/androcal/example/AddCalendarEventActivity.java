package androcal.example;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androcal.provider.EventsDO;
import androcal.provider.EventsDAO;
import cpp_skywell.androcal.R;

public class AddCalendarEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar_event);
    }

    public void onStart() {
        super.onStart();
        final EditText eventNameEdit = (EditText) findViewById(R.id.txtEventName);
        final EditText startDate = (EditText) findViewById(R.id.txtDateStart);
        final EditText endDate = (EditText) findViewById(R.id.txtDateEnd);
        final EditText startTime = (EditText) findViewById(R.id.txtTimeStart);
        final EditText endTime = (EditText) findViewById(R.id.txtTimeEnd);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dialog = new DateDialog();
                dialog.setDate(startDate);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                eventNameEdit.clearFocus();
                dialog.show(ft, "DatePicker");
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dialog = new DateDialog();
                dialog.setDate(endDate);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                eventNameEdit.clearFocus();
                dialog.show(ft, "DatePicker");
            }
        });
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeDialog dialog = new TimeDialog();
                dialog.setEventTime(startTime);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                eventNameEdit.clearFocus();
                dialog.show(ft, "TimePicker");
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeDialog dialog = new TimeDialog();
                dialog.setEventTime(endTime);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                eventNameEdit.clearFocus();
                dialog.show(ft, "TimePicker");
            }
        });
        eventNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    public void onClickAddEventToCalendar(View view) {
        final EditText eventNameEdit = (EditText) findViewById(R.id.txtEventName);
        final EditText evenStartDateEdit = (EditText) findViewById(R.id.txtDateStart);
        final EditText evenStartTimeEdit = (EditText) findViewById(R.id.txtTimeStart);
        final EditText evenEndDateEdit = (EditText) findViewById(R.id.txtDateEnd);
        final EditText evenEndTimeEdit = (EditText) findViewById(R.id.txtTimeEnd);

        String eventName = eventNameEdit.getText().toString();
        String evenStart = evenStartDateEdit.getText().toString() + " " + evenStartTimeEdit.getText().toString();
        String evenEnd = evenEndDateEdit.getText().toString() + " " + evenEndTimeEdit.getText().toString();

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        try {
            Date eventStartDate = formatter.parse(evenStart);
            Date eventEndDate = formatter.parse(evenEnd);
            EventsDO event = new EventsDO();

            event.setName(eventName);
            event.setStart(eventStartDate);
            event.setEnd(eventEndDate);
            event.setRefId("");
            event.setSource(EventsDO.Source.NONE);
            event.setStatus(EventsDO.STATUS_NORMAL);
            event.setDirty(true);

//            EventsDAO eventsDAO = EventsDAOFactory.create(this.getApplicationContext());
            EventsDAO eventsDAO = new EventsDAO(this.getContentResolver());
            long rowId = eventsDAO.add(event);

            // DEBUG custom fields
//            event.setId(rowId);
//            event.setCustomValue("ckey1", "cvalue1");
//            event.setCustomValue("ckey2", "cvalue2");
//            eventsDAO.addCustomFields(event);

            showMessage(rowId, view);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(long rowId, View view) {
        String msg = "";
        if (rowId > 0) {
            msg = "You have successfully added event!";
        } else {
            msg = "Something went wrong, please try again.";
        }
        Snackbar messageSnackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        messageSnackbar.show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
