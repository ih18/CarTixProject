package com.example.cartix;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    //1
    private Button btnregister;
    private EditText Name;
    private EditText LastName;
    private EditText Email;
    private EditText password;
    private TextView login_link;
    private CheckBox showHidePass;

    FirebaseControl firebaseControl;
    Check check;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //2
        btnregister = findViewById(R.id.btnregister);
        Name = findViewById(R.id.RegisterName);
        LastName = findViewById(R.id.RegisterLastName);
        Email = findViewById(R.id.RegisterEmail);
        password = findViewById(R.id.RegisterPassword);
        showHidePass = findViewById(R.id.hideShowPassRegister);
        firebaseControl = new FirebaseControl(this);
        check = new Check();
        login_link = findViewById(R.id.login_link);
        login_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        showHidePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    password.setTransformationMethod(null);
                }
                else{
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when button is clicked
                String firstName = Name.getText().toString();
                String lastName = LastName.getText().toString();
                String email = Email.getText().toString();
                String pass = password.getText().toString();

                // בדיקת קלט
                if (!check.checkString(firstName)) {
                    Toast.makeText(RegisterActivity.this, "יש להזין שם פרטי", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkString(lastName)) {
                    Toast.makeText(RegisterActivity.this, "יש להזין שם משפחה", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "כתובת האימייל אינה תקינה", Toast.LENGTH_SHORT).show();
                }
                else if (!check.checkPassword(pass)) {
                    Toast.makeText(RegisterActivity.this, "הסיסמה לא תקינה וחייבת להיות באורך של 6 תווים לפחות", Toast.LENGTH_SHORT).show();
                }
                else {
                    firebaseControl.createUser(new UserInformation(Name.getText().toString(), LastName.getText().toString(), Email.getText().toString(), password.getText().toString()));
                    Toast.makeText(RegisterActivity.this, "Button Clicked!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

}