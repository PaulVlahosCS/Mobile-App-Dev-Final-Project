package com.example.shelterconnect.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.example.shelterconnect.R;
import com.example.shelterconnect.adapters.RequestAdapterDonor;
import com.example.shelterconnect.controller.items.CreateItemActivity;
import com.example.shelterconnect.controller.items.ReadItemActivity;
import com.example.shelterconnect.controller.items.UpdateItemActivity;
import com.example.shelterconnect.database.Api;
import com.example.shelterconnect.database.RequestHandler;
import com.example.shelterconnect.model.Item;
import com.example.shelterconnect.model.Request;
import com.example.shelterconnect.util.Functions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DonorHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<Request> requests;
    private ArrayList<Item> itemList;
    private ListView requestList;
    private HashMap<Integer, String> itemIDNameMap;
    private HashMap<Integer, Double> itemIDPriceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_donor_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.itemToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("DONOR HOMEPAGE");
        toolbar.setSubtitle("");

        findViewById(R.id.viewDonationsButton).setOnClickListener(this);

        this.requestList = (ListView) findViewById(R.id.requestList);

        this.requests = new ArrayList<Request>();
        this.itemList = new ArrayList<Item>();
        this.itemIDNameMap = new HashMap<>();
        this.itemIDPriceMap = new HashMap<>();

        readRequests();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.home) {

            int userLevel = Functions.getUserLevel(this);

            if (userLevel == 0 | userLevel == -1) {
                Intent myIntent = new Intent(this, DonorHomeActivity.class);
                startActivity(myIntent);
                return true;
            } else if (userLevel == 1) {
                Intent myIntent = new Intent(this, WorkerHomeActivity.class);
                startActivity(myIntent);
                return true;
            } else if (userLevel == 2) {
                Intent myIntent = new Intent(this, OrganizerHomeActivity.class);
                startActivity(myIntent);
                return true;
            }

        } else if (id == R.id.listItems) {
            Intent myIntent = new Intent(this, ReadItemActivity.class);
            startActivity(myIntent);
            return true;

        } else if (id == R.id.addItem) {
            Intent myIntent = new Intent(this, CreateItemActivity.class);
            startActivity(myIntent);
            return true;

        } else if (id == R.id.editItems) {
            Intent myIntent = new Intent(this, UpdateItemActivity.class);
            startActivity(myIntent);
            return true;
        } else if (id == R.id.logout) {

            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("userLevel", Context.MODE_PRIVATE).edit().putString("position", "").apply();
            Intent myIntent = new Intent(this, LoginActivity.class);
            startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void readRequests() {
        DonorHomeActivity.PerformNetworkRequest request = new DonorHomeActivity.PerformNetworkRequest(Api.URL_READ_REQUESTS, null, Api.CODE_GET_REQUEST);
        request.execute();
    }

    private void refreshItemList(JSONArray items) throws JSONException {
        itemList.clear();
        this.itemIDPriceMap.clear();
        this.itemIDNameMap.clear();

        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.getJSONObject(i);

            System.out.println(obj);

            Item currItem = new Item(obj.getInt("itemID"),  obj.getString("name"),
                    obj.getDouble("price"), obj.getInt("quantity"));

            this.itemIDNameMap.put(currItem.getItemID(), currItem.getName());
            this.itemIDPriceMap.put(currItem.getItemID(), currItem.getPrice());

            itemList.add(currItem);
        }

        System.out.println(this.itemIDPriceMap);
    }

    private void refreshRequestList(JSONArray items) throws JSONException {
        requests.clear();

        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.getJSONObject(i);

            int activeInt = obj.getInt("active");
            boolean active = false;

            if (activeInt == 0) {
                active = true;
            } else if (activeInt == 1) {
                active = false;
            }

            Request newRequest = new Request(
                    obj.getInt("requestID"),
                    obj.getInt("quantity"),
                    obj.getDouble("amountNeeded"),
                    obj.getDouble("amountRaised"),
                    obj.getInt("workerID"),
                    obj.getInt("itemID"),
                    active
            );

            if(this.itemIDNameMap.get(newRequest.getItemID()) != null){
                newRequest.setName(this.itemIDNameMap.get(newRequest.getItemID()));
            }

            if(this.itemIDPriceMap.get(newRequest.getItemID()) != null){
                newRequest.setItemPrice(this.itemIDPriceMap.get(obj.getInt("itemID")));
            }

            requests.add(newRequest);
        }

        RequestAdapterDonor adapter = new RequestAdapterDonor(this, this.requests);
        this.requestList.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.viewDonationsButton:
                startActivity(new Intent(this, MyDonationsActivity.class));
                break;
        }
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
                    refreshItemList(object.getJSONArray("items"));
                    refreshRequestList(object.getJSONArray("requests"));
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

}
