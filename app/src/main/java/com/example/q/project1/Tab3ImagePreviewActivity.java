package com.example.q.project1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

/**
 * Created by q on 2016-12-28.
 */

public class Tab3ImagePreviewActivity extends Activity {
    static final int NEW_DRAWING_ON_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab3_image_priview);

        Intent i = getIntent();
        final String file_path = i.getStringExtra("file_path");
        Uri image_uri = Uri.parse("file://" + file_path);

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.tab3_draw_on_image_btn);
        button.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Tab3ImagePreviewActivity.this, DrawActivity.class);
                i.putExtra("file_path", file_path);
                startActivityForResult(i, NEW_DRAWING_ON_IMAGE);
            }
        });

        try {
            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);
            ImageView image_view = (ImageView) findViewById(R.id.tab3_image_preview);
            image_view.setImageBitmap(image_bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode, data);
        finish();
    }
}
