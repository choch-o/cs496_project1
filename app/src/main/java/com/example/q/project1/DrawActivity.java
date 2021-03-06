package com.example.q.project1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.UUID;

import static java.security.AccessController.getContext;

/**
 * Created by q on 2016-12-27.
 */

public class DrawActivity extends Activity implements View.OnClickListener {
    static final int FROM_DRAWING_ON_IMAGE = 101;
    static final int FROM_DRAWING_NEW = 100;

    private FrameLayout frameLayout;
    private boolean new_draw;
    private DrawingView drawView;
    private ImageView currPaintView;
    private ImageView backgroundView;

    private float menuHeight;

    private float smallBrush, mediumBrush, largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        // Find views
        backgroundView = (ImageView) findViewById(R.id.background_view);
        drawView = (DrawingView)findViewById(R.id.drawing_view);
        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        Intent i = getIntent();

        // Get screen width and height
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float parentWidth = displaymetrics.widthPixels;
        float parentHeight = displaymetrics.heightPixels;

        // Get menu bar height
        final LinearLayout menuBar = (LinearLayout) findViewById(R.id.draw_menu_bar);
        ViewTreeObserver vto = menuBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                menuBar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                menuHeight = menuBar.getMeasuredHeight();
            }
        });
        parentHeight = parentHeight - menuHeight;

        // adjust image size
        try {
            String file_path = i.getExtras().getString("file_path");
            Uri image_uri = Uri.parse("file://" + file_path);
            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image_uri);

            float imgWidth = image_bitmap.getWidth();
            float imgHeight = image_bitmap.getHeight();

            ViewGroup.LayoutParams params = frameLayout.getLayoutParams();
            if ((parentHeight / parentWidth) > (imgHeight / imgWidth)) {
                params.width = (int) parentWidth;
                params.height = (int) (imgHeight * ( parentWidth / imgWidth));
            } else {
                params.width = (int) (imgWidth * (parentHeight / imgHeight));
                params.height = (int) parentHeight;
            }
            int frameWidth = params.width;
            int frameHeight = params.height;
            frameLayout.setLayoutParams(params);

            params = drawView.getLayoutParams();
            params.width = frameWidth;
            params.height = frameHeight;
            drawView.setLayoutParams(params);

            params = backgroundView.getLayoutParams();
            params.width = frameWidth;
            params.height = frameHeight;
            backgroundView.setLayoutParams(params);

            backgroundView.setImageBitmap(image_bitmap);
            new_draw = false;
        } catch (Exception e) {
            drawView.setBackgroundColor(getResources().getColor(R.color.white));
            new_draw = true;
        }

        Button redBrush = (Button) findViewById(R.id.palette_red);
        redBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        redBrush.setOnClickListener(this);
        Button orangeBrush = (Button) findViewById(R.id.palette_orange);
        orangeBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
        orangeBrush.setOnClickListener(this);
        Button yellowBrush = (Button) findViewById(R.id.palette_yellow);
        yellowBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        yellowBrush.setOnClickListener(this);
        Button greenBrush = (Button) findViewById(R.id.palette_green);
        greenBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        greenBrush.setOnClickListener(this);
        Button blueBrush = (Button) findViewById(R.id.palette_blue);
        blueBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
        blueBrush.setOnClickListener(this);
        Button blackBrush = (Button) findViewById(R.id.palette_black);
        blackBrush.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
        blackBrush.setOnClickListener(this);
        Button eraser = (Button) findViewById(R.id.palette_eraser);
        eraser.setOnClickListener(this);

        Button saveBtn = (Button) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        currPaintView = (ImageView) findViewById(R.id.curr_paint);

        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.palette_eraser:
                drawView.setErase(true);
                currPaintView.setBackgroundColor(getResources().getColor(R.color.white));
                drawView.setBrushSize(250);
                break;
            case R.id.save_btn:
                onClickSaveBtn();
                break;
            default:
                drawView.setErase(false);
                drawView.setBrushSize(20);
                drawView.setColor(v.getTag().toString());
                currPaintView.setBackgroundColor(Color.parseColor(v.getTag().toString()));
        }
    }

    public void onClickSaveBtn() {
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
        saveDialog.setTitle("Save");
        saveDialog.setMessage("Save drawing to device Gallery?");
        saveDialog.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                frameLayout.setDrawingCacheEnabled(true);
                frameLayout.buildDrawingCache(true);
                Bitmap captureView = frameLayout.getDrawingCache();
                // write the image to a file
                String file_name = UUID.randomUUID().toString();
                System.out.println("File name : " + file_name);
                String imageSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), captureView,
                        file_name + ".png", "drawing"
                );
                if (imageSaved != null) {
                    Toast savedToast = Toast.makeText(getApplicationContext(),
                            "Successfully saved!", Toast.LENGTH_SHORT);
                    savedToast.show();
                    Intent i = new Intent();
                    i.putExtra("file_path", imageSaved);
                    if (new_draw) {
                        System.out.println("Check new");
                        setResult(FROM_DRAWING_NEW, i);
                    } else {
                        System.out.println("Check old");
                        setResult(FROM_DRAWING_ON_IMAGE, i);
                    }
                    finish();
                }
                else {
                    Toast unsavedToast = Toast.makeText(getApplicationContext(),
                            "Failed to save.", Toast.LENGTH_SHORT);
                    unsavedToast.show();
                }
                drawView.destroyDrawingCache();
            }
        });
        saveDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        saveDialog.show();
    }
}
