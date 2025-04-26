package com.example.cartix;

import android.util.Patterns;

public class Check {

    public boolean checkString(String str)
    {
        if(!str.isEmpty())
            return true;
        return false;
    }
    public boolean checkPassword(String password)
    {
        if(password.length()>=6)
            return true;
        return false;
    }
    public boolean checkEmail(String email)
    {
        if(Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return true;
        return false;
    }


}
