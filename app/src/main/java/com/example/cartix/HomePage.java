package com.example.cartix;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class HomePage extends AppCompatActivity implements FirebaseCallback, SeekBar.OnSeekBarChangeListener, View.OnClickListener, SensorEventListener {
    private static final String TAG = "HomePage"; // Add a TAG for logging

    FirebaseControl firebaseControl;

    ConstraintLayout layout;

    TextView helloText;
    TextView engine_vol_text;
    TextView year_text;
    TextView horsepower_text;
    TextView textking;
    TextView textView;
    TextView textView8;
    TextView textView10;
    TextView textView11;
    TextView textView12;
    TextView textView13;
    TextView textView14;
    TextView textView17;

    EditText editTextPrice;

    Button testButton;
    Button familycar;
    Button sportcar;
    Button jeepcar;
    Button manualcar;

    String hpval;
    String enginevolVal;
    String prodyearVal;
    String carType = ""; // Initialize carType to avoid NullPointerException
    int preferedPrice;


    SeekBar horse_power;
    SeekBar engine_vol;
    SeekBar prod_year;
    String aiRequest;

    private SensorManager sensorManager;
    private Sensor lightSensor;


    private WifiBroadcastReceiver wifiReceiver;




    // קריאה לפונקציה ששולחת את הבקשה ל-ChatGPT


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Log.d(TAG, "onCreate: Starting HomePage activity");

        firebaseControl = new FirebaseControl(this);
        firebaseControl.readData((FirebaseCallback) this);
        layout = findViewById(R.id.main);
        testButton = findViewById(R.id.testbutton);
        familycar = findViewById(R.id.familycar);
        sportcar = findViewById(R.id.sportcar);
        jeepcar = findViewById(R.id.jeepcar);
        manualcar = findViewById(R.id.manualcar);
        helloText = findViewById(R.id.helloText);
        editTextPrice = findViewById(R.id.editTextPrice);
        textView = findViewById(R.id.textView8);
        textView8 = findViewById(R.id.textView8);
        textView10 = findViewById(R.id.textView10);
        textView11 = findViewById(R.id.textView11);
        textView12 = findViewById(R.id.textView12);
        textView13 = findViewById(R.id.textView13);
        textView14 = findViewById(R.id.textView14);
        textView17 = findViewById(R.id.textView17);
        horse_power = findViewById(R.id.hourse_power);
        engine_vol = findViewById(R.id.engine_volume);
        engine_vol_text = findViewById(R.id.engine_volume_text);
        prod_year = findViewById(R.id.prod_year);
        year_text = findViewById(R.id.year_text);
        horsepower_text = findViewById(R.id.horsepower_text);
        textking = findViewById(R.id.textking);

        if(textking == null) {
            Log.e(TAG, "textking TextView not found in layout!");
        } else {
            Log.d(TAG, "textking TextView initialized successfully");
            textking.setText("רמת תאורה: טוען...");
        }


        horse_power.setOnSeekBarChangeListener(this);
        engine_vol.setOnSeekBarChangeListener(this);
        prod_year.setOnSeekBarChangeListener(this);


        wifiReceiver = new WifiBroadcastReceiver();

        // אתחול מנהל החיישנים
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // קבלת החיישן מהמכשיר
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            if (lightSensor == null) {
                Log.e(TAG, "Light sensor not available on this device");
                if(textking != null) {
                    textking.setText("חיישן תאורה אינו זמין במכשיר זה");
                }
            } else {
                Log.d(TAG, "Light sensor available and initialized");
            }
        } else {
            Log.e(TAG, "SensorManager is null");
        }

        // Register receiver for Wi-Fi and connectivity change
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, filter);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ENIGNE: " + engine_vol_text.getText().toString());
                System.out.println("YEAR: " + year_text.getText().toString());
                System.out.println("hourse: " + horsepower_text.getText().toString());
                System.out.println("price: " + editTextPrice.getText().toString());

                carType = ""; // Reset carType before checking buttons

                if(familycar.getTag().equals("red"))
                {
                    carType += "family car, ";
                }
                if(manualcar.getTag().equals("red"))
                {
                    carType += "manual car, ";
                }
                if(jeepcar.getTag().equals("red"))
                {
                    carType += "jeep/SUV, ";
                }
                if(sportcar.getTag().equals("red"))
                {
                    carType += "sport car, ";
                }

                // Remove the trailing comma and space if they exist
                if(carType.endsWith(", ")) {
                    carType = carType.substring(0, carType.length() - 2);
                }

                // If no car type is selected, set a default
                if(carType.isEmpty()) {
                    carType = "any car";
                }

                sendNotification();

                // Make sure price is entered before proceeding
                String priceText = editTextPrice.getText().toString();
                if(priceText.isEmpty()) {
                    Toast.makeText(HomePage.this, "אנא הכנס מחיר", Toast.LENGTH_SHORT).show();
                    return;
                }

                aiRequest = createAIRequest(engine_vol_text.getText().toString(),
                        year_text.getText().toString(),
                        horsepower_text.getText().toString(),
                        priceText,
                        carType);
                Intent intent = new Intent(HomePage.this,GeminiActivity.class);
                intent.putExtra("key", aiRequest);
                startActivity(intent);
            }
        });

        familycar.setOnClickListener(this);
        sportcar.setOnClickListener(this);
        jeepcar.setOnClickListener(this);
        manualcar.setOnClickListener(this);


        int stepEngineVol = 1;
        int maxEngineVol = 7000;
        int minEngineVol = 100;

        engine_vol_text.setText(minEngineVol + "");

// Ex :
// If you want values from 3 to 5 with a step of 0.1 (3, 3.1, 3.2, ..., 5)
// this means that you have 21 possible values in the seekbar.
// So the range of the seek bar will be [0 ; (5-3)/0.1 = 20].
        engine_vol.setMax((maxEngineVol - minEngineVol) / stepEngineVol);


        engine_vol.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        int value = minEngineVol + (progress * stepEngineVol);

                        engine_vol_text.setText(value + "");
                    }
                }
        );

        int stepYear = 1;
        int maxYear = 2022;
        int minYear = 1992;

        year_text.setText(minYear + "");

// Ex :
// If you want values from 3 to 5 with a step of 0.1 (3, 3.1, 3.2, ..., 5)
// this means that you have 21 possible values in the seekbar.
// So the range of the seek bar will be [0 ; (5-3)/0.1 = 20].
        prod_year.setMax((maxYear - minYear) / stepYear);


        prod_year.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        int value = minYear + (progress * stepYear);

                        year_text.setText(value + "");
                    }
                }
        );


        int stepHorsePower = 1;
        int maxHorsePower = 1200;
        int minHorsePower = 70;

        horsepower_text.setText(minHorsePower + "");

// Ex :
// If you want values from 3 to 5 with a step of 0.1 (3, 3.1, 3.2, ..., 5)
// this means that you have 21 possible values in the seekbar.
// So the range of the seek bar will be [0 ; (5-3)/0.1 = 20].
        horse_power.setMax((maxHorsePower - minHorsePower) / stepHorsePower);


        horse_power.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        int value = minHorsePower + (progress * stepHorsePower);

                        horsepower_text.setText(value + "");
                    }
                }
        );

        Log.d(TAG, "onCreate: HomePage setup complete");
    }

    private void sendNotification() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 1, intent, FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (1000), pendingIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }
    @Override
    public void onCallbackUser(UserInformation user)
    {
        Toast.makeText(this, "HI  " +user.getUserName(),Toast.LENGTH_SHORT).show();
        helloText.setText("HI  " + user.getEmail());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menuHomePage)
        {
            startActivity(new Intent(this, HomePage.class));
            finish();
        }
        if(item.getItemId()==R.id.menuFavoritesHistory)
        {
            startActivity(new Intent(this, FavoritesManagerActivity.class));
            finish();
        }
        if (item.getItemId() == R.id.menuHistory) {
            // Make sure this ID matches the one in your menu XML file
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        if(item.getItemId()==R.id.menuLogout)
        {
            firebaseControl.logOutUser();
        }

        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == horse_power) {
            hpval = progress + "";
        }
        if (seekBar == engine_vol) {
            enginevolVal = progress + "";
        }
        if (seekBar == prod_year) {
            prodyearVal = progress + "";
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        if (v == familycar) {
            if (familycar.getTag().equals("blue")) {
                //carType += "family car";
                familycar.setBackgroundColor(Color.parseColor("#A020F0"));
                familycar.setTag("red");
            } else {
                familycar.setBackgroundColor(Color.parseColor("#808080"));
                familycar.setTag("blue");

            }
        }

        if (v == sportcar) {
            if (sportcar.getTag().equals("blue")) {
                //  carType += "family car";
                sportcar.setBackgroundColor(Color.parseColor("#A020F0"));
                sportcar.setTag("red");
            } else {
                sportcar.setBackgroundColor(Color.parseColor("#808080"));
                sportcar.setTag("blue");

            }
        }
        if(v == jeepcar)
        {
            if (jeepcar.getTag().equals("blue")) {
                //  carType += "family car";
                jeepcar.setBackgroundColor(Color.parseColor("#A020F0"));
                jeepcar.    setTag("red");
            } else {
                jeepcar.setBackgroundColor(Color.parseColor("#808080"));
                jeepcar.setTag("blue");

            }
        }
        if(v == manualcar)
        {
            if (manualcar.getTag().equals("blue")) {
                //  carType += "family car";
                manualcar.setBackgroundColor(Color.parseColor("#A020F0"));
                manualcar.setTag("red");
            } else {
                manualcar.setBackgroundColor(Color.parseColor("#808080"));
                manualcar.setTag("blue");

            }
        }
    }

    private String createAIRequest(String engineVol, String year, String horsepower,String price, String carType) {
        int priceInt = Integer.parseInt(price);
        String prompt ="תדמין שאתה מומחה ברכבים בעל 50 שנות עניין בסוגי רכבים ומפרט שלהם \n" +
                "החזר את 10 הרכבים המשתלמים ביותר בטווח האפשרויות שנבחרו (טווח בערך של האפשרויות)\n" +
                "נפח מנוע: " + engineVol + "\n" +
                "שנה: " + year + "\n" +
                "הספק: " + horsepower + "\n" +
                "סוגי רכב: " + carType + "\n" +
                "מחיר הרכב: " + priceInt + "\n"+
                "אם אין רכב באותו המחיר ובאותו נפח מנוע וכוח סוס מדוייקים, תביא רכבים עם אותו מחיר שביקשתי אשר בעלי נפח מנוע וכוח סוס דומים וקרובים למה שביקשתי. \n" +
                "כתוב את כל המידע באנגלית, ואת המחירים בשקלים (שקלים חדשים). כתוב את המחיר בהתאם למחירים בעולם, תשקלל את המחיר לפי רכב חדש באותו שנה. \n" +
                "הפרידו את התיאור, בין השם המחיר והשנה תשים סלאש. אל תמספר את התוצאות. בין כל מכונית תשים את התו ^ . בתחילת הרכבים שאתה נותן ובסופם תוסיף את הסימן * .\n" +
                " תרשום רק את השם של הרכב, המחיר והשנה של הרכב בדיוק כמו שביקשתי, מדויק. \n" +
                "אל תרשום עוד מלל חוץ ממה שביקשתי, אל תתאר את הרכבים חוץ ממה שביקשתי!\n";

        return prompt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(wifiReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering light sensor listener");
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Unregistering light sensor listener");
        // ביטול רישום כדי לחסוך במשאבים
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0]; // ערך התאורה
            Log.d(TAG, "Light sensor value: " + lightLevel);

            // Check if textking is available
            if (textking != null) {
                textking.setText("רמת תאורה: " + lightLevel);
                // Make sure the textking is visible
                textking.setVisibility(View.VISIBLE);
            } else {
                Log.e(TAG, "textking is null in onSensorChanged");
            }

            // שינוי צבע הרקע על פי התאורה
            if (lightLevel < 100) {
                // Low light level (dark mode) - keeping this as is
                layout.setBackgroundColor(Color.parseColor("#121212")); // darker background for low light
                textView.setTextColor(Color.parseColor("#FFFFFF"));
                textView8.setTextColor(Color.parseColor("#FFFFFF"));
                textView10.setTextColor(Color.parseColor("#FFFFFF"));
                textView11.setTextColor(Color.parseColor("#FFFFFF"));
                textView12.setTextColor(Color.parseColor("#FFFFFF"));
                textView13.setTextColor(Color.parseColor("#FFFFFF"));
                textView14.setTextColor(Color.parseColor("#FFFFFF"));
                textking.setTextColor(Color.parseColor("#FFFFFF"));
                textking.setBackgroundColor(Color.parseColor("#333333")); // darker background for visibility
                helloText.setTextColor(Color.parseColor("#FFFFFF"));
                year_text.setTextColor(Color.parseColor("#FFFFFF"));
                textView17.setTextColor(Color.parseColor("#FFFFFF"));
                horsepower_text.setTextColor(Color.parseColor("#FFFFFF"));
                engine_vol_text.setTextColor(Color.parseColor("#FFFFFF"));
                testButton.setTextColor(Color.parseColor("#FFFFFF"));

                // Buttons background color - darker for low light
                familycar.setBackgroundColor(Color.parseColor("#003366"));
                sportcar.setBackgroundColor(Color.parseColor("#003366"));
                jeepcar.setBackgroundColor(Color.parseColor("#003366"));
                manualcar.setBackgroundColor(Color.parseColor("#003366"));

                // EditText background for low light
                editTextPrice.setBackgroundColor(Color.parseColor("#2E2E2E"));
                editTextPrice.setTextColor(Color.parseColor("#FFFFFF"));
            } else if (lightLevel < 500) {
                // Medium light level - blue theme
                layout.setBackgroundColor(Color.parseColor("#E6F2FF")); // light blue background

                // Text colors - dark blue for better readability
                int textColor = Color.parseColor("#003366"); // dark blue
                textView.setTextColor(textColor);
                textView8.setTextColor(textColor);
                textView10.setTextColor(textColor);
                textView11.setTextColor(textColor);
                textView12.setTextColor(textColor);
                textView13.setTextColor(textColor);
                textView14.setTextColor(textColor);
                textView17.setTextColor(textColor);

                // Special text elements
                textking.setTextColor(Color.parseColor("#FFFFFF"));
                textking.setBackgroundColor(Color.parseColor("#336699")); // medium blue background
                helloText.setTextColor(Color.parseColor("#336699")); // medium blue

                // Value display text
                year_text.setTextColor(textColor);
                horsepower_text.setTextColor(textColor);
                engine_vol_text.setTextColor(textColor);

                // Button colors - consistent blue shades
                int buttonColor = Color.parseColor("#4D88C4"); // medium-light blue
                familycar.setBackgroundColor(buttonColor);
                sportcar.setBackgroundColor(buttonColor);
                jeepcar.setBackgroundColor(buttonColor);
                manualcar.setBackgroundColor(buttonColor);

                // All button text in white for better contrast
                familycar.setTextColor(Color.parseColor("#FFFFFF"));
                sportcar.setTextColor(Color.parseColor("#FFFFFF"));
                jeepcar.setTextColor(Color.parseColor("#FFFFFF"));
                manualcar.setTextColor(Color.parseColor("#FFFFFF"));
                testButton.setBackgroundColor(Color.parseColor("#336699")); // slightly darker blue
                testButton.setTextColor(Color.parseColor("#FFFFFF"));

                // EditText styling
                editTextPrice.setBackgroundColor(Color.parseColor("#FFFFFF"));
                editTextPrice.setTextColor(textColor);
            } else {
                // High light level - clean white theme with green accents
                layout.setBackgroundColor(Color.parseColor("#FFFFFF")); // pure white background

                // Text colors - dark green for professional look
                int textColor = Color.parseColor("#004D40"); // dark green
                textView.setTextColor(textColor);
                textView8.setTextColor(textColor);
                textView10.setTextColor(textColor);
                textView11.setTextColor(textColor);
                textView12.setTextColor(textColor);
                textView13.setTextColor(textColor);
                textView14.setTextColor(textColor);
                textView17.setTextColor(textColor);

                // Special text elements
                textking.setTextColor(Color.parseColor("#FFFFFF"));
                textking.setBackgroundColor(Color.parseColor("#00796B")); // darker green background
                helloText.setTextColor(Color.parseColor("#00796B")); // medium green

                // Value display text
                year_text.setTextColor(textColor);
                horsepower_text.setTextColor(textColor);
                engine_vol_text.setTextColor(textColor);

                // Button colors - consistent green shades
                int buttonColor = Color.parseColor("#26A69A"); // light green
                familycar.setBackgroundColor(buttonColor);
                sportcar.setBackgroundColor(buttonColor);
                jeepcar.setBackgroundColor(buttonColor);
                manualcar.setBackgroundColor(buttonColor);

                // All button text in white for better contrast
                familycar.setTextColor(Color.parseColor("#FFFFFF"));
                sportcar.setTextColor(Color.parseColor("#FFFFFF"));
                jeepcar.setTextColor(Color.parseColor("#FFFFFF"));
                manualcar.setTextColor(Color.parseColor("#FFFFFF"));
                testButton.setBackgroundColor(Color.parseColor("#00796B")); // medium green
                testButton.setTextColor(Color.parseColor("#FFFFFF"));

                // EditText styling
                editTextPrice.setBackgroundColor(Color.parseColor("#E0F2F1")); // very light green
                editTextPrice.setTextColor(textColor);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // לא נדרש כאן
    }
}