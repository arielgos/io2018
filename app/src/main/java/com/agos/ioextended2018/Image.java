package com.agos.ioextended2018;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.agos.ioextended2018.model.Item;
import com.androidquery.AQuery;

public class Image extends AppCompatActivity {

    private Item item;

    private AQuery aquery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        item = (Item) getIntent().getExtras().getSerializable("item");

        aquery = new AQuery(this);
        aquery.id(R.id.profile_image).image(item.getUserImage());
        aquery.id(R.id.image).image("https://firebasestorage.googleapis.com/v0/b/ioextended-901ad.appspot.com/o/" + item.getUrl().replace("Pic", "resized-Pic") + "?alt=media");

        ((TextView) findViewById(R.id.profile_name)).setText(item.getUser());
        ((TextView) findViewById(R.id.text)).setText(item.getText());
        ((TextView) findViewById(R.id.labels)).setText(item.getLabels());

    }
}
