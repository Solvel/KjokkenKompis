package com.sondre.kjokkenkompis.recipes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;
import com.sondre.kjokkenkompis.ingredients.EditIngredient;
import com.sondre.kjokkenkompis.ingredients.ListIngredient;

import java.util.ArrayList;
import java.util.Locale;

public class EditRecipe extends AppCompatActivity implements RecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = "EditRecipe";
    RecipesDbHelper recipesDbHelper;
    RecipesIngredientsDbHelper recipesIngredientsDbHelper;
    Button btn_insert, deleteRecipe, updateRecipe;
    EditText et_insert, Approach, RecipeListName;
    TextView ingredients, txtapproach;


    //set 2 different stats for ui
    private boolean state = false;
    private RecyclerView myRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAdapter myRecyclerViewAdapter;
    private ViewGroup.MarginLayoutParams mlp_approach, mlp_txtapproach;
    private int selectedID;
    private String selectedTitle;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe);

        /*Custom menu */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        View cView = getLayoutInflater().inflate(R.layout.menu, null);
        actionBar.setCustomView(cView);

        RecipeListName = findViewById(R.id.RecipeListName);
        Approach = findViewById(R.id.Approach);
        txtapproach = findViewById(R.id.txtApproach);
        myRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        linearLayoutManager =  new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        myRecyclerViewAdapter = new RecyclerViewAdapter(this);
        myRecyclerViewAdapter.setOnItemClickListener(this);
        myRecyclerView.setAdapter(myRecyclerViewAdapter);
        myRecyclerView.setLayoutManager(linearLayoutManager);
        mlp_approach = (ViewGroup.MarginLayoutParams) Approach.getLayoutParams();
        mlp_txtapproach = (ViewGroup.MarginLayoutParams) txtapproach.getLayoutParams();
        deleteRecipe = (Button) findViewById(R.id.deleteRecipe);
        updateRecipe = (Button) findViewById(R.id.updateRecipe);
        recipesDbHelper = new RecipesDbHelper(this);
        recipesIngredientsDbHelper = new RecipesIngredientsDbHelper(this);
        btn_insert = (Button) findViewById(R.id.btn_insert);
        et_insert = (EditText) findViewById(R.id.et_insert);
        ingredients = (TextView) findViewById(R.id.Ingredienser);


        //get the intent extra from the ListDataActivity
        Intent receivedIntent = getIntent();

        //now get the itemID we passed as an extra
        selectedID = receivedIntent.getIntExtra("id",-1); //NOTE: -1 is just the default value

        //now get the name we passed as an extra
        selectedTitle = receivedIntent.getStringExtra("title");
        Log.d("ID", String.valueOf(selectedID));
        Log.d("Name,", selectedTitle);

        /*set Title*/
        TextView title = findViewById(R.id.Title);
        title.setText(selectedTitle);

        btn_insert.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String newItem = et_insert.getText().toString();
                if(!newItem.equals("")){
                    if(myRecyclerViewAdapter.getItemCount()>=1){
                        myRecyclerViewAdapter.add(myRecyclerViewAdapter.getItemCount(), newItem);
                        et_insert.setText("");
                    }else{
                        myRecyclerViewAdapter.add(0, newItem);
                        et_insert.setText("");
                    }
                }
            }
        });

        Approach.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    state = true;

                    //make invisible
                    myRecyclerView.setVisibility(View.INVISIBLE);
                    et_insert.setVisibility(View.INVISIBLE);
                    btn_insert.setVisibility(View.INVISIBLE);
                    ingredients.setVisibility(View.INVISIBLE);

                    //change positions
                    mlp_approach.setMargins(0, 600, 0, 0);
                    Approach.setLayoutParams(mlp_approach);
                    mlp_txtapproach.setMargins(0,400,0,0);
                    txtapproach.setLayoutParams(mlp_txtapproach);
                }else{
                    state = false;

                    //make visible
                    myRecyclerView.setVisibility(View.VISIBLE);
                    et_insert.setVisibility(View.VISIBLE);
                    btn_insert.setVisibility(View.VISIBLE);
                    ingredients.setVisibility(View.VISIBLE);

                    //change positions
                    mlp_approach.setMargins(0, convert(430), 0, 0);
                    Approach.setLayoutParams(mlp_approach);
                    mlp_txtapproach.setMargins(0,convert(390),0,0);
                    txtapproach.setLayoutParams(mlp_txtapproach);
                }
            }
        });

        deleteRecipe.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                recipesDbHelper.deleteName(selectedID,selectedTitle);
                recipesIngredientsDbHelper.deleteName(selectedTitle);
                toastMessage("slettet " + selectedTitle + " fra databasen");
                Intent intent = new Intent(EditRecipe.this, ListRecipe.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        updateRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("start","add recipe");
                String newTitle = RecipeListName.getText().toString();                              // Getting the title
                String newApproach = Approach.getText().toString();                                 // Getting the approach

                //check if name exist in db
                Cursor data = recipesDbHelper.getData();
                String exname;
                while (data.moveToNext()) {
                    exname = data.getString(1);
                    if(newTitle.equalsIgnoreCase(exname)){
                        Log.d("recipe_table","Name already exist" );
                        toastMessage("Navn finnes allerede");
                        return;
                    }
                }
                //check if title or approch is empty
                if (RecipeListName.length() == 0) {
                    newTitle = selectedTitle;
                }
                if(Approach.length() == 0){
                    toastMessage("Du må skrive en fremgangsmåte");
                    return;
                }
                //add ingredients to DB, linked with name
                if(myRecyclerViewAdapter.getItemCount() != 0){
                    //delete previous data
                    recipesIngredientsDbHelper.deleteName(selectedTitle);
                    for(int i = 0; i < myRecyclerViewAdapter.getItemCount(); i++) {                 // Loops through the recycleview to get items
                        AddIngredient(myRecyclerViewAdapter.getItem(i), newTitle);
                        Log.d("ingredient",i + " " + myRecyclerViewAdapter.getItem(i));
                    }
                }else{
                    toastMessage("Du må legge til ingredienser");
                    return;
                }
                data.close();
                //add title and approach to DB
                recipesDbHelper.updateName(newTitle,selectedID,selectedTitle,newApproach);
                Log.d("Title and approch", newTitle + " and " + newApproach);
                Intent intent = new Intent(EditRecipe.this, ListRecipeIngredient.class);
                intent.putExtra("title", newTitle);
                intent.putExtra("id", selectedID);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
        populateListView();
    }

    private void populateListView() {

        /*******************************
         * title: recipeingredienttitle *
         * ingredients: listingredients *
         * approach: recipeapproach     *
         * *****************************/

        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        Cursor ingredientsData = recipesIngredientsDbHelper.getData();
        Cursor recipeData = recipesDbHelper.getData();

        // show INGREDIENTS data                                                                              // Fills in the ingredients data in the textview from the database
        while(ingredientsData.moveToNext()){

            //show recipe ingredients
            if(selectedTitle.equalsIgnoreCase(ingredientsData.getString(2))){
                if(myRecyclerViewAdapter.getItemCount()>=1){
                    //rest of the ingredients in order
                    myRecyclerViewAdapter.add(myRecyclerViewAdapter.getItemCount(), ingredientsData.getString(1));
                }else{
                    //fist ingredient
                    myRecyclerViewAdapter.add(0, ingredientsData.getString(1));
                }
            }
        }
        ingredientsData.close();

        //show APPROACH data                                                                                // Fills in the approach in the textview from the database
        while(recipeData.moveToNext()){
            if(selectedTitle.equalsIgnoreCase(recipeData.getString(1))){
                Approach.setText(recipeData.getString(2));
            }
        }
        recipeData.close();

        myRecyclerView.setAdapter(myRecyclerViewAdapter);
    }

    //convert px to dp
    public int convert (int input){
        Resources r = this.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                input,
                r.getDisplayMetrics()
        );
        return px;
    }

    @Override
    public void onItemClick(RecyclerViewAdapter.ItemHolder item, int position) {
        Toast.makeText(this, "Remove " + position + " : " + item.getItemName(), Toast.LENGTH_SHORT).show();
        myRecyclerViewAdapter.remove(position);
    }


    public void AddIngredient(String ingredients, String list) {
        boolean insertData = recipesIngredientsDbHelper.addDataIngredient(ingredients, list);
        if (insertData) {
        } else {
            toastMessage("Something went wrong");
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void Home(View view){
        Intent intent = new Intent(EditRecipe.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
    @Override
    public void onBackPressed()
    {
        if(state){
            state = false;

            //make visible
            myRecyclerView.setVisibility(View.VISIBLE);
            et_insert.setVisibility(View.VISIBLE);
            btn_insert.setVisibility(View.VISIBLE);
            ingredients.setVisibility(View.VISIBLE);

            //change positions
            mlp_approach.setMargins(0, convert(430), 0, 0);
            Approach.setLayoutParams(mlp_approach);
            mlp_txtapproach.setMargins(0,convert(390),0,0);
            txtapproach.setLayoutParams(mlp_txtapproach);
            Approach.clearFocus();
        }else {
            Intent intent = new Intent(EditRecipe.this, ListRecipeIngredient.class);
            intent.putExtra("id", selectedID);
            intent.putExtra("title", selectedTitle);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}
