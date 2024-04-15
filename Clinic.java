// DatabaseHelper.java
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "clinic_db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables for users, doctors, and appointments
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, email TEXT, password TEXT)");
        db.execSQL("CREATE TABLE doctors (id INTEGER PRIMARY KEY, name TEXT, specialty TEXT, available_slots TEXT)");
        db.execSQL("CREATE TABLE appointments (id INTEGER PRIMARY KEY, user_id INTEGER, doctor_id INTEGER, appointment_time TEXT, FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (doctor_id) REFERENCES doctors(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema changes
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS doctors");
        db.execSQL("DROP TABLE IF EXISTS appointments");
        onCreate(db);
    }

    // Implement CRUD operations for users, doctors, and appointments
    public boolean addUser(String name, String email, String password) {
        // ...
    }

    public boolean addDoctor(String name, String specialty, String availableSlots) {
        // ...
    }

    public boolean addAppointment(int userId, int doctorId, String appointmentTime) {
        // ...
    }

    // Implement other necessary methods
}


// AppointmentActivity.java
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AppointmentActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Spinner doctorSpinner, timeSlotSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        databaseHelper = new DatabaseHelper(this);

        doctorSpinner = findViewById(R.id.doctor_spinner);
        timeSlotSpinner = findViewById(R.id.time_slot_spinner);

        // Populate the doctor spinner
        Cursor doctorsCursor = databaseHelper.getAllDoctors();
        ArrayList<String> doctorNames = new ArrayList<>();
        while (doctorsCursor.moveToNext()) {
            String doctorName = doctorsCursor.getString(doctorsCursor.getColumnIndex("name"));
            doctorNames.add(doctorName);
        }
        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorNames);
        doctorSpinner.setAdapter(doctorAdapter);

        // Populate the time slot spinner based on the selected doctor
        doctorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDoctor = (String) parent.getItemAtPosition(position);
                Cursor availableSlotsCursor = databaseHelper.getAvailableSlotsByDoctor(selectedDoctor);
                ArrayList<String> availableSlots = new ArrayList<>();
                while (availableSlotsCursor.moveToNext()) {
                    String slot = availableSlotsCursor.getString(availableSlotsCursor.getColumnIndex("available_slots"));
                    availableSlots.add(slot);
                }
                ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(AppointmentActivity.this, android.R.layout.simple_spinner_item, availableSlots);
                timeSlotSpinner.setAdapter(slotAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle appointment booking
        findViewById(R.id.book_appointment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDoctor = (String) doctorSpinner.getSelectedItem();
                String selectedTimeSlot = (String) timeSlotSpinner.getSelectedItem();
                int doctorId = databaseHelper.getDoctorIdByName(selectedDoctor);
                int userId = databaseHelper.getCurrentUserId(); // Assuming you have a method to get the current user's ID

                if (databaseHelper.addAppointment(userId, doctorId, selectedTimeSlot)) {
                    Toast.makeText(AppointmentActivity.this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AppointmentActivity.this, "Failed to book appointment.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
