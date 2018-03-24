package com.example.shelterconnect.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shelterconnect.R;
import com.example.shelterconnect.adapters.ItemAdapter;
import com.example.shelterconnect.controller.items.ReadItemActivity;
import com.example.shelterconnect.database.Api;
import com.example.shelterconnect.database.RequestHandler;
import com.example.shelterconnect.model.Donor;
import com.example.shelterconnect.model.Employee;
import com.example.shelterconnect.model.Item;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paulv on 3/17/2018.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    EditText signInEmail, signInPassword;
    ProgressBar progressBar;
    ArrayList<Donor> donorList;
    ArrayList<Employee> workerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        signInEmail = findViewById(R.id.signInEmail);
        signInPassword = findViewById(R.id.signInPassword);
        progressBar = findViewById(R.id.loginProgressBar);

        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.sign_in).setOnClickListener(this);
    }

    private class PerformNetworkRequest extends AsyncTask<Void, Void, String> {
        String url;
        HashMap<String, String> params;
        int requestCode;

        PerformNetworkRequest(String url, HashMap<String, String> params, int requestCode) {
            this.url = url;
            this.params = params;
            this.requestCode = requestCode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(s);

                if (!object.getBoolean("error")) {

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            RequestHandler requestHandler = new RequestHandler();

            if (requestCode == Api.CODE_POST_REQUEST) {
                return requestHandler.sendPostRequest(url, params);
            }

            if (requestCode == Api.CODE_GET_REQUEST) {
                return requestHandler.sendGetRequest(url);
            }

            return null;
        }
    }

    private void readDonors() {
        LoginActivity.PerformNetworkRequest request = new LoginActivity.PerformNetworkRequest(Api.URL_READ_DONORS, null, Api.CODE_GET_REQUEST);
        request.execute();
    }

    private void readWorkers() {
        LoginActivity.PerformNetworkRequest request = new LoginActivity.PerformNetworkRequest(Api.URL_READ_EMPLOYEE, null, Api.CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshDonorList(JSONArray items) throws JSONException {
        donorList.clear();

        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.getJSONObject(i);

            System.out.println(obj);

            donorList.add(new Donor(
                    obj.getInt("donorID"),
                    obj.getString("name"),
                    obj.getString("phone"),
                    obj.getString("address"),
                    obj.getString("email")
            ));
        }
    }

    private void userLogin() {
        String email = signInEmail.getText().toString().trim();
        String password = signInPassword.getText().toString().trim();

        //Check if email is empty
        if (email.isEmpty()) {
            signInEmail.setError("Email is required.");
            signInEmail.requestFocus();
            return;
        }

        //Check if a valid email is used
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signInEmail.setError("Please enter a valid E-mail");
            signInEmail.requestFocus();
            return;
        }

        //Check if password is empty
        if (password.isEmpty()) {
            signInPassword.setError("Password is required");
            signInPassword.requestFocus();
            return;
        }

        //Check if password is at least 6 characters
        if (password.length() < 6) {
            signInPassword.setError("Minimum length of password must be 6 characters");
            signInPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            String fireBaseEmail = "";
            Donor currDonor = null;
            Employee currWorker = null;

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    fireBaseEmail = mAuth.getCurrentUser().getEmail();
                    readDonors();
                    readWorkers();
                    for (Donor d : donorList) {
                        if (fireBaseEmail.equals(d.getEmail())) {
                            currDonor = d;
                        }
                    }
                    for (Employee e : workerList) {
                        if (fireBaseEmail.equals(e.getEmail())) {
                            currWorker = e;
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                startActivity(new Intent(this, SignUp.class));

                break;
            case R.id.sign_in:
                userLogin();


                break;
        }
    }
}
