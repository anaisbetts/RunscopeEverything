package com.example.RunscopeEverything;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final EditText text = (EditText)findViewById(R.id.tokenText);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("runscopeEverything", Context.MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("token", text.getText().toString());
                editor.commit();
            }
        });
    }
}
