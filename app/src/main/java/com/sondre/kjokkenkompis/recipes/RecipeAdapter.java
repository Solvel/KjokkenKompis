package com.sondre.kjokkenkompis.recipes;

import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sondre.kjokkenkompis.R;
import com.sondre.kjokkenkompis.shoppinglists.ItemDbHelper;
import com.sondre.kjokkenkompis.shoppinglists.ShoppingDbHelper;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class RecipeAdapter extends ArrayAdapter {

    private int resourceLayout;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private int width, height;
    private Button yes, no;

    private ShoppingDbHelper shoppingDbHelper;
    private ItemDbHelper itemDbHelper;
    private RecipesIngredientsDbHelper mRecipesIngredientsDbHelper;
    ArrayList<String> lists = new ArrayList<>();
    ArrayList<String> ingredientdata = new ArrayList<>();

    public RecipeAdapter(Context context, int resource, List<Recipe> recipeList) {
        super(context, resource, recipeList);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        RecipesDbHelper recipesDbHelper = new RecipesDbHelper(parent.getContext());
        View v = view;
        Recipe recipe = (Recipe) getItem(position);

        layoutInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup, null);
        View itemLayout = layoutInflater.inflate(R.layout.list_recipe, container, false);
        relativeLayout = (RelativeLayout) itemLayout.findViewById(R.id.list_recipe);
        yes = (Button) itemLayout.findViewById(R.id.yes);
        no = (Button) itemLayout.findViewById(R.id.no);
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
        shoppingDbHelper = new ShoppingDbHelper(getContext());
        itemDbHelper = new ItemDbHelper(getContext());
        mRecipesIngredientsDbHelper = new RecipesIngredientsDbHelper(getContext());

        if (v == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            v = layoutInflater.inflate(resourceLayout, null);
        }

        if (recipe != null) {
            //change list item properties
            TextView textView = (TextView) v.findViewById(R.id.cl_text);
            ImageView possibleView = (ImageView) v.findViewById(R.id.cl_Image);
            ImageView favoriteView = (ImageView) v.findViewById(R.id.cl_Image1);

            //if item exist in ingredientDB
            if(recipe.full == 1){
                Log.d(recipe.name,"green, all ingredients");
                possibleView.setImageResource(R.drawable.cart_green);
            }else{
                Log.d(recipe.name,"red, not all ingredinents");
                possibleView.setImageResource(R.drawable.cart_red);
            }
            if(recipe.favorite == 1){
                Log.d(recipe.name,"filled, is a favorite");
                favoriteView.setImageResource(R.drawable.starfull);
            }else{
                Log.d(recipe.name,"hollow, is not a favorite");
                favoriteView.setImageResource(R.drawable.starnull);
            }

            if (textView != null) {
                textView.setText(recipe.name);
            }
            if(recipe.favorite == 0){
                favoriteView.setImageResource(R.drawable.starnull);
            }else{
                favoriteView.setImageResource(R.drawable.starfull);
            }

            possibleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final PopupWindow popupWindow = new PopupWindow(container, width/3 * 2, height/7 * 2, true);
                    popupWindow.setAnimationStyle(R.style.translate);
                    popupWindow.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, width/2 - width/6 * 2, height/2 - height/14 * 2);

                    container.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent){
                            popupWindow.dismiss();
                            return true;
                        }
                    });

                    no = (Button) container.findViewById(R.id.no);
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });

                    yes = (Button) container.findViewById(R.id.yes);
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cursor data = shoppingDbHelper.getData();
                            Cursor ingredientsData = mRecipesIngredientsDbHelper.getData();
                            String exname;
                            try {
                                // INGREDIENTS                                                                              // Fills in the ingredients data in the textview from the database
                                while(ingredientsData.moveToNext()){
                                    //show recipe ingredients
                                    if(recipe.name.equalsIgnoreCase(ingredientsData.getString(2))){
                                        ingredientdata.add(ingredientsData.getString(1));
                                    }
                                }
                                //shoppinglist list
                                while (data.moveToNext()) {
                                    exname = data.getString(1);
                                    lists.add(exname);
                                    Log.d("list",exname);
                                }
                                if(lists.size() != 0){
                                    //add items to the first shoppinglist
                                    for(int i = 0; i < ingredientdata.size(); i++){
                                        String newEntry = ingredientdata.get(i);
                                        if(newEntry.length() != 0){
                                            AddData(newEntry, lists.get(0));
                                        }
                                    }
                                } else {
                                    //add items to the shoppinglist with recipe name
                                    shoppingDbHelper.addData(recipe.name);
                                    for(int i = 0; i < ingredientdata.size(); i++){
                                        String newEntry = ingredientdata.get(i);
                                        if(newEntry.length() != 0){
                                            AddData(newEntry, recipe.name);
                                        }
                                    }
                                }
                            } finally {
                                data.close();
                            }
                            popupWindow.dismiss();
                        }
                    });
                    popupWindow.showAsDropDown(container, 0, 0);
                }
            });

            //change favorite icon
            favoriteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recipe.favorite == 1){
                        favoriteView.setImageResource(R.drawable.starnull);
                        recipesDbHelper.updateFavorite(recipe.name, 0);
                        recipe.favorite = 0;
                    }else{
                        favoriteView.setImageResource(R.drawable.starfull);
                        recipesDbHelper.updateFavorite(recipe.name, 1);
                        recipe.favorite = 1;
                    }
                }
            });

        }
        return v;
    }

    public void AddData(String newEntry, String liste) {
        //check if name exist in db
        Cursor data = itemDbHelper.getData();
        String exname;
        while (data.moveToNext()) {
            exname = data.getString(1);
            if(newEntry.equalsIgnoreCase(exname)){
                Log.d("vare_table","These Items Are Already In Your Shopping List!" );
                toastMessage(newEntry + " finnes allerede");
                //TODO: Add a "Do you still want to add these items button"
                return;
            }
        }
        boolean insertData = itemDbHelper.addData(newEntry, liste);
        if (insertData) {
            toastMessage( newEntry + " ble lagt til " + liste);
        } else {
            toastMessage("Something went wrong");
        }
    }

    private void toastMessage(String message){
        Toast.makeText(getContext(),message, Toast.LENGTH_SHORT).show();
    }
}
