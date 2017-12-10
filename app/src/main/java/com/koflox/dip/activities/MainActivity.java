package com.koflox.dip.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.koflox.dip.R;
import com.koflox.dip.fragments.FileDialogFragment;
import com.koflox.dip.util.BitmapProcessor;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_LOCATION = 101;

    private ImageView imageView, ivRgb, ivLum;
    private TextView tvMaxBrightness, tvMinBrightness, tvLimit, tvLevel;


    private Bitmap sourceImage;
    private int maxBrightness, minBrightness, limit, level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.iv);
        ivRgb = (ImageView) findViewById(R.id.iv_rgb);
        ivLum = (ImageView) findViewById(R.id.iv_lum);

        tvMaxBrightness = (TextView) findViewById(R.id.tv_bright_max);
        tvMinBrightness = (TextView) findViewById(R.id.tv_bright_min);
        tvLimit = (TextView) findViewById(R.id.tv_limit);
        tvLevel = (TextView) findViewById(R.id.tv_level);


        Button btnSelectImage = (Button) findViewById(R.id.btn_select);
        Button btnDrawHistogram = (Button) findViewById(R.id.btn_histogram);
        Button btnLinearFilter = (Button) findViewById(R.id.btn_filter);
        Button btnBrightnessPicker = (Button) findViewById(R.id.btn_bright_pick);
        Button btnSmooth = (Button) findViewById(R.id.btn_smooth);
        Button btnEdges = (Button) findViewById(R.id.btn_edges);
        Button btnLimitPicker = (Button) findViewById(R.id.btn_limit_pick);
        Button btnPaster = (Button) findViewById(R.id.btn_paster);
        Button btnLevelPicker = (Button) findViewById(R.id.btn_level_pick);
        Button btnSharp = (Button) findViewById(R.id.btn_sharp);

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });
        btnDrawHistogram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapProcessor.drawHistogram(sourceImage, ivRgb, ivLum, MainActivity.this);
            }
        });
        btnLinearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapProcessor.linearContrastProcess(sourceImage, imageView, maxBrightness,
                        minBrightness, MainActivity.this);
            }
        });
        btnBrightnessPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker();
            }
        });
        final double[][] matrix = { { 1, 2, 1}, { 2, 4, 2}, { 1, 2, 1} };
        btnSmooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = null;
                BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                if (bitmapDrawable != null) bitmap = bitmapDrawable.getBitmap();
                BitmapProcessor.smooth(bitmap, imageView,
                    new double[][] {{ 1, 2, 1}, { 2, 4, 2}, { 1, 2, 1}}, MainActivity.this);
            }
        });
        btnEdges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapProcessor.edgesFiler(sourceImage, imageView, MainActivity.this, limit);
            }
        });
        btnLimitPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLimitPicker();
            }
        });
        btnPaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapProcessor.pasteurize(sourceImage, imageView, MainActivity.this, level);
            }
        });
        btnLevelPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLevelPicker();
            }
        });
        btnSharp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double k = 0.5;
                Bitmap bitmap = null;
                BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                if (bitmapDrawable != null) bitmap = bitmapDrawable.getBitmap();
                BitmapProcessor.smooth(bitmap, imageView,
                        new double[][] {{ -k/8, -k/8, -k/8 },
                                { -k/8, k/8 + 1, -k/8 },
                                { -k/8, -k/8, -k/8 }}, MainActivity.this);
            }
        });


    }

    private void chooseFile() {
        if ((android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                && (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_LOCATION);
        } else {
            showFileDialog();
        }
    }

    private void showFileDialog() {
        FragmentTransaction frTransaction = getFragmentManager().beginTransaction();
        Fragment prevFileDialog = getFragmentManager().findFragmentByTag("fileDialog");
        if (prevFileDialog != null) {
            frTransaction.remove(prevFileDialog);
        }

        FileDialogFragment newFileDialog = new FileDialogFragment();
        newFileDialog.show(frTransaction, "fileDialog");
    }

    public void showImage(String path) {
        Toast.makeText(MainActivity.this, path, Toast.LENGTH_SHORT).show();
        sourceImage = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(sourceImage);
        ivRgb.setImageResource(android.R.color.transparent);
        ivLum.setImageResource(android.R.color.transparent);
    }

    private void showNumberPicker() {
        AlertDialog dialog;
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View npView = inflater.inflate(R.layout.number_picker_dialog, null);
        final NumberPicker maxPicker = (NumberPicker) npView.findViewById(R.id.max_picker);
        maxPicker.setMaxValue(255);
        maxPicker.setMinValue(0);
        maxPicker.setValue(maxBrightness);
        final NumberPicker minPicker = (NumberPicker) npView.findViewById(R.id.min_picker);
        minPicker.setMaxValue(255);
        minPicker.setMinValue(0);
        minPicker.setValue(minBrightness);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(npView);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        maxBrightness = maxPicker.getValue();
                        minBrightness = minPicker.getValue();
                        tvMaxBrightness.setText(Integer.toString(maxBrightness));
                        tvMinBrightness.setText(Integer.toString(minBrightness));
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    private void showLimitPicker() {
        AlertDialog dialog;
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View npView = inflater.inflate(R.layout.limit_picker_dialog, null);
        final NumberPicker limitPicker = (NumberPicker) npView.findViewById(R.id.limit_picker);
        limitPicker.setMaxValue(100);
        limitPicker.setMinValue(0);
        limitPicker.setValue(limit);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(npView);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        limit = limitPicker.getValue();
                        tvLimit.setText(Integer.toString(limit));
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    private void showLevelPicker() {
        AlertDialog dialog;
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View npView = inflater.inflate(R.layout.level_picker_dialog, null);
        final NumberPicker limitPicker = (NumberPicker) npView.findViewById(R.id.level_picker);
        limitPicker.setMaxValue(300);
        limitPicker.setMinValue(0);
        limitPicker.setValue(level);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(npView);
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        level = limitPicker.getValue();
                        tvLevel.setText(Integer.toString(level));
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }
}
