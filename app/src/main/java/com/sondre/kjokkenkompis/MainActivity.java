package com.sondre.kjokkenkompis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sondre.kjokkenkompis.ingredients.ListIngredient;
import com.sondre.kjokkenkompis.shoppinglists.Editshopping;
import com.sondre.kjokkenkompis.shoppinglists.Listshopping;
import com.sondre.kjokkenkompis.recipes.ListRecipe;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
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
        title.setText("Kjøkken Kompis");

        ImageView social = findViewById(R.id.socialview);
        social.setColorFilter(Color.argb(200,200,200,200));
    }

    public void openkjoleskap(View view){

        Intent intent = new Intent(MainActivity.this, ListIngredient.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
    public void openhandlelist(View view){

        Intent intent = new Intent(MainActivity.this, Listshopping.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
    public void openRecipeMode(View view){
        Intent intent = new Intent(MainActivity.this, ListRecipe.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
    public void openSocial(View view){
        //TODO: Create a social page                                Social.class
        toastMessage("Sosial siden er foreløpig under utvikling!");
    }
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed()
    {
        this.finishAffinity();
    }

    public void Home(View view){
        toastMessage("Du er hjemme");
    }
}