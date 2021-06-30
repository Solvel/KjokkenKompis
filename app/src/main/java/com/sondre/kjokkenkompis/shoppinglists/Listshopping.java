package com.sondre.kjokkenkompis.shoppinglists;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;

import java.util.ArrayList;

public class Listshopping extends AppCompatActivity {
    private static final String TAG = "ListDataActivity";
    private ShoppingDbHelper shoppingDbHelper;
    private ListView listView;
    private EditText textaddshopping;
    private FloatingActionButton fabadd;

    //set 2 different stats for ui
    private boolean state = false;

    //to control keyboard and viewlist
    private InputMethodManager imm;
    private ViewGroup.MarginLayoutParams mlp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_shopping);
        listView = (ListView) findViewById(R.id.shoppingView);
        shoppingDbHelper = new ShoppingDbHelper(this);
        mlp = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();

        //set btn and text input invisible
        textaddshopping = findViewById(R.id.textaddshopping);
        textaddshopping.setVisibility(View.INVISIBLE);

        //to control keyboard and viewlist
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mlp = (ViewGroup.MarginLayoutParams) listView.getLayoutParams();

        //show list
        populateListView();

        //Custom menu
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        View cView = getLayoutInflater().inflate(R.layout.menu, null);
        actionBar.setCustomView(cView);

        //set Title
        TextView title = findViewById(R.id.Title);
        title.setText("Handlelister");

        //set add button
        fabadd = findViewById(R.id.fab_add_shopping);
        fabadd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //change buttons and textview
                textaddshopping.setVisibility(View.VISIBLE);
                fabadd.setVisibility(View.INVISIBLE);

                //change listview
                mlp.setMargins(0, 0, 0, 150);
                listView.setLayoutParams(mlp);

                //set focus and show keyboard
                textaddshopping.requestFocus();
                imm.showSoftInput(textaddshopping, InputMethodManager.SHOW_IMPLICIT);
                state = true;
            }
        });


        //add item via textfield
        textaddshopping.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //add data
                    String newEntry = textaddshopping.getText().toString();
                    //check if name exist in db
                    Cursor data = shoppingDbHelper.getData();
                    String exname;
                    while (data.moveToNext()) {
                        exname = data.getString(1);
                        if(newEntry.equalsIgnoreCase(exname)){
                            Log.d(TAG,"Name already exist" );
                            toastMessage("Navn finnes allerede");
                            return true;
                        }
                    }
                    //check if empty
                    if (textaddshopping.length() != 0) {
                        AddData(newEntry);
                        textaddshopping.setText("");
                    } else {
                        toastMessage("Du må skrive noe i tekstfeltet!");
                    }
                    //open new list
                    Intent editScreenIntent = new Intent(Listshopping.this, Listitem.class);
                    editScreenIntent.putExtra("id", data.getCount()+1);
                    editScreenIntent.putExtra("name", newEntry);
                    startActivity(editScreenIntent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    return true;
                }
                return false;
            }
        });
    }

    private void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        Cursor data = shoppingDbHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while(data.moveToNext()){
            listData.add(data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);

        //making items clickable
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "onItemClick: You Clicked on " + name);

                Cursor data = shoppingDbHelper.getItemID(name);
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1){
                    Log.d(TAG, "onItemClick: The ID is: " + itemID);
                    Intent editScreenIntent = new Intent(Listshopping.this, Listitem.class);
                    editScreenIntent.putExtra("id", itemID);
                    editScreenIntent.putExtra("name", name);
                    startActivity(editScreenIntent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                } else {
                    toastMessage("No ID associated with that name");
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "onItemClick: You Clicked on " + name);

                Cursor data = shoppingDbHelper.getItemID(name);
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1) {
                    Log.d(TAG, "onItemClick: The ID is: " + itemID);
                    Intent editScreenIntent = new Intent(Listshopping.this, Editshopping.class);
                    editScreenIntent.putExtra("id", itemID);
                    editScreenIntent.putExtra("name", name);
                    startActivity(editScreenIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                return true;
            }
        });
    }

    //Massage to user
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //Home button, onclick in menu.xml
    public void Home(View view){
        Intent intent = new Intent(Listshopping.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    //Adding data via DBhelper
    public void AddData(String newEntry) {
        boolean insertData = shoppingDbHelper.addData(newEntry);

        if (insertData) {
        } else {
            toastMessage("Something went wrong");
        }
    }

    @Override //System backbutton logic
    public void onBackPressed()
    {
        if(state){
            //hide textview and show (+)button
            textaddshopping.setVisibility(View.INVISIBLE);
            fabadd.setVisibility(View.VISIBLE);
            state = false;

            //change listview
            mlp.setMargins(0, 0, 0, 0);
            listView.setLayoutParams(mlp);
        }else {
            Intent intent = new Intent(Listshopping.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}