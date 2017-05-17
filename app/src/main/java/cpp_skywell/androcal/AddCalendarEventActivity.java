package cpp_skywell.androcal;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cpp_skywell.androcal.ContentProvider.EventsDO;
import cpp_skywell.androcal.ContentProvider.Google.GEventsDAO;

public class AddCalendarEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar_event);
    }

    public void onStart(){
        super.onStart();
        final EditText startDate = (EditText)findViewById(R.id.txtDateStart);
        final EditText endDate = (EditText)findViewById(R.id.txtDateEnd);
        final EditText startTime = (EditText)findViewById(R.id.txtTimeStart);
        final EditText endTime = (EditText)findViewById(R.id.txtTimeEnd);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dialog = new DateDialog();
                dialog.setDate(startDate);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, "DatePicker");
            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog dialog = new DateDialog();
                dialog.setDate(endDate);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, "DatePicker");
            }
        });
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeDialog dialog = new TimeDialog();
                dialog.setEventTime(startTime);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, "TimePicker");
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeDialog dialog = new TimeDialog();
                dialog.setEventTime(endTime);
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                dialog.show(ft, "TimePicker");
            }
        });
    }

    public void onClickAddEventToCalendar(View view){
        final EditText eventNameEdit = (EditText)findViewById(R.id.txtEventName);
        final EditText evenStartDateEdit = (EditText)findViewById(R.id.txtDateStart);
        final EditText evenStartTimeEdit = (EditText)findViewById(R.id.txtTimeStart);
        final EditText evenEndDateEdit = (EditText)findViewById(R.id.txtDateEnd);
        final EditText evenEndTimeEdit = (EditText)findViewById(R.id.txtTimeEnd);

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

            GEventsDAO gEventsDAO = GEventsDAO.getInstance();
            gEventsDAO.add(event);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
