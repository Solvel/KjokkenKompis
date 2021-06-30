package com.sondre.kjokkenkompis.shoppinglists;
import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by User on 2/28/2017.
 */

public class Editshopping extends AppCompatActivity {

    private static final String TAG = "EditDataActivity";

    private Button btnSave,btnDelete;
    private EditText editable_item;

    ShoppingDbHelper shoppingDbHelper;
    ItemDbHelper itemDbHelper;

    private String selectedName;
    private int selectedID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_ingredient);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        editable_item = (EditText) findViewById(R.id.editable_item);
        shoppingDbHelper = new ShoppingDbHelper(this);
        itemDbHelper = new ItemDbHelper(this);

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

        //now get the itemID we passed as an extra
        selectedID = receivedIntent.getIntExtra("id",-1); //NOTE: -1 is just the default value

        //now get the name we passed as an extra
        selectedName = receivedIntent.getStringExtra("name");

        //set the text to show the current selected name
        editable_item.setText(selectedName);

        /*set Title*/
        TextView title = findViewById(R.id.Title);
        title.setText("Endre Ingrediens "+ selectedName);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newlistname = editable_item.getText().toString();
                if(!newlistname.equals("")){
                    //update name to shoppinglist and keeping items.
                    shoppingDbHelper.updateName(newlistname,selectedID,selectedName);
                    itemDbHelper.updatelist(newlistname,selectedName);
                    toastMessage("Nytt navn");
                    Intent intent = new Intent(Editshopping.this, Listshopping.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                }else{
                    toastMessage("Du må skrive et navn");
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shoppingDbHelper.deleteName(selectedID,selectedName);
                itemDbHelper.deletelist(selectedName);
                editable_item.setText("");
                toastMessage("slettet " + selectedName + " fra databasen");
                Intent intent = new Intent(Editshopping.this, Listshopping.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

    }

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void Home(View view){
        Intent intent = new Intent(Editshopping.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Editshopping.this, Listshopping.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}