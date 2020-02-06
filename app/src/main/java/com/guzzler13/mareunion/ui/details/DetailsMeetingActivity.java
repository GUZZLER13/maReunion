package com.guzzler13.mareunion.ui.details;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.guzzler13.mareunion.R;
import com.guzzler13.mareunion.di.DI;
import com.guzzler13.mareunion.model.Meeting;
import com.guzzler13.mareunion.service.MeetingApiService;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class DetailsMeetingActivity extends AppCompatActivity {

    private MeetingApiService mApiService = DI.getMeetingApiService();
    private Meeting meeting;
    private Spinner mMeetingRoomsSpinner;
    private TextView mMeetingName;
    private TextView mDateEdit;
    private TextView mBeginTimeEdit;
    private TextView mEndTimeEdit;
    private AutoCompleteTextView mParticipantsAutoCompleteTextView;
    private ChipGroup mParticipantsChipGroup;
    private Button mButtonSave;
    private Button addParticipantButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_meeting);

        mMeetingRoomsSpinner = findViewById(R.id.spinner);
        mMeetingName = findViewById(R.id.meetingName_editText);
        mDateEdit = findViewById(R.id.dateEdit_TextView);
        mBeginTimeEdit = findViewById(R.id.beginTimeEdit_TextView);
        mEndTimeEdit = findViewById(R.id.endTimeEdit_TextView);
        mParticipantsAutoCompleteTextView = findViewById(R.id.participant_autoCompleteTextView);
        mParticipantsChipGroup = findViewById(R.id.chipGroup);
        addParticipantButton = findViewById(R.id.addParticipant_button);
        mButtonSave = findViewById(R.id.saveMetting_button);


        //Spinner to choose meeting room :
        ArrayList<String> meetingRooms = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.meeting_rooms_arrays)));
        meetingRooms.add(0, getString(R.string.choose_meetingRoom));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, meetingRooms) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from Spinner, first item will be use for hint
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);// Set the hint text color gray
                    tv.setTextSize(18);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        mMeetingRoomsSpinner.setAdapter(spinnerAdapter);
        mMeetingRoomsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position > 0 && view != null) {
                    TextView tv = (TextView) view;
                    tv.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {


            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //AutocompleteTextView + chips to add the participants :
        ArrayAdapter<CharSequence> adapterParticipants = ArrayAdapter.createFromResource(this,
                R.array.participants_arrays, android.R.layout.simple_dropdown_item_1line);
        mParticipantsAutoCompleteTextView.setAdapter(adapterParticipants);
        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mParticipantsAutoCompleteTextView.getText() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.hideSoftInputFromWindow(mParticipantsAutoCompleteTextView.getWindowToken(), 1);

                    String participant;
                    participant = mParticipantsAutoCompleteTextView.getText().toString();
                    // ensure it's an e-mail
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(participant).matches() || participant.isEmpty()) {
                        Toast.makeText(DetailsMeetingActivity.this, R.string.its_not_an_email_message
                                , Toast.LENGTH_SHORT).show();
                    } else {

                        final Chip chip = addChip(participant);
                        chip.setChipBackgroundColorResource(R.color.colorGreen);
                        mParticipantsChipGroup.addView(chip);
                        chip.setOnCloseIconClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mParticipantsChipGroup.removeView(chip);
                            }
                        });
                    }
                }
                mParticipantsAutoCompleteTextView.setText("");
            }
        });


        int id = getIntent().getIntExtra("id", -1);

        // click à partir d'un item
        if (id != -1) {
            meeting = mApiService.getMeetings().get(id);

            int position = spinnerAdapter.getPosition(meeting.getMeetingRoom().getmNameRoom());
            mMeetingRoomsSpinner.setSelection(position);

            String mParticipants = meeting.getParticipants();
            String nameReunion = meeting.getName();
            String dateTime = meeting.getDateBegin().toString("dd/MM");
            String hourBegin = meeting.getDateBegin().toString("kk:mm");
            String hourEnd = meeting.getDateEnd().toString("kk:mm");


            mMeetingName.setText(nameReunion);
            mDateEdit.setText(dateTime);
            mDateEdit.setTextColor(getResources().getColor(R.color.colorBlack));
            mBeginTimeEdit.setText(hourBegin);
            mBeginTimeEdit.setTextColor(getResources().getColor(R.color.colorBlack));
            mEndTimeEdit.setText(hourEnd);
            mEndTimeEdit.setTextColor(getResources().getColor(R.color.colorBlack));
            String[] parts = mParticipants.split(", ");
            for (String part : parts) {
                final Chip chip = addChip(part);
                mParticipantsChipGroup.addView(chip);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mParticipantsChipGroup.removeView(chip);
                    }
                });
            }


        } else {
//             click à partir du FLOAT (création ou modif meeting)

//            mButtonSave.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//
//                    Meeting meeting = new Meeting(
//                            25,
//                            mMeetingName.getText().toString(),
//                            new DateTime(mBeginTimeEdit.getText().)
//
//
//
//
//                            );
//
//
//                    mApiService.addMeeting(meeting);
//                }
//            });

            mDateEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dateHandle();

                }
            });

            mBeginTimeEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    BegintimeHandle();
                }
            });

            mEndTimeEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    EndtimeHandle();
                }
            });





        }
    }

    private Chip addChip(String participant) {
        final Chip chip = new Chip(mParticipantsChipGroup.getContext());
        chip.setChipBackgroundColorResource(R.color.colorGrey);
        chip.setText(participant);
        chip.setChipIcon(getDrawable(R.drawable.ic_person_pin_black_18dp));
        chip.setCheckable(false);
        chip.setClickable(true);
        chip.setCloseIconVisible(true);
        return chip;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void BegintimeHandle(){

        final Calendar calendar1 = Calendar.getInstance();

        int HOUR = calendar1.get(Calendar.HOUR_OF_DAY);
        int MINUTE = calendar1.get(Calendar.MINUTE);

        boolean is24hourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                String timeString = hour +" "+ minute;
                mBeginTimeEdit.setText(timeString);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR,hour);
                calendar1.set(Calendar.MINUTE,minute);

                CharSequence timeTexte = DateFormat.format("HH:mm", calendar1);

                mBeginTimeEdit.setText(timeTexte);

            }
        },HOUR,MINUTE,true);

        timePickerDialog.show();

    }

    public void EndtimeHandle(){

        final Calendar calendar1 = Calendar.getInstance();

        int HOUR = calendar1.get(Calendar.HOUR_OF_DAY);
        int MINUTE = calendar1.get(Calendar.MINUTE);

        boolean is24hourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                String timeString = hour +" "+ minute;
                mEndTimeEdit.setText(timeString);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.HOUR,hour);
                calendar1.set(Calendar.MINUTE,minute);

                CharSequence timeTexte = DateFormat.format("HH:mm", calendar1);

                mEndTimeEdit.setText(timeTexte);

            }
        },HOUR,MINUTE,is24hourFormat);

        timePickerDialog.show();

    }

    public void dateHandle() {

        final Calendar calendar = Calendar.getInstance();

        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                String dateString = year + " " + month + " " + " " + date;
                mDateEdit.setText(dateString);

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(Calendar.YEAR, year);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.DATE, date);

                CharSequence dateTexte = DateFormat.format("dd/MM", calendar1);

                mDateEdit.setText(dateTexte);
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();

    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home :
//                Intent intent = new Intent(this, MeetingListActivity.class);
//                startActivity(intent);
//        }
//        return super.onOptionsItemSelected(item);
//    }
}

