package com.sondre.kjokkenkompis.recipes;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;

public class AddRecipe extends AppCompatActivity implements RecyclerViewAdapter.OnItemClickListener {

    RecipesDbHelper recipesDbHelper;
    RecipesIngredientsDbHelper recipesIngredientsDbHelper;
    Button btn_insert, btnAddRecipe;
    EditText et_insert, Approach, RecipeListName;
    TextView ingredients, txtapproach;

    //set 2 different stats for ui
    private boolean state = false;

    private RecyclerView myRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAdapter myRecyclerViewAdapter;
    private ViewGroup.MarginLayoutParams mlp_approach, mlp_txtapproach;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_recipe);

        /*Custom menu */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        View cView = getLayoutInflater().inflate(R.layout.menu, null);
        actionBar.setCustomView(cView);
        /*set Title*/
        TextView title = findViewById(R.id.Title);

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
        btnAddRecipe = (Button) findViewById(R.id.btnAddRecipe);
        recipesDbHelper = new RecipesDbHelper(this);
        recipesIngredientsDbHelper = new RecipesIngredientsDbHelper(this);
        btn_insert = (Button) findViewById(R.id.btn_insert);
        et_insert = (EditText) findViewById(R.id.et_insert);
        ingredients = (TextView) findViewById(R.id.Ingredienser);

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

        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
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
                        toastMessage("Name already exist");
                        return;
                    }
                }
                //check if title or approch is empty
                if (RecipeListName.length() == 0) {
                    toastMessage("Du må gi en tittel");
                    return;
                }
                if(Approach.length() == 0){
                    toastMessage("Du må skrive en fremgangsmåte");
                    return;
                }
                //add ingredients to DB, linked with name
                if(myRecyclerViewAdapter.getItemCount() != 0){
                    for(int i = 0; i < myRecyclerViewAdapter.getItemCount(); i++) {                 // Loops through the recycleview to get items
                        AddIngredient(myRecyclerViewAdapter.getItem(i), newTitle);
                        Log.d("ingredient",i + " " + myRecyclerViewAdapter.getItem(i));
                    }
                }else{
                    toastMessage("Du må legge til ingredienser");
                    return;
                }
                //add title and approach to DB
                AddData(newTitle, newApproach);
                Log.d("Title and approch", newTitle + " and " + newApproach);
                Intent intent = new Intent(AddRecipe.this, ListRecipe.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
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

    public void AddData(String newTitle, String newApproach) {
        boolean insertData = recipesDbHelper.addData(newTitle, newApproach);
        if (insertData) {
        } else {
            toastMessage("Something went wrong");
        }
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
        Intent intent = new Intent(AddRecipe.this, MainActivity.class);
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
            Intent intent = new Intent(AddRecipe.this, ListRecipe.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}
