package com.altona.dashboard.view.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.altona.dashboard.R;

import java.util.function.Consumer;

public class UserInputDialog {

    public static void open(Context context, String text, String value, Consumer<String> onOk, Runnable onCancel) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_key_value, null);

        TextView textView = view.findViewById(R.id.dialog_title);
        textView.setText(text);
        final EditText editText = view.findViewById(R.id.dialog_input);
        editText.setText(value);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    onOk.accept(editText.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    onCancel.run();
                    dialog.cancel();
                })
                .create();
        alertDialog.show();
    }

}
