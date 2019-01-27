package com.pp.mte;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Created by kevin on 11/02/18.
 */

public class EditorActivity extends AppCompatActivity {

    private EditText editorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editorText = (EditText) findViewById(R.id.editorText);

        editorText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editorText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }
}
