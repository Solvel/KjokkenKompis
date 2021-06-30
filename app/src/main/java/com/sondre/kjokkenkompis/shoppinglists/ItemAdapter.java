package com.sondre.kjokkenkompis.shoppinglists;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.sondre.kjokkenkompis.R;
import java.util.List;

public class ItemAdapter extends ArrayAdapter {

    private int resourceLayout;
    private Context mContext;

    public ItemAdapter(Context context, int resource, List<Item> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        View v = view;
        Item item = (Item) getItem(position);

        if (v == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(mContext);
            v = layoutInflater.inflate(resourceLayout, null);
        }

        if (item != null) {
            //change list item properties
            TextView textView = (TextView) v.findViewById(R.id.cl_text);
            ImageView imageView = (ImageView) v.findViewById(R.id.cl_Image);
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.cl_check);

            //if item exist in ingredientDB
            if(item.ingredient){
                Log.d(item.name,"show");
                imageView.setVisibility(View.VISIBLE);
            }else{
                Log.d(item.name,"not show");
                imageView.setVisibility(View.INVISIBLE);
            }

            if (textView != null) {
                textView.setText(item.name);
            }

            //change checkmark to itemlist
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    item.setischecked(isChecked);
                }
            });
        }
        return v;
    }
}
