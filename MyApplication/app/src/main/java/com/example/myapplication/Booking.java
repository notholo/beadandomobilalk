package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Booking extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout timeSlotLayout;
    private DatabaseReference databaseReference;
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 18;
    private static final int TIME_SLOT_INTERVAL = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        calendarView = findViewById(R.id.calendar_view);
        timeSlotLayout = findViewById(R.id.time_slot_layout);
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                showTimeSlots(year, month, dayOfMonth);
            }
        });
    }

    private void showTimeSlots(int year, int month, int dayOfMonth) {
        timeSlotLayout.removeAllViews();
        String dateKey = getDateKey(year, month, dayOfMonth);
        databaseReference.child("appointments").child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> existingTimeSlots = getExistingTimeSlots(dataSnapshot);
                createTimeSlots(year, month, dayOfMonth, existingTimeSlots, dateKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Booking", "Error fetching appointments", databaseError.toException());
            }
        });
    }

    private String getDateKey(int year, int month, int dayOfMonth) {
        return String.format("%d-%d-%d", year, month, dayOfMonth);
    }

    private Set<String> getExistingTimeSlots(DataSnapshot dataSnapshot) {
        Set<String> existingTimeSlots = new TreeSet<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            existingTimeSlots.add(snapshot.getKey());
        }
        return existingTimeSlots;
    }

    private Map<String, Button> timeSlotButtons = new HashMap<>();
    private void createTimeSlots(int year, int month, int dayOfMonth, Set<String> existingTimeSlots, String dateKey) {
        for (int i = START_HOUR * 60; i < END_HOUR * 60; i += TIME_SLOT_INTERVAL) {
            String timeSlot = String.format("%02d:%02d", i / 60, i % 60);
            if (!existingTimeSlots.contains(timeSlot)) { // Ellenőrizd, hogy az adott időpont már foglalt-e
                checkAvailability(year, month, dayOfMonth, timeSlot, dateKey);
            }
        }
    }

    private void checkAvailability(int year, int month, int dayOfMonth, String timeSlot, String dateKey) {
        databaseReference.child("appointments").child(dateKey).child(timeSlot).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) { // Ha az adott időpont foglalatlan
                    Button timeSlotButton = createTimeSlotButton(timeSlot, year, month, dayOfMonth, dateKey);
                    timeSlotLayout.addView(timeSlotButton);
                    timeSlotButtons.put(timeSlot, timeSlotButton);
                } else {
                    timeSlotButtons.remove(timeSlot); // Távolítsa el az időpontot a listából, ha foglalt
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Booking", "Error checking availability", databaseError.toException());
            }
        });
    }

    private Button createTimeSlotButton(String timeSlot, int year, int month, int dayOfMonth, String dateKey) {
        Button timeSlotButton = new Button(Booking.this);
        timeSlotButton.setText(timeSlot);
        timeSlotButton.setTag(timeSlot); // set tag for easy removal
        timeSlotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeSlot = ((Button) v).getText().toString();
                bookAppointment(year, month, dayOfMonth, timeSlot, dateKey);
            }
        });
        return timeSlotButton;
    }

    private static final int TIME_SLOT_LAYOUT_ID = R.id.time_slot_layout;
    private void bookAppointment(int year, int month, int dayOfMonth, String timeSlot, String dateKey) {
        String appointmentKey = databaseReference.push().getKey();
        Appointment appointment = new Appointment(year, month, dayOfMonth, timeSlot);
        databaseReference.child("appointments").child(dateKey).child(appointmentKey).setValue(appointment);

        // Távolítsa el az időpontot a rendelkezésre álló időpontok közül
        Button timeSlotButton = timeSlotButtons.remove(timeSlot);
        if (timeSlotButton != null) {
            timeSlotLayout.removeView(timeSlotButton);
        }
        // Távolítsa el az időpontot a már foglalt időpontok közül
        removeTimeSlotView(timeSlot);
    }

    private void removeTimeSlotView(String timeSlot) {
        ViewGroup timeSlotLayout = findViewById(TIME_SLOT_LAYOUT_ID);
        if (timeSlotLayout != null) {
            View timeSlotView = timeSlotLayout.findViewWithTag(timeSlot);
            if (timeSlotView != null) {
                timeSlotLayout.removeView(timeSlotView);
            }
        }
    }
}
