package com.sondre.kjokkenkompis.shoppinglists;

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
import com.sondre.kjokkenkompis.ingredients.IngredientsDbHelper;

import java.util.ArrayList;

public class Listitem extends AppCompatActivity {

    private static final String TAG = "ItemDbHelper";

    private ItemDbHelper itemDbHelper;
    private IngredientsDbHelper ingredientsDb;
    private ListView mListView;
    private EditText textadditem;
    private String selectedName;
    private FloatingActionButton fabadd, fabdelete, fabing;
    ArrayList<Item> listData;

    //set 2 different states for ui
    private boolean state = false;

    //to control keyboard and viewlist
    private InputMethodManager imm;
    private ViewGroup.MarginLayoutParams mlp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);
        mListView = (ListView) findViewById(R.id.listvare);
        itemDbHelper = new ItemDbHelper(this);
        ingredientsDb = new IngredientsDbHelper(this);
        //mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listData = new ArrayList<Item>();

        //set btn and text input invisible
        textadditem = findViewById(R.id.textadditem);
        textadditem.setVisibility(View.INVISIBLE);

        //to control keyboard and viewlist
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mlp = (ViewGroup.MarginLayoutParams) mListView.getLayoutParams();

        /*Custom menu */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        View cView = getLayoutInflater().inflate(R.layout.menu, null);
        actionBar.setCustomView(cView);


        //get the intent extra from the ListDataActivity
        Intent receivedIntent = getIntent();

        //now get the name we passed as an extra
        selectedName = receivedIntent.getStringExtra("name");
        Log.d("name",selectedName);

        /*set Title*/
        TextView title = findViewById(R.id.Title);
        title.setText(selectedName);

        //show list
        populateListView();

        /*add button*/

        fabadd = findViewById(R.id.fabadd);
        fabadd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //change buttons and textview
                textadditem.setVisibility(View.VISIBLE);
                fabadd.setVisibility(View.INVISIBLE);
                fabdelete.setVisibility(View.INVISIBLE);
                fabing.setVisibility(View.INVISIBLE);

                //set focus and show keyboard
                textadditem.requestFocus();
                imm.showSoftInput(textadditem, InputMethodManager.SHOW_IMPLICIT);
                state = true;

                //change listview
                mlp.setMargins(0, 0, 0, 150);
                mListView.setLayoutParams(mlp);
            }
        });

        /*add button vare to ingredients*/
        fabing = findViewById(R.id.fabing);
        fabing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected = "";
                String name;
                int cntChoice = mListView.getCount();

                for (int i = 0; i < cntChoice; i++) {
                    Log.d("i", String.valueOf(i));
                    if(listData.get(i).getischecked()) {
                        name = listData.get(i).getName();
                        selected += listData.get(i).getName() + " , ";
                        ingredientsDb.addData(name);
                    }
                }
                toastMessage("Added " + selected + " to Ingrediens");
                populateListView();
                //set focus on last item in list
                mListView.setSelection(mListView.getAdapter().getCount()-1);
            }
        });

        /*add button slett to delete items selected*/
        fabdelete = findViewById(R.id.fabdelete);
        fabdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selected = "";
                String name;
                int check = 0;
                int cntChoice = mListView.getCount();
                for (int i = 0; i < cntChoice; i++) {
                    Log.d("i=", String.valueOf(i));
                    if (listData.get(i).getischecked()) {
                        check++;
                        name = listData.get(i).getName();
                        selected += listData.get(i).getName() + " , ";
                        itemDbHelper.deleteName(name);
                    }
                }
                if(check == 0){
                    toastMessage("Marker de du vil slette");
                }else {
                    toastMessage("Slettet " + selected);
                    populateListView();
                    //set focus on last item in list
                    mListView.setSelection(mListView.getAdapter().getCount()-1);
                }
            }
        });

        //add item via textfield
        textadditem.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String newEntry = textadditem.getText().toString();
                    String liste = selectedName;
                    //check if name exist in db
                    Cursor data = itemDbHelper.getData();
                    String exname;
                    while (data.moveToNext()) {
                        exname = data.getString(1);
                        //check if this list already has that name
                        if(newEntry.equalsIgnoreCase(exname) & liste.equalsIgnoreCase(data.getString(2))){
                            Log.d(TAG,"Name already exist" );
                            toastMessage("Navn finnes allerede");
                            return true;
                        }
                    }
                    //check if empty
                    if (textadditem.length() != 0) {
                        AddData(newEntry, liste);
                        textadditem.setText("");
                    } else {
                        toastMessage("Du må skrive noe");
                    }
                    populateListView();
                    //set focus on last item in list
                    mListView.setSelection(mListView.getAdapter().getCount()-1);
                    return true;
                }
                return false;
            }
        });
    }

    private void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        Cursor data = itemDbHelper.getData();
        Cursor ingredientdata = ingredientsDb.getData();
        listData.clear();
        while(data.moveToNext()){
            boolean isingredient = false;
            //show handlelise varer
            Log.d("item: ", data.getString(1));
            //display shoppinglist items
            if(selectedName.equalsIgnoreCase(data.getString(2))){
                //show icon if name exist in ingredientsDB
                while(ingredientdata.moveToNext()){
                    Log.d("ingredient: ", ingredientdata.getString(1));
                    String name = data.getString(1);
                    //compare this list name against ingredientsDB
                    if(name.equalsIgnoreCase(ingredientdata.getString(1))){
                        Log.d("true ", name);
                        isingredient = true;
                    }
                }
                ingredientdata.moveToFirst();
                //add item to the list
                Item item = new Item(data.getString(1),isingredient,false);
                listData.add(item);
            }
        }
        data.close();
        ingredientdata.close();
        ListAdapter adapter = new ItemAdapter(this, R.layout.custom_list_item, listData);
        mListView.setAdapter(adapter);
    }

    public void AddData(String newEntry, String liste) {

        boolean insertData = itemDbHelper.addData(newEntry, liste);
        if (insertData) {
        } else {
            toastMessage("Something went wrong");
        }
    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void Home(View view){
        Intent intent = new Intent(Listitem.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed()
    {
        if(state) {
        //hide textview and show (+)button
        textadditem.setVisibility(View.INVISIBLE);
        fabadd.setVisibility(View.VISIBLE);
        fabdelete.setVisibility(View.VISIBLE);
        fabing.setVisibility(View.VISIBLE);
        state = false;

            //change listview
            mlp.setMargins(0, 0, 0, 300);
            mListView.setLayoutParams(mlp);
    }else {
            Intent intent = new Intent(Listitem.this, Listshopping.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}