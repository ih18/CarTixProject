package com.example.cartix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder>
{
    private ArrayList<Car> cars;
    private OnCarFavoriteListener favoriteListener;

    // Interface for favorite click callbacks
    public interface OnCarFavoriteListener {
        void onFavoriteChanged(Car car, boolean isFavorite);
    }

    public CarAdapter(ArrayList<Car> cars) {
        this.cars = cars;
    }

    public CarAdapter(ArrayList<Car> cars, OnCarFavoriteListener listener) {
        this.cars = cars;
        this.favoriteListener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View carView;
        carView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleitem_favorites, parent, false);
        return new CarViewHolder(carView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car currentCar = cars.get(position);
        holder.carNameTextView.setText(String.valueOf(currentCar.getCarName()));
        holder.yearTextView.setText(String.valueOf(currentCar.getYear()));
        holder.moneyTextView.setText(String.valueOf(currentCar.getMoney()));

        // Set the checkbox state based on favorite status
        if (holder.favoriteCheckBox != null) {
            holder.favoriteCheckBox.setChecked(currentCar.isFavorite());

            // Set up the checkbox click listener
            holder.favoriteCheckBox.setOnClickListener(v -> {
                boolean isChecked = holder.favoriteCheckBox.isChecked();
                currentCar.setFavorite(isChecked);

                // Notify listener about favorite status change
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteChanged(currentCar, isChecked);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    // Method to update the dataset
    public void updateData(ArrayList<Car> newCars) {
        this.cars = newCars;
        notifyDataSetChanged();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {

        public TextView carNameTextView;
        public TextView yearTextView;
        public TextView moneyTextView;
        public CheckBox favoriteCheckBox;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carNameTextView = itemView.findViewById(R.id.textView16);
            yearTextView = itemView.findViewById(R.id.textView20);
            moneyTextView = itemView.findViewById(R.id.textView15);
            favoriteCheckBox = itemView.findViewById(R.id.checkBoxFavorite);
        }
    }
}