package com.example.cartix;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private static final String TAG = "HistoryAdapter";
    private ArrayList<HistoryItem> historyItems;
    private TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private Context context;
    private OnViewCarsClickListener carClickListener;

    // Interface for car viewing callback
    public interface OnViewCarsClickListener {
        void onViewCarsClicked(String responseText);
    }

    // ViewHolder class definition
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView queryTextView;
        TextView responseTextView;
        TextView timestampTextView;
        ImageButton speakButton;
        Button viewCarsButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            queryTextView = itemView.findViewById(R.id.textViewQuery);
            responseTextView = itemView.findViewById(R.id.textViewResponse);
            timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
            speakButton = itemView.findViewById(R.id.buttonSpeak);
            viewCarsButton = itemView.findViewById(R.id.buttonViewCars);

            // Make sure the speak button is found
            if (speakButton == null) {
                Log.e("HistoryAdapter", "Speak button not found in layout!");
            } else {
                // Force visibility
                speakButton.setVisibility(View.VISIBLE);
            }

            // Initialize as not expanded
            responseTextView.setTag(false);

            // Hide the query TextView if it exists
            if (queryTextView != null) {
                queryTextView.setVisibility(View.GONE);
            }
        }
    }

    public HistoryAdapter(Context context, ArrayList<HistoryItem> historyItems) {
        this.context = context;
        this.historyItems = historyItems != null ? historyItems : new ArrayList<>();

        // Initialize TextToSpeech once at adapter creation instead of for each ViewHolder
        initTextToSpeech();

        // If context implements the interface, set up the listener
        if (context instanceof OnViewCarsClickListener) {
            this.carClickListener = (OnViewCarsClickListener) context;
        }
    }

    private void initTextToSpeech() {
        if (textToSpeech == null && context != null) {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    ttsInitialized = (result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED);
                    Log.d(TAG, "TTS initialized successfully: " + ttsInitialized);
                } else {
                    Log.e(TAG, "TTS initialization failed with status: " + status);
                    ttsInitialized = false;
                }
            });
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleitem_history, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        try {
            if (position >= 0 && position < historyItems.size()) {
                HistoryItem currentItem = historyItems.get(position);

                if (currentItem != null) {
                    // Format response to a shorter preview if too long with null check
                    String response = currentItem.getResponse();
                    if (response != null) {
                        if (response.length() > 100) {
                            response = response.substring(0, 97) + "...";
                        }
                        holder.responseTextView.setText(response);
                    } else {
                        holder.responseTextView.setText("No response");
                    }

                    // Set timestamp with null check
                    holder.timestampTextView.setText(currentItem.getTimestamp() != null ?
                            currentItem.getTimestamp() : "Unknown time");

                    // IMPORTANT: Always ensure speak button is visible
                    if (holder.speakButton != null) {
                        holder.speakButton.setVisibility(View.VISIBLE);

                        holder.speakButton.setOnClickListener(v -> {
                            if (ttsInitialized && currentItem.getResponse() != null) {
                                textToSpeech.speak(currentItem.getResponse(),
                                        TextToSpeech.QUEUE_FLUSH, null, "history_tts_" + position);
                            } else {
                                Toast.makeText(v.getContext(), "Text-to-Speech not available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e(TAG, "Speak button is null for position " + position);
                    }

                    // Set up view cars button
                    holder.viewCarsButton.setOnClickListener(v -> {
                        if (carClickListener != null && currentItem.getResponse() != null) {
                            carClickListener.onViewCarsClicked(currentItem.getResponse());
                        }
                    });

                    // Handle item expansion on click
                    holder.itemView.setOnClickListener(v -> {
                        // Toggle between showing preview and full response
                        boolean isExpanded = holder.responseTextView.getTag() != null &&
                                (boolean) holder.responseTextView.getTag();

                        if (isExpanded) {
                            // Show preview
                            String previewText = currentItem.getResponse();
                            if (previewText != null && previewText.length() > 100) {
                                previewText = previewText.substring(0, 97) + "...";
                            }
                            holder.responseTextView.setText(previewText != null ?
                                    previewText : "No response");
                        } else {
                            // Show full response
                            holder.responseTextView.setText(currentItem.getResponse() != null ?
                                    currentItem.getResponse() : "No response");
                        }

                        holder.responseTextView.setTag(!isExpanded);
                    });
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that might occur during binding
            Log.e(TAG, "Error binding view holder: " + e.getMessage(), e);
            holder.responseTextView.setText("There was a problem loading this history item");
        }
    }

    @Override
    public int getItemCount() {
        return historyItems != null ? historyItems.size() : 0;
    }
}