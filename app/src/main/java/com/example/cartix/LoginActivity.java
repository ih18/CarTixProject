package com.example.cartix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText emailLogin;
    private EditText passwordLogin;
    private FirebaseControl firebaseControl;
    private TextView signin_link;
    private Check check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //2
        btnLogin = findViewById(R.id.btnlogin);
        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.loginpassword);
        signin_link = findViewById(R.id.signin_link);
        signin_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        firebaseControl = new FirebaseControl(this);
        if (firebaseControl.isConnect())
        {
            startActivity(new Intent(this, FavoritesManagerActivity.class));
        }
        check = new Check();



        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailLogin.getText().toString();
                String password = passwordLogin.getText().toString();

                if (!check.checkString(email)) {
                    Toast.makeText(LoginActivity.this, "שדה האימייל ריק, נא למלא אימייל", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkEmail(email)) {
                    Toast.makeText(LoginActivity.this, "האימייל אינו תקין", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkString(password)) {
                    Toast.makeText(LoginActivity.this, "שדה הסיסמה ריק, נא למלא סיסמה", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkPassword(password)) {
                    Toast.makeText(LoginActivity.this, "הסיסמה צריכה להכיל לפחות 6 תווים", Toast.LENGTH_SHORT).show();
                }
                else {
                    firebaseControl.logInUser(emailLogin.getText().toString(), passwordLogin.getText().toString());
                }
            }
        });
    }






}