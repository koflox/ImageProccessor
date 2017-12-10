package com.koflox.dip.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koflox.dip.R;
import com.koflox.dip.activities.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDialogFragment extends DialogFragment{

    public static final String PREF_KEY_SOURCE_ROUTE = "pref_key_source_route";


    Button buttonSelect;
    Button buttonUp;
    TextView textFolder;

    ListView folderList;

    File root;
    File curFolder;

    private List<String> filePathList = new ArrayList<>();
    private List<String> fileNameList = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final SharedPreferences settings;
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = root;

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.file_dialog_layout);
        dialog.setTitle("Choose folder");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        textFolder = (TextView) dialog.findViewById(R.id.folder);

        buttonUp = (Button) dialog.findViewById(R.id.up);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListDir(curFolder.getParentFile());
            }
        });

        buttonSelect = (Button) dialog.findViewById(R.id.select);
//        buttonSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor = settings.edit();
//                String imageSourceRoute = textFolder.getText().toString();
//                editor.putString(PREF_KEY_SOURCE_ROUTE, imageSourceRoute);
//                editor.apply();
//
//                ((MainActivity) getActivity()).showImage(imageSourceRoute);
//
//                dismiss();
//            }
//        });

        folderList = (ListView) dialog.findViewById(R.id.folder_list);
        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selected = new File(filePathList.get(position));
                if(selected.isDirectory()) {
                    ListDir(selected);
                } else {
                     if (selected.isFile()) {
                         ((MainActivity) getActivity()).showImage(selected.getPath());
                         dismiss();
                     }
                }
            }
        });

        if (settings.contains(PREF_KEY_SOURCE_ROUTE)) {
            File chosenFolder = new File(settings.getString(PREF_KEY_SOURCE_ROUTE, "does not exist"));
            ListDir(chosenFolder);
        } else {
            ListDir(root);
        }
        return dialog;
    }

    void ListDir(File f) {
        if (f.equals(root)) {
            buttonUp.setEnabled(false);
        } else {
            buttonUp.setEnabled(true);
        }

        curFolder = f;
        textFolder.setText(f.getPath());

        File[] files = f.listFiles();
        filePathList.clear();
        fileNameList.clear();

        for (File file : files) {
            filePathList.add(file.getPath());
            fileNameList.add(file.getName());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, fileNameList);
        folderList.setAdapter(directoryList);
    }
}
