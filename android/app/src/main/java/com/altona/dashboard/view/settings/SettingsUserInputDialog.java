package com.altona.dashboard.view.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.altona.dashboard.R;

import java.util.function.Consumer;

public class SettingsUserInputDialog {

    static void open(Context context, String property, String currentValue, final Consumer<String> setter) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_user_input, null);

        TextView textView = view.findViewById(R.id.dialog_title);
        textView.setText("Set " + property);
        final EditText editText = view.findViewById(R.id.dialog_input);
        editText.setText(currentValue);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    setter.accept(editText.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create();
        alertDialog.show();
    }

}
