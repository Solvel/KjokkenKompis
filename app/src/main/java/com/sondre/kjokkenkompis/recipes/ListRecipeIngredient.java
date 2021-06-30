package com.sondre.kjokkenkompis.recipes;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.CaseMap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sondre.kjokkenkompis.MainActivity;
import com.sondre.kjokkenkompis.R;
import com.sondre.kjokkenkompis.shoppinglists.ItemDbHelper;
import com.sondre.kjokkenkompis.shoppinglists.ShoppingDbHelper;

import java.util.ArrayList;
import java.util.Locale;


public class ListRecipeIngredient extends AppCompatActivity {

    private static final String TAG = "addrecipe";

    RecipesIngredientsDbHelper mRecipesIngredientsDbHelper;
    RecipesDbHelper mRecipesDbHelper;
    private ShoppingDbHelper shoppingDbHelper;
    private ItemDbHelper itemDbHelper;
    ImageButton btn_start_pause, timer;
    TextView textview_countdown;
    FloatingActionButton fabEdit;

    private ListView ListIngredientsView;
    private TextView ApproachView;
    private EditText timepicker;
    private int selectedID;
    private String selectedTitle;
    private CountDownTimer countDownTimer;
    private boolean TimerRunning;
    private long StartTimeInMillis;
    private long TimeLeftInMillis;
    private long EndTime;
    private boolean state = false;
    private Button addIngredients;
    private Vibrator v;
    public Ringtone r;
    public Uri notification;
    private InputMethodManager imm;

    ArrayList<String> listData = new ArrayList<>();
    ArrayList<String> lists = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recipe_ingredients);
        ListIngredientsView = (ListView) findViewById(R.id.listingredients);
        ApproachView = (TextView) findViewById(R.id.recipeapproach);
        fabEdit = findViewById(R.id.fab_edit_recipe_ingredients);

        mRecipesIngredientsDbHelper = new RecipesIngredientsDbHelper(this);
        mRecipesDbHelper = new RecipesDbHelper(this);
        shoppingDbHelper = new ShoppingDbHelper(this);
        itemDbHelper = new ItemDbHelper(this);
        textview_countdown = (TextView) findViewById(R.id.textview_countdown);
        btn_start_pause = (ImageButton) findViewById(R.id.btn_start_pause);
        timepicker =(EditText) findViewById(R.id.timePicker);
        timer = findViewById(R.id.timer);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);


        //hide timerstuff
        btn_start_pause.setVisibility(View.INVISIBLE);
        timepicker.setVisibility(View.INVISIBLE);


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
        selectedTitle = receivedIntent.getStringExtra("title");

        /*set Title*/
        TextView title = findViewById(R.id.Title);
        title.setText(selectedTitle);

        addIngredients = (Button) findViewById(R.id.addIngredients);
        addIngredients.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Cursor data = shoppingDbHelper.getData();
                String exname;
                try {
                    //shoppinglist list
                    while (data.moveToNext()) {
                        exname = data.getString(1);
                        lists.add(exname);
                    }
                    if(lists.size() != 0){
                        //add items to the first shoppinglist
                        for(int i = 0; i < listData.size(); i++){
                            String newEntry = listData.get(i);
                            if(newEntry.length() != 0){
                                AddData(newEntry, lists.get(0));
                            }
                        }
                    } else {
                        //add items to the shoppinglist with recipe name
                        shoppingDbHelper.addData(selectedTitle);
                        for(int i = 0; i < listData.size(); i++){
                            String newEntry = listData.get(i);
                            if(newEntry.length() != 0){
                                AddData(newEntry, selectedTitle);
                            }
                        }
                    }
                } finally {
                    data.close();
                }
            }
        });

        btn_start_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TimerRunning){
                    pauseTimer();
                }else {
                    startTimer();
                }
            }
        });

        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRecipeIngredient.this, EditRecipe.class);
                intent.putExtra("title", selectedTitle);
                intent.putExtra("id", selectedID);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state){
                 state = false;
                    btn_start_pause.setVisibility(View.INVISIBLE);
                    timepicker.setVisibility(View.INVISIBLE);
                    fabEdit.setVisibility(View.VISIBLE);
                    addIngredients.setVisibility(View.VISIBLE);
                }else{
                    state = true;
                    btn_start_pause.setVisibility(View.VISIBLE);
                    timepicker.setVisibility(View.VISIBLE);
                    fabEdit.setVisibility(View.INVISIBLE);
                    addIngredients.setVisibility(View.INVISIBLE);
                    timepicker.requestFocus();
                    imm.showSoftInput(timepicker, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        timepicker.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String input = timepicker.getText().toString();
                    if (input.length() == 0) {
                        toastMessage("Du må skrive noe");
                        return true;
                    }
                    long millisInput = Long.parseLong(input) * 60000;
                    if (millisInput == 0) {
                        toastMessage("Skriv et positivt tall");
                        return true;
                    }
                    setTime(millisInput);
                    timepicker.setText("");
                    return true;
                }
                return false;
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
        Cursor ingredientsData = mRecipesIngredientsDbHelper.getData();
        Cursor recipeData = mRecipesDbHelper.getData();

        // INGREDIENTS                                                                              // Fills in the ingredients data in the textview from the database
        while(ingredientsData.moveToNext()){
            //show recipe ingredients
            if(selectedTitle.equalsIgnoreCase(ingredientsData.getString(2))){
                listData.add(ingredientsData.getString(1));
            }
        }

        // APPROACH                                                                                 // Fills in the approach in the textview from the database
        ApproachView.setMovementMethod(new ScrollingMovementMethod());
        while(recipeData.moveToNext()){
            if(selectedTitle.equalsIgnoreCase(recipeData.getString(1))){
                ApproachView.setText(recipeData.getString(2));
            }
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        ListIngredientsView.setAdapter(adapter);
    }

    private void setTime(long milliseconds) {
        StartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer(){
        EndTime = System.currentTimeMillis() + TimeLeftInMillis;

        countDownTimer = new CountDownTimer(TimeLeftInMillis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                updateButtons();
                //show stop timer popup
                //show_Notification(); this is more advanced notification, but doesn't work
                //play sound
                //doesn't work outside the app
                try {
                    notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //vibrate
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(1000);
                }
            }
        }.start();
        TimerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        TimerRunning = false;
        updateButtons();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void resetTimer() {
        TimeLeftInMillis = StartTimeInMillis;
        updateCountDownText();
        updateButtons();
    }

    private void updateCountDownText(){
        int hours = (int) (TimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((TimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        textview_countdown.setText(timeLeftFormatted);
    }
    private void updateButtons() {
        if (TimerRunning) {
            btn_start_pause.setImageResource(R.drawable.pause);
        } else {
            btn_start_pause.setImageResource(R.drawable.play);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("millisLeft", TimeLeftInMillis);
        editor.putBoolean("timerRunning", TimerRunning);
        editor.putLong("endTime", EndTime);
        editor.apply();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        TimeLeftInMillis = prefs.getLong("millisLeft", 600000);
        TimerRunning = prefs.getBoolean("timerRunning", false);
        updateCountDownText();
        updateButtons();
        if (TimerRunning) {
            EndTime = prefs.getLong("endTime", 0);
            TimeLeftInMillis = EndTime - System.currentTimeMillis();
            if (TimeLeftInMillis < 0) {
                TimeLeftInMillis = 0;
                TimerRunning = false;
                updateCountDownText();
                updateButtons();
            } else {
                startTimer();
            }
        }
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

    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }

    public void Home(View view){
        Intent intent = new Intent(ListRecipeIngredient.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed()
    {
        if(state){
            state = false;
            btn_start_pause.setVisibility(View.INVISIBLE);
            timepicker.setVisibility(View.INVISIBLE);
            fabEdit.setVisibility(View.VISIBLE);
        }else {
            Intent intent = new Intent(ListRecipeIngredient.this, ListRecipe.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }
    }
}