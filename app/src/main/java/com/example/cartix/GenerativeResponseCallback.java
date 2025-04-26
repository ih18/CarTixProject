package com.example.cartix;

public interface GenerativeResponseCallback {
    void onResponse(String response);
    void onError(Throwable error);
}
