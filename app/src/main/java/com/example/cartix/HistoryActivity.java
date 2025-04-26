package com.example.cartix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements CarAdapter.OnCarFavoriteListener, HistoryAdapter.OnViewCarsClickListener {
    private static final String TAG = "HistoryActivity";
    private static final String PREFS_NAME = "CarTixHistory";
    private static final String HISTORY_KEY = "gemini_responses";
    private static final int MAX_HISTORY_SIZE = 50;

    private RecyclerView recyclerView;
    private RecyclerView carRecyclerView;
    private TextView emptyStateTextView;
    private ArrayList<HistoryItem> historyItems = new ArrayList<>();
    private HistoryAdapter adapter;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private FloatingActionButton saveToFavoritesButton;
    private ArrayList<Car> currentCarList = new ArrayList<>();
    private CarAdapter carAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Debug the history data at startup
        debugHistoryData();

        try {
            // Set up views
            recyclerView = findViewById(R.id.recyclerViewHistory);
            carRecyclerView = findViewById(R.id.recyclerViewHistoryCars);
            emptyStateTextView = findViewById(R.id.textViewEmptyState);
            saveToFavoritesButton = findViewById(R.id.saveToFavoritesButton);

            if (recyclerView == null) {
                Log.e(TAG, "RecyclerView not found in layout");
                Toast.makeText(this, "Error initializing UI", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set up RecyclerViews
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            carRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Initialize TextToSpeech
            initTextToSpeech();

            // Get history items safely
            historyItems = getSavedHistoryItems();

            // Create adapters
            adapter = new HistoryAdapter(this, historyItems);
            recyclerView.setAdapter(adapter);

            // Initialize cars RecyclerView
            carAdapter = new CarAdapter(currentCarList, this);
            carRecyclerView.setAdapter(carAdapter);

            // Update empty state visibility
            updateEmptyState();

            // Setup save to favorites button
            saveToFavoritesButton.setOnClickListener(v -> {
                boolean hasFavorites = false;
                for (Car car : currentCarList) {
                    if (car.isFavorite()) {
                        hasFavorites = true;
                        FavoritesManagerActivity.addCarToFavorites(HistoryActivity.this, car);
                    }
                }

                if (hasFavorites) {
                    Toast.makeText(HistoryActivity.this, "Selected cars saved to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HistoryActivity.this, "Please select cars to save", Toast.LENGTH_SHORT).show();
                }
            });

            // Set action bar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Response History");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Fatal error in onCreate", e);
            Toast.makeText(this, "Could not initialize history view", Toast.LENGTH_SHORT).show();
        }
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                ttsInitialized = (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED);
                Log.d(TAG, "TTS initialized successfully: " + ttsInitialized);
            } else {
                Log.e(TAG, "TTS initialization failed with status: " + status);
                ttsInitialized = false;
            }

            // Refresh the adapter to update the speak buttons
            if (adapter != null) {
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        });
    }

    private void debugHistoryData() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String json = prefs.getString(HISTORY_KEY, null);

            Log.d(TAG, "============ DEBUG HISTORY DATA ============");
            if (json == null) {
                Log.d(TAG, "No history data found (null)");
            } else if (json.isEmpty()) {
                Log.d(TAG, "History data is empty string");
            } else {
                Log.d(TAG, "History data length: " + json.length());
                Log.d(TAG, "First 100 chars: " + json.substring(0, Math.min(json.length(), 100)));

                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
                    ArrayList<HistoryItem> items = gson.fromJson(json, type);

                    if (items == null) {
                        Log.d(TAG, "Deserialized to null");
                    } else {
                        Log.d(TAG, "Successfully deserialized " + items.size() + " items");
                        if (!items.isEmpty()) {
                            HistoryItem first = items.get(0);
                            if (first != null) {
                                Log.d(TAG, "First item - Response length: " + (first.getResponse() != null ?
                                        first.getResponse().length() : "null") +
                                        ", Timestamp: " + (first.getTimestamp() != null ? first.getTimestamp() : "null"));
                            } else {
                                Log.d(TAG, "First item is null");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error deserializing JSON: " + e.getMessage(), e);
                }
            }
            Log.d(TAG, "=========================================");
        } catch (Exception e) {
            Log.e(TAG, "Error in debug method: " + e.getMessage(), e);
        }
    }

    // Implement OnViewCarsClickListener interface
    @Override
    public void onViewCarsClicked(String responseText) {
        extractAndDisplayCars(responseText);
    }

    // Inner adapter only used if the main adapter fails to load
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private ArrayList<HistoryItem> items;
        private Context context;

        HistoryAdapter(Context context, ArrayList<HistoryItem> items) {
            this.context = context;
            this.items = items != null ? items : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycleitem_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                if (position < 0 || position >= items.size()) return;

                HistoryItem item = items.get(position);
                if (item == null) return;

                // Hide the query TextView if it exists
                if (holder.queryTextView != null) {
                    holder.queryTextView.setVisibility(View.GONE);
                }

                String response = item.getResponse();
                String displayResponse = response;

                if (response != null && response.length() > 100) {
                    displayResponse = response.substring(0, 97) + "...";
                }

                holder.responseTextView.setText(displayResponse != null ? displayResponse : "No response");
                holder.responseTextView.setTag(false); // Not expanded initially

                holder.timestampTextView.setText(item.getTimestamp() != null ?
                        item.getTimestamp() : "Unknown time");

                // IMPORTANT FIX: Always make the speak button visible regardless of TTS status
                if (holder.speakButton != null) {
                    holder.speakButton.setVisibility(View.VISIBLE);

                    holder.speakButton.setOnClickListener(v -> {
                        if (ttsInitialized && item.getResponse() != null) {
                            textToSpeech.speak(item.getResponse(),
                                    TextToSpeech.QUEUE_FLUSH, null, "history_" + position);
                        } else {
                            Toast.makeText(context, "Text-to-Speech not available",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "Speak button is null for position " + position);
                }

                // Set up view cars button
                holder.viewCarsButton.setOnClickListener(v -> {
                    extractAndDisplayCars(item.getResponse());
                });

                // Set up click listener for expanding/collapsing
                holder.itemView.setOnClickListener(v -> {
                    boolean isExpanded = holder.responseTextView.getTag() != null &&
                            (boolean) holder.responseTextView.getTag();

                    if (isExpanded) {
                        // Collapse
                        if (response != null && response.length() > 100) {
                            holder.responseTextView.setText(response.substring(0, 97) + "...");
                        }
                    } else {
                        // Expand
                        holder.responseTextView.setText(response != null ? response : "No response");
                    }

                    holder.responseTextView.setTag(!isExpanded);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error binding view holder", e);
                holder.responseTextView.setText("Could not load this item");
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView queryTextView, responseTextView, timestampTextView;
            ImageButton speakButton;
            Button viewCarsButton;

            ViewHolder(@NonNull View v) {
                super(v);
                queryTextView = v.findViewById(R.id.textViewQuery);
                responseTextView = v.findViewById(R.id.textViewResponse);
                timestampTextView = v.findViewById(R.id.textViewTimestamp);
                speakButton = v.findViewById(R.id.buttonSpeak);
                viewCarsButton = v.findViewById(R.id.buttonViewCars);

                // Always ensure the query is hidden
                if (queryTextView != null) {
                    queryTextView.setVisibility(View.GONE);
                }

                // IMPORTANT FIX: Force the speak button to be visible in the ViewHolder constructor
                if (speakButton != null) {
                    speakButton.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "Speak button not found in ViewHolder");
                }
            }
        }
    }

    // Extract cars from response text and display them
    private void extractAndDisplayCars(String responseText) {
        if (responseText == null || responseText.isEmpty()) {
            Toast.makeText(this, "No response data to extract cars from", Toast.LENGTH_SHORT).show();
            return;
        }

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
                        if (FavoritesManagerActivity.isCarFavorite(this, car)) {
                            car.setFavorite(true);
                        }

                        newCars.add(car);
                    }
                }

                // Only update the adapter if we have valid cars
                if (!newCars.isEmpty()) {
                    currentCarList = newCars;
                    carAdapter.updateData(newCars);
                    carRecyclerView.setVisibility(View.VISIBLE);
                    saveToFavoritesButton.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Found " + newCars.size() + " cars", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No cars found in this response", Toast.LENGTH_SHORT).show();
                    carRecyclerView.setVisibility(View.GONE);
                    saveToFavoritesButton.setVisibility(View.GONE);
                }
            } else {
                // Handle case where asterisks were not found
                Toast.makeText(this, "No car data found in this response", Toast.LENGTH_SHORT).show();
                carRecyclerView.setVisibility(View.GONE);
                saveToFavoritesButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            // Handle any errors during processing
            Toast.makeText(this, "Error processing car data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            carRecyclerView.setVisibility(View.GONE);
            saveToFavoritesButton.setVisibility(View.GONE);
        }
    }

    // Update empty state text visibility
    private void updateEmptyState() {
        if (emptyStateTextView == null || recyclerView == null) return;

        if (historyItems == null || historyItems.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Safely get history items from SharedPreferences
    private ArrayList<HistoryItem> getSavedHistoryItems() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(HISTORY_KEY, null);

            if (json == null || json.isEmpty()) {
                Log.d(TAG, "No history data found");
                return new ArrayList<>();
            }

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
            ArrayList<HistoryItem> history = gson.fromJson(json, type);

            if (history == null) {
                Log.d(TAG, "History deserialized to null");
                return new ArrayList<>();
            }

            Log.d(TAG, "Successfully loaded " + history.size() + " history items");
            return history;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing error", e);
            // Reset corrupted data
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(HISTORY_KEY)
                    .apply();
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading history", e);
            return new ArrayList<>();
        }
    }

    // Static method to add response to history - simplified for robustness
    public static void addResponseToHistory(Context context, String query, String response) {
        if (context == null || query == null || response == null) {
            Log.e(TAG, "Null parameters in addResponseToHistory");
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            ArrayList<HistoryItem> historyItems = new ArrayList<>();

            // Try to get existing items
            try {
                String json = prefs.getString(HISTORY_KEY, null);
                if (json != null && !json.isEmpty()) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
                    ArrayList<HistoryItem> existingItems = gson.fromJson(json, type);
                    if (existingItems != null) {
                        historyItems = existingItems;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading existing history", e);
                // Continue with empty list if there was an error
            }

            // Create timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Add new item
            historyItems.add(0, new HistoryItem(query, response, timestamp));

            // Limit history size
            if (historyItems.size() > MAX_HISTORY_SIZE) {
                historyItems = new ArrayList<>(historyItems.subList(0, MAX_HISTORY_SIZE));
            }

            // Save to SharedPreferences
            Gson gson = new Gson();
            String updatedJson = gson.toJson(historyItems);
            prefs.edit().putString(HISTORY_KEY, updatedJson).apply();

            Log.d(TAG, "Successfully added new history item");
        } catch (Exception e) {
            Log.e(TAG, "Failed to save history", e);
        }
    }

    // Clear all history
    public void clearHistory() {
        try {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(HISTORY_KEY)
                    .apply();

            historyItems.clear();
            updateEmptyState();

            // Refresh adapter
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing history", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                finish(); // Use finish instead of onBackPressed which is deprecated
                return true;
            }
            if (itemId == R.id.menuHomePage) {
                startActivity(new Intent(this, HomePage.class));
                finish();
                return true;
            }
            if (itemId == R.id.menuFavoritesHistory) {
                startActivity(new Intent(this, FavoritesManagerActivity.class));
                finish();
                return true;
            }
            if (itemId == R.id.menuClearHistory) {
                clearHistory();
                return true;
            }
            if (itemId == R.id.menuLogout) {
                FirebaseControl firebaseControl = new FirebaseControl(this);
                firebaseControl.logOutUser();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in menu handling", e);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // Clean up TTS resources
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        super.onDestroy();
    }

    // Implement OnCarFavoriteListener
    @Override
    public void onFavoriteChanged(Car car, boolean isFavorite) {
        car.setFavorite(isFavorite);
    }
}