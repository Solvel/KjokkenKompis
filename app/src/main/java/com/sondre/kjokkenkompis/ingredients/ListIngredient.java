package com.sondre.kjokkenkompis.ingredients;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class ListIngredient extends AppCompatActivity {
    private static final String TAG = "ListDataActivity";
    private IngredientsDbHelper ingredientsDbHelper;
    private ListView mListView;
    private EditText textaddingredient;
    private FloatingActionButton fab;

    //set 2 different stats for ui
    private boolean state = false;

    //to control keyboard and viewlist
    private InputMethodManager imm;
    private ViewGroup.MarginLayoutParams mlp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_ingredient);
        mListView = (ListView) findViewById(R.id.listView);
        ingredientsDbHelper = new IngredientsDbHelper(this);

        //set btn and text input invisible
        textaddingredient = findViewById(R.id.textaddingredient);
        textaddingredient.setVisibility(View.INVISIBLE);

        //to control keyboard and viewlist
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mlp = (ViewGroup.MarginLayoutParams) mListView.getLayoutParams();

        //show list
        populateListView(mListView);

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
        title.setText("Ingredienser");

        //set add button
        fab = findViewById(R.id.fab_add_ingredient);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //change buttons and textview
                textaddingredient.setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);

                //change listview
                mlp.setMargins(0, 0, 0, 150);
                mListView.setLayoutParams(mlp);

                //set focus and show keyboard
                textaddingredient.requestFocus();
                imm.showSoftInput(textaddingredient, InputMethodManager.SHOW_IMPLICIT);
                state = true;
            }
        });

        //add item via textfield
        textaddingredient.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String newEntry = textaddingredient.getText().toString();
                    //check if name exist in db
                    Cursor data = ingredientsDbHelper.getData();
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
                    if (textaddingredient.length() != 0) {
                        AddData(newEntry);
                        textaddingredient.setText("");
                    } else {
                        toastMessage("Du må skrive noe");
                    }
                    populateListView(mListView);
                    //set focus on last item in list
                    mListView.setSelection(mListView.getAdapter().getCount()-1);
                    return true;
                }
                return false;
            }
        });
    }

    private void populateListView(ListView listView) {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        Cursor data = ingredientsDbHelper.getData();
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

                Cursor data = ingredientsDbHelper.getItemID(name);
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1){
                    Log.d(TAG, "onItemClick: The ID is: " + itemID);
                    Intent editScreenIntent = new Intent(ListIngredient.this, EditIngredient.class);
                    editScreenIntent.putExtra("id", itemID);
                    editScreenIntent.putExtra("name", name);
                    startActivity(editScreenIntent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                } else {
                    toastMessage("No ID associated with that name");
                }
            }
        });
    }

    //Massage to user
    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //Home button, onclick in menu.xml
    public void Home(View view){
        Intent intent = new Intent(ListIngredient.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    //Adding data via DBhelper
    public void AddData(String newEntry) {
        boolean insertData = ingredientsDbHelper.addData(newEntry);

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
            textaddingredient.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.VISIBLE);
            state = false;

            //change listview
            mlp.setMargins(0, 0, 0, 0);
            mListView.setLayoutParams(mlp);
        }else {
            Intent intent = new Intent(ListIngredient.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}