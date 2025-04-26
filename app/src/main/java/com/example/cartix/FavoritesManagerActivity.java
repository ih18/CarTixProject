package com.example.cartix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FavoritesManagerActivity extends AppCompatActivity implements CarAdapter.OnCarFavoriteListener {
    private static final String PREFS_NAME = "CarTixFavorites";
    private static final String FAVORITES_KEY = "favorite_cars";
    private static final String TAG = "FavoritesManager";

    private SharedPreferences prefs;
    private Gson gson;
    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private ArrayList<Car> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Initialize SharedPreferences and Gson
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get favorites and set up the adapter
        favorites = getFavorites();
        carAdapter = new CarAdapter(favorites, this);
        recyclerView.setAdapter(carAdapter);

        // Set action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Favorites");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Improved save a car to favorites with duplicate checking
    public void addToFavorites(Car car) {
        if (car == null) {
            Log.e(TAG, "Attempted to add null car to favorites");
            return;
        }

        // Enhanced duplicate checking
        boolean isDuplicate = false;
        for (Car favoriteCar : favorites) {
            // Check by ID (primary method)
            if (favoriteCar.getId() != null && favoriteCar.getId().equals(car.getId())) {
                isDuplicate = true;
                break;
            }

            // Additional check by car name and year (secondary method if IDs differ but it's the same car)
            if (favoriteCar.getCarName() != null && favoriteCar.getYear() != null &&
                    favoriteCar.getCarName().equals(car.getCarName()) &&
                    favoriteCar.getYear().equals(car.getYear()) &&
                    favoriteCar.getMoney().equals(car.getMoney())) {
                isDuplicate = true;
                Log.d(TAG, "Duplicate car found by properties: " + car.getCarName());
                break;
            }
        }

        if (isDuplicate) {
            Log.d(TAG, "Car already in favorites: " + car.getCarName());
            Toast.makeText(this, car.getCarName() + " is already in favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add car to favorites
        car.setFavorite(true);
        favorites.add(car);
        saveFavorites(favorites);
        carAdapter.notifyDataSetChanged();

        Log.d(TAG, "Added car to favorites: " + car.getCarName());
        Toast.makeText(this, car.getCarName() + " added to favorites", Toast.LENGTH_SHORT).show();
    }

    // Remove a car from favorites
    public void removeFromFavorites(Car car) {
        if (car == null) {
            Log.e(TAG, "Attempted to remove null car from favorites");
            return;
        }

        ArrayList<Car> updatedFavorites = new ArrayList<>();
        boolean wasRemoved = false;

        // Create a new list without the car to remove
        for (Car favoriteCar : favorites) {
            if (!isMatchingCar(favoriteCar, car)) {
                updatedFavorites.add(favoriteCar);
            } else {
                wasRemoved = true;
            }
        }

        if (wasRemoved) {
            favorites = updatedFavorites;
            saveFavorites(favorites);
            carAdapter.updateData(favorites);

            Log.d(TAG, "Removed car from favorites: " + car.getCarName());
            Toast.makeText(this, car.getCarName() + " removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Car not found in favorites: " + car.getCarName());
        }
    }

    // Helper method to determine if two cars match (by ID or properties)
    private boolean isMatchingCar(Car car1, Car car2) {
        if (car1 == null || car2 == null) return false;

        // Check by ID first
        if (car1.getId() != null && car1.getId().equals(car2.getId())) {
            return true;
        }

        // Then check by name, year, and price as fallback
        return car1.getCarName() != null && car1.getYear() != null &&
                car1.getCarName().equals(car2.getCarName()) &&
                car1.getYear().equals(car2.getYear()) &&
                car1.getMoney().equals(car2.getMoney());
    }

    // Check if a car is in favorites
    public boolean isFavorite(Car car) {
        if (car == null) return false;

        for (Car favoriteCar : favorites) {
            if (isMatchingCar(favoriteCar, car)) {
                return true;
            }
        }
        return false;
    }

    // Get all favorite cars
    public ArrayList<Car> getFavorites() {
        String json = prefs.getString(FAVORITES_KEY, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Car>>() {}.getType();
        ArrayList<Car> favorites = gson.fromJson(json, type);

        return favorites != null ? favorites : new ArrayList<>();
    }

    // Save the list of favorites
    private void saveFavorites(ArrayList<Car> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(FAVORITES_KEY, json).apply();
    }

    // Update a car's favorite status
    public void updateFavoriteStatus(Car car, boolean isFavorite) {
        if (isFavorite) {
            addToFavorites(car);
        } else {
            removeFromFavorites(car);
        }
    }

    // Create a static method to access favorites from other activities
    public static ArrayList<Car> getFavoritesFromPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FAVORITES_KEY, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Car>>() {}.getType();
        ArrayList<Car> favorites = new Gson().fromJson(json, type);

        return favorites != null ? favorites : new ArrayList<>();
    }

    // Static method to check if a car is favorite
    public static boolean isCarFavorite(Context context, Car car) {
        if (car == null || context == null) return false;

        ArrayList<Car> favorites = getFavoritesFromPrefs(context);

        for (Car favoriteCar : favorites) {
            // Check by ID
            if (favoriteCar.getId() != null && favoriteCar.getId().equals(car.getId())) {
                return true;
            }

            // Also check by name, year and price (as cars might be created with different IDs)
            if (favoriteCar.getCarName() != null && favoriteCar.getYear() != null &&
                    favoriteCar.getCarName().equals(car.getCarName()) &&
                    favoriteCar.getYear().equals(car.getYear()) &&
                    favoriteCar.getMoney().equals(car.getMoney())) {
                return true;
            }
        }

        return false;
    }

    // Improved static method to add a car to favorites with duplicate checking
    public static void addCarToFavorites(Context context, Car car) {
        if (context == null || car == null) {
            Log.e(TAG, "Context or car is null in addCarToFavorites");
            return;
        }

        ArrayList<Car> favorites = getFavoritesFromPrefs(context);
        boolean isDuplicate = false;

        // Enhanced duplicate checking
        for (Car favoriteCar : favorites) {
            // Check by ID
            if (favoriteCar.getId() != null && favoriteCar.getId().equals(car.getId())) {
                isDuplicate = true;
                break;
            }

            // Check by properties
            if (favoriteCar.getCarName() != null && favoriteCar.getYear() != null &&
                    favoriteCar.getCarName().equals(car.getCarName()) &&
                    favoriteCar.getYear().equals(car.getYear()) &&
                    favoriteCar.getMoney().equals(car.getMoney())) {
                isDuplicate = true;
                Log.d(TAG, "Duplicate car found by properties: " + car.getCarName());
                break;
            }
        }

        if (isDuplicate) {
            Log.d(TAG, "Car already in favorites (static method): " + car.getCarName());
            // Can't show toast from static method, so just log
            return;
        }

        // Add car to favorites
        car.setFavorite(true);
        favorites.add(car);

        // Save to SharedPreferences
        String json = new Gson().toJson(favorites);
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(FAVORITES_KEY, json).apply();

        Log.d(TAG, "Added car to favorites (static method): " + car.getCarName());
    }

    // Static method to remove a car from favorites
    public static void removeCarFromFavorites(Context context, Car car) {
        if (context == null || car == null) {
            Log.e(TAG, "Context or car is null in removeCarFromFavorites");
            return;
        }

        ArrayList<Car> favorites = getFavoritesFromPrefs(context);
        ArrayList<Car> updatedFavorites = new ArrayList<>();
        boolean wasRemoved = false;

        // Create a new list without the car to remove
        for (Car favoriteCar : favorites) {
            boolean isMatch = false;

            // Check by ID
            if (favoriteCar.getId() != null && favoriteCar.getId().equals(car.getId())) {
                isMatch = true;
            }

            // Check by properties
            else if (favoriteCar.getCarName() != null && favoriteCar.getYear() != null &&
                    favoriteCar.getCarName().equals(car.getCarName()) &&
                    favoriteCar.getYear().equals(car.getYear()) &&
                    favoriteCar.getMoney().equals(car.getMoney())) {
                isMatch = true;
            }

            if (!isMatch) {
                updatedFavorites.add(favoriteCar);
            } else {
                wasRemoved = true;
            }
        }

        if (wasRemoved) {
            // Save to SharedPreferences
            String json = new Gson().toJson(updatedFavorites);
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(FAVORITES_KEY, json).apply();

            Log.d(TAG, "Removed car from favorites (static method): " + car.getCarName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menuHomePage) {
            startActivity(new Intent(this, HomePage.class));
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menuFavoritesHistory) {
            // No need to navigate to itself if we're already in this activity
            if (!(this instanceof FavoritesManagerActivity)) {
                startActivity(new Intent(this, FavoritesManagerActivity.class));
                finish();
            }
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

    @Override
    public void onFavoriteChanged(Car car, boolean isFavorite) {
        updateFavoriteStatus(car, isFavorite);
    }
}