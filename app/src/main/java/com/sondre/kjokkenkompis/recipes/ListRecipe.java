package com.sondre.kjokkenkompis.recipes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;
import com.sondre.kjokkenkompis.ingredients.IngredientsDbHelper;


import java.util.ArrayList;
import java.util.Collections;


public class ListRecipe extends AppCompatActivity {
    private static final String TAG = "ListRecipes";
    RecipesIngredientsDbHelper recipesIngredientsDbHelper;
    RecipesDbHelper recipesDbHelper;
    IngredientsDbHelper mIngredientsDbHelper;
    public static ListView mListView;
    ArrayList<Recipe> listData;
    public ArrayList<String> result, favorite;
    public static ListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recipe);
        mListView = (ListView) findViewById(R.id.recipeView);
        recipesDbHelper = new RecipesDbHelper(this);
        mIngredientsDbHelper = new IngredientsDbHelper(this);
        recipesIngredientsDbHelper = new RecipesIngredientsDbHelper(this);
        Cursor recipeIngredientData = recipesIngredientsDbHelper.getData();
        Cursor inventoryData = mIngredientsDbHelper.getData();
        listData = new ArrayList<Recipe>();
        Cursor recipes = recipesDbHelper.getData();
        favorite = new ArrayList<>();
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swipelayout);

        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            addrecipes();
            settings.edit().putBoolean("my_first_time", false).commit();
        }


        /**************************
         *                         *
         * Recipe finder algorithm *
         *                         *
         * ************************/
        //if statment is for not crashing when 0 in list
        if(recipes.getCount() != 0) {
            // All the ingredients we have in our pantry
            Log.d("algo", " go");
            ArrayList<String> inventory = new ArrayList<>();                                            // All pantry ingredients: inventory array
            while (inventoryData.moveToNext()) {
                inventory.add(inventoryData.getString(1).toLowerCase());
            }

            // Get all the recipes ingredients and titles
            ArrayList<String> RecipeIngredient = new ArrayList<>();                                     // All the recipe's ingredients in array
            ArrayList<String> RecipeTitle = new ArrayList<>();                                          // All the recipe titles in array

            while (recipeIngredientData.moveToNext()) {
                RecipeIngredient.add(recipeIngredientData.getString(1).toLowerCase());
                RecipeTitle.add(recipeIngredientData.getString(2).toLowerCase());
            }

            String temp = RecipeTitle.get(0);
            ArrayList<String> uniqueTitles = new ArrayList<>();
            ArrayList<Integer> uniqueTitleIndexes = new ArrayList<>();
            ArrayList<String> tempRecipe = new ArrayList<>();
            ArrayList<String> singleRecipes = new ArrayList<>();
            ArrayList<Integer> singleTitleIndexes = new ArrayList<>();
            result = new ArrayList<>();

            /****************************************
             * For recipes with several ingredients *
             * *************************************/

            for (int i = 0; i < RecipeTitle.size(); i++) {                                               // Finds the duplicate recipe titles and puts them into the uniqueTitles array
                if (temp.equalsIgnoreCase(RecipeTitle.get(i))) {
                    if (!uniqueTitles.contains(RecipeTitle.get(i))) {
                        uniqueTitles.add(RecipeTitle.get(i));
                    }
                }
                temp = RecipeTitle.get(i);
            }

            for (int i = 0; i < uniqueTitles.size(); i++) {
                uniqueTitleIndexes.clear();
                tempRecipe.clear();
                for (int j = 0; j < RecipeTitle.size(); j++) {
                    if (uniqueTitles.get(i).equalsIgnoreCase(RecipeTitle.get(j))) {
                        uniqueTitleIndexes.add(j);                                                      // Indexes of duplicate recipe titles
                    }
                }

                for (int k = 0; k < uniqueTitleIndexes.size(); k++) {
                    tempRecipe.add(RecipeIngredient.get(uniqueTitleIndexes.get(k)));                    // Ingredients of the recipe
                }
                if (inventory.containsAll(tempRecipe)) {
                    result.add(RecipeTitle.get(uniqueTitleIndexes.get(0)));                             // Possible recipes with several ingredients to make from your inventory and recipes
                }
            }

            /****************************************
             * For recipes with only one ingredient *
             * *************************************/

            for (int i = 0; i < RecipeTitle.size(); i++) {
                if(!uniqueTitles.contains(RecipeTitle.get(i))){
                    singleRecipes.add(RecipeTitle.get(i));
                }
            }

            for (int i = 0; i < singleRecipes.size(); i++){                                             // Gets the titles indexes
                for (int j = 0; j < RecipeTitle.size(); j++) {
                    if (singleRecipes.get(i).equalsIgnoreCase(RecipeTitle.get(j))) {
                        singleTitleIndexes.add(j);
                    }
                }
            }

            for(int i = 0; i < singleTitleIndexes.size(); i++){
                if(inventory.contains(RecipeIngredient.get(singleTitleIndexes.get(i)))){
                    result.add(RecipeTitle.get(singleTitleIndexes.get(i)));                             // Adds the possible recipes to the result
                }
            }
        }

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
        title.setText("Mine Oppskrifter");

        /*add button*/
        FloatingActionButton fab = findViewById(R.id.fab_add_recipe);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(ListRecipe.this, AddRecipe.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateListView(); // your code
                pullToRefresh.setRefreshing(false);
            }
        });
        populateListView();
    }



    public void populateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");
        Cursor data = recipesDbHelper.getData();
        listData.clear();
        while(data.moveToNext()){
            //do i have ingredients to the recipe
            int full = 0;
            int favorite = 0;
            if(!data.getString(1).contains("null")){
                //Is it full Ingredients
                for (int i = 0; i < result.size(); i++) {
                        Log.d("result", result.get(i));
                        Log.d("getstring", data.getString(1));
                        if (data.getString(1).equalsIgnoreCase(result.get(i))) {
                            full = 1;
                            Log.d("full", "1");
                        }
                }
                //Is it favorite
                if (data.getString(3).equalsIgnoreCase("1")) {
                    favorite = 1;
                }
                Recipe recipe = new Recipe(data.getString(1), full, favorite);
                listData.add(recipe);
                Collections.sort(listData, Recipe.RecipeFavorite);
                for(Recipe str: listData){
                    Log.d("YEET: ", str.getName());
                }
            }
        }

        adapter = new RecipeAdapter(this, R.layout.custom_list_recipe, listData);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = listData.get(i).name;
                Log.d(TAG, "onItemClick: You Clicked on " + title);
                Cursor data = recipesDbHelper.getItemID(title);
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1){
                    Log.d(TAG, "onItemClick: The ID is: " + itemID);
                    Intent editScreenIntent = new Intent(ListRecipe.this, ListRecipeIngredient.class);
                    editScreenIntent.putExtra("id", itemID);
                    editScreenIntent.putExtra("title", title);
                    startActivity(editScreenIntent);
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                } else {
                    toastMessage("No ID associated with that title");
                }
            }
        });
    }

    private void addrecipes(){
        recipesDbHelper.addData("masala kylling", "legg kyllingbryst i en plastpose. bruk en kjevle for å flate litt ut hvert kyllingbryst, du vil ha dem omtrent ½ tomme tykke.\n" +
                "krydre kylling rikelig på begge sider med salt og pepper.\n" +
                "varm opp en stor stekepanne til middels varme. tilsett 2 ss smør. når smøret smelter, tilsett kyllingbryst, kok til det er gyldenbrunt på begge sider, ca 2-3 minutter per side. fjern fra pannen og videre til en plate, dekk med folie. (det er ok hvis kyllingen ikke er tilberedt hele veien, vil den koke ferdig i sausen.)\n" +
                "tilsett gjenværende smør i skilleten, når smør smelter, legg til sopp. kok 2-3 minutter til det er mykt og brunt. tilsett marsala vin, la det småkoke til det er redusert med halvparten, ca 1-2 minutter. tilsett kraftig kremaktig og salt. la det småkoke i 1 minutt og tilsett deretter kyllingbryst sammen med eventuell akkumulert juice tilbake i pannen. dekk til og la det småkoke til kyllingen er gjennomstekt og sausen har tyknet, ca 2-3 minutter. krydre etter smak med salt og pepper.");
        recipesIngredientsDbHelper.addDataIngredient("kylling", "masala kylling");
        recipesIngredientsDbHelper.addDataIngredient("smør", "masala kylling");
        recipesIngredientsDbHelper.addDataIngredient("marsala vin", "masala kylling");
        recipesIngredientsDbHelper.addDataIngredient("sjampinjong", "masala kylling");
        recipesIngredientsDbHelper.addDataIngredient("fløte", "masala kylling");

        recipesDbHelper.addData("laks i folie", "varm opp ovnen til 180c. spray 4 store stykker aluminiumsfolie med kokespray.\n" +
                "legg hver laksefilet på toppen av et stykke aluminiumsfolie. topp hver filet med ca 2 ss pesto, ¼ kopp løk, ¼ av tomatene og ca. 2 ss fetaost. det er ikke nødvendig å måle ingrediensene - bare bruk så mye eller så lite du vil!\n" +
                "forsegle aluminiumsfoliepakkene ved å brette dem over fisken og klemme godt sammen for å lukke. legg foliepakkene på et stort bakeplate og stek i ca. 25 minutter. fisk gjøres når den lett flasser med en gaffel.");
        recipesIngredientsDbHelper.addDataIngredient("laks", "laks i folie");
        recipesIngredientsDbHelper.addDataIngredient("pesto", "laks i folie");
        recipesIngredientsDbHelper.addDataIngredient("løk", "laks i folie");
        recipesIngredientsDbHelper.addDataIngredient("chrerrytomat", "laks i folie");
        recipesIngredientsDbHelper.addDataIngredient("feta ost", "laks i folie");

        recipesDbHelper.addData("hvitløk scampi pasta", "fyll en middels gryte med vann. kok opp og smak til med salt. den skal smake like salt som havet. fordi vi bare bruker 1/2 lb pasta, trenger du ikke å fylle en enorm gryte med vann, du vil at vannet skal være stivelsesholdig siden vi bruker det i sausen. jeg liker at vannet bare kommer opp ca. 1/2-tommers over pastaen.\n" +
                "varm opp en stor stekepanne til middels varme. tilsett 4 ss smør. når smør smelter, tilsett hvitløk. sauter i 2 minutter. vær forsiktig så du ikke brenner.\n" +
                "mens hvitløken koker, krydrer rekene på begge sider med salt og pepper. tilsett reker på stekepannen med smør og hvitløk. tilsett samtidig pastaen i kokende vann. kok reker til lyserosa og krøllet, ca 3-4 minutter. kok pasta til aldente.\n" +
                "når reken er ferdig, tar du den fra varmen hvis pastaen ikke er kokt ennå. (du vil ikke at de skal overkokte. når pastaen er ferdig, tilsett i reker, hvitløk og smør, kom tilbake på varmen, men reduser den til lav. tilsett rød pepperflak og 1/2 kopp pastavann. kast å kombinere, og tilsett deretter resterende 1 ss smør, kast igjen. tilsett eventuelt mer pastavann for å skape ønsket konsistens (jeg la til en annen 1/2 kopp). krydre pasta med salt og pepper.");
        recipesIngredientsDbHelper.addDataIngredient("smør", "hvitløk scampi pasta");
        recipesIngredientsDbHelper.addDataIngredient("hvitløk", "hvitløk scampi pasta");
        recipesIngredientsDbHelper.addDataIngredient("scampi", "hvitløk scampi pasta");
        recipesIngredientsDbHelper.addDataIngredient("chilli flak", "hvitløk scampi pasta");
        recipesIngredientsDbHelper.addDataIngredient("egg", "hvitløk scampi pasta");

        recipesDbHelper.addData("ravioli og spinat lasagne", "forvarm ovnen til 350 grader.\n" +
                "i en stor stekepanne over middels varme, smuldrer og kok pølse til den er brunet.\n" +
                "tilsett spinat og rør sammen til det er visnet.\n" +
                "i 9x13 panne, stek nok pastasaus til å dekke bunnen av pannen.\n" +
                "ordne et enkelt lag ravioli for å dekke bunnen av pannen. dekk med 1-1 / 2 kopper mozzarellaost og legg resterende osteravioli på toppen.\n" +
                "fordel pølse og spinatblanding jevnt over ravioli.\n" +
                "dårlig gjenværende marinara-saus over pølse og spinat og topp med gjenværende ost.\n" +
                "stek i 30 minutter eller til den er varm og boblet. la avkjøles 10 minutter før du skjærer og serverer.");
        recipesIngredientsDbHelper.addDataIngredient("italiensk pølse", "ravioli og spinat lasagne");
        recipesIngredientsDbHelper.addDataIngredient("baby spinat", "ravioli og spinat lasagne");
        recipesIngredientsDbHelper.addDataIngredient("marinara saus", "ravioli og spinat lasagne");
        recipesIngredientsDbHelper.addDataIngredient("ravioli ost", "ravioli og spinat lasagne");
        recipesIngredientsDbHelper.addDataIngredient("mozzarella ost", "ravioli og spinat lasagne");

        recipesDbHelper.addData("pannekaker", "antall prosjoner 4\n1\n" +
                "smelt smør i en kjele og la det avkjøles noe.\n" +
                "\n" +
                "2\n" +
                "bland mel og salt. tilsett ca. halvparten av melken og visp til du får en tykk og klumpfri røre.\n" +
                "\n" +
                "tips\n" +
                "det går fint an å lage en grovere variant av disse pannekakene. da bytter du ut deler av hvetemelet med lik mengde sammalt hvete, grov.\n" +
                "\n" +
                "3\n" +
                "tilsett smeltet smør til røren, deretter litt og litt av melken, og til slutt rører du inn eggene til røren blir tynn og fin.\n" +
                "\n" +
                "4\n" +
                "la pannekakerøren svelle (hvile) i minst 20 minutter før du steker pannekakene. dette er viktig for å få et godt resultat.\n" +
                "\n" +
                "5\n" +
                "smelt litt smør i en varm stekepanne. bruk en mugge eller øse til å helle i røre til den så vidt dekker hele bunnen. for å få fordelt røren jevnt, kan du dreie stekepannen rundt. stek pannekaken på middels sterk varme i 1–2 minutter, eller til røren har stivnet på oversiden. vend pannekaken og stek omtrent like lenge på den andre siden.\n" +
                "\n" +
                "6\n" +
                "legg ferdigstekte pannekaker oppå hverandre på en tallerken for å sikre at de holder seg varme. server med for eksempel blåbærsyltetøy.");
        recipesIngredientsDbHelper.addDataIngredient("smeltet smør til røren 3ss", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("hvetemel 3dl", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("salt 1/2ts", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("lettmelk 6dl", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("egg 3stk", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("smør til steking 2ss", "pannekaker");
        recipesIngredientsDbHelper.addDataIngredient("blåbærsyltetøy 2dl", "pannekaker");

        recipesDbHelper.addData("speltvafler", "antall vafler ca 9 stk\nbland alt sammen i en bolle\nstek på et vafeljern og server med syltetøy");
        recipesIngredientsDbHelper.addDataIngredient("smeltet smør 100g", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("egg 3stk", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("lettmelk 5dl", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("sukker 2ss", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("vaniljesukker 1ts", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("kardemomme 1ts", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("bakepulver 1ts", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("salt 1klype", "speltvafler");
        recipesIngredientsDbHelper.addDataIngredient("siktet speltmel 5dl", "speltvafler");
    }

    private void toastMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void Home(View view){
        Intent intent = new Intent(ListRecipe.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(ListRecipe.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
