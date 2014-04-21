package com.example.RunscopeEverything;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;
import java.util.Set;

public class SettingsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final EditText text = (EditText)findViewById(R.id.tokenText);
        final Button saveButton = (Button)findViewById(R.id.saveButton);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveButton.setEnabled(editable.length() > 0);
            }
        });

        // NB: Can't believe I have to do this
        final Activity that = this;

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("runscope", Context.MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", text.getText().toString());
                editor.commit();

                Toast t = Toast.makeText(that, "Runscope Bucket ID has been set. Restart any already running apps to apply the new bucket.", 5);
                t.show();
            }
        });

        SharedPreferences prefs = getSharedPreferences("runscope", Context.MODE_WORLD_READABLE);
        text.setText(prefs.getString("token", ""));
    }
}
