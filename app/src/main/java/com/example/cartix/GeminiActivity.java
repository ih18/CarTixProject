package com.example.cartix;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class GeminiActivity extends AppCompatActivity implements OnInitListener, CarAdapter.OnCarFavoriteListener {

    private TextToSpeech textToSpeech;
    private Button speakButton;
    public Button historyButton;
    private FloatingActionButton saveToFavoritesButton;

    public RecyclerView recyclerView;
    public ArrayList<Car> cars;
    public CarAdapter carAdapter;

    private String responseText = "";  // Variable to store the response text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        historyButton = findViewById(R.id.historyButton);
        speakButton = findViewById(R.id.speakButton);
        saveToFavoritesButton = findViewById(R.id.saveToFavoritesButton);

        // Set up RecyclerView
        cars = new ArrayList<>();
        recyclerView = findViewById(R.id.recycleview_gemini);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter with favorite listener
        carAdapter = new CarAdapter(cars, this);
        recyclerView.setAdapter(carAdapter);

        // Set up history button click listener
        historyButton.setOnClickListener(v -> {
            try {
                Log.d("GeminiActivity", "History button clicked");
                Intent intent = new Intent(GeminiActivity.this, HistoryActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("GeminiActivity", "Error launching history activity", e);
                Toast.makeText(GeminiActivity.this,
                        "Could not open history: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Get the query from intent
        GeminiManager model = new GeminiManager();
        Intent intent = getIntent();
        String query = intent.getStringExtra("key");

        // Handle response from Gemini
        model.getResponse(query, new GenerativeResponseCallback() {
            @Override
            public void onResponse(String response) {
                // Set the response text
                responseText = response;  // Save the response

                // Save the response to history
                HistoryActivity.addResponseToHistory(GeminiActivity.this, query, response);

                try {
                    // Extract content between asterisks (*)
                    int startIndex = responseText.indexOf('*');
                    int endIndex = responseText.lastIndexOf('*');

                    if (startIndex >= 0 && endIndex > startIndex) {
                        // Extract the content between asterisks
                        String carListContent = responseText.substring(startIndex + 1, endIndex).trim();

                        // Split by "^" character to get individual car entries
                        String[] arrOfString = carListContent.split("\\^");

                        // Create a new ArrayList for cars
                        ArrayList<Car> newCars = new ArrayList<>();

                        for (String a : arrOfString) {
                            // Skip empty entries
                            if (a.trim().isEmpty()) continue;

                            // Split each car entry by "/"
                            String[] temp = a.split("/");

                            // Make sure we have year/price/name (at least 3 parts)
                            if (temp.length >= 3) {
                                String year = temp[0].trim();
                                String money = temp[1].trim();
                                String carName = temp[2].trim();

                                // Create a new Car object and add to the list
                                Car car = new Car(carName, year, money);

                                // Check if this car is already a favorite using static method
                                if (FavoritesManagerActivity.isCarFavorite(GeminiActivity.this, car)) {
                                    car.setFavorite(true);
                                }

                                newCars.add(car);
                            }
                        }

                        // Only update the adapter if we have valid cars
                        if (!newCars.isEmpty()) {
                            cars = newCars;
                            carAdapter.updateData(newCars);
                        } else {
                            Toast.makeText(GeminiActivity.this,
                                    "No cars found in the response",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle case where asterisks were not found
                        Toast.makeText(GeminiActivity.this,
                                "Could not find car data in the response",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    // Handle any errors during processing
                    Toast.makeText(GeminiActivity.this,
                            "Error processing data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Throwable error) {
                // Handle error and set error message to response text
                responseText = "Error generating response!";  // Save the error message

                // Save error to history
                HistoryActivity.addResponseToHistory(GeminiActivity.this, query, responseText);

                // Show error toast
                Toast.makeText(GeminiActivity.this,
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Save to favorites button click event
        saveToFavoritesButton.setOnClickListener(v -> {
            boolean hasFavorites = false;
            for (Car car : cars) {
                if (car.isFavorite()) {
                    hasFavorites = true;
                    // Use static method from FavoritesManagerActivity
                    FavoritesManagerActivity.addCarToFavorites(GeminiActivity.this, car);
                }
            }

            if (hasFavorites) {
                Toast.makeText(GeminiActivity.this, "Selected cars saved to favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GeminiActivity.this, "Please select cars to save", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, this);

        // Set up the Speak button to read out the response
        speakButton.setOnClickListener(v -> {
            if (!responseText.isEmpty()) {
                speakOut(responseText);  // Speak out the response
            } else {
                Toast.makeText(GeminiActivity.this, "No response to speak", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language
            int langResult = textToSpeech.setLanguage(Locale.US);
            if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language is not supported or data is missing
                Toast.makeText(this, "Text-to-speech language not supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Initialization failed
            Toast.makeText(this, "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to speak the text
    private void speakOut(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        // Shutdown TextToSpeech to release resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuHomePage) {
            startActivity(new Intent(this, HomePage.class));
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menuFavoritesHistory) {
            startActivity(new Intent(this, FavoritesManagerActivity.class));
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menuHistory) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.menuLogout) {
            FirebaseControl firebaseControl = new FirebaseControl(this);
            firebaseControl.logOutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implement the CarAdapter.OnCarFavoriteListener interface
    @Override
    public void onFavoriteChanged(Car car, boolean isFavorite) {
        // Update the car's favorite status when toggled in the adapter
        car.setFavorite(isFavorite);
    }
}