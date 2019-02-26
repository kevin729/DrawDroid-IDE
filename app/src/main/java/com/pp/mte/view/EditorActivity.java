package com.pp.mte.view;


import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pp.mte.EditorI;
import com.pp.mte.R;
import com.pp.mte.presenter.EditorPresenter;

/**
 * Created by kevin on 11/02/18.
 */

public class EditorActivity extends AppCompatActivity implements EditorI.View{

    public EditorPresenter presenter;

    private boolean drawingMode = false;
    private MenuItem drawBtn;
    private EditText editorText;
    private CanvasView canvasView;
    private ViewGroup layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        presenter = new EditorPresenter(getBaseContext(), this);

        Toolbar toolbar = findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.backbtn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener((View view) -> {
            if (drawingMode) {
                exitDrawingMode();
            } else {
                startActivity(new Intent(EditorActivity.this, ProjectsActivity.class));
            }
        });

        editorText = findViewById(R.id.editorText);

        editorText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editorText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        //sets up drawing view
        canvasView = new CanvasView(this, this);
        layout = findViewById(R.id.editorLayout);
        canvasView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) canvasView.getLayoutParams();
        lp.addRule(RelativeLayout.BELOW, R.id.app_bar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        drawBtn = menu.findItem(R.id.drawBtn);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawBtn:
                if (drawingMode) {
                    exitDrawingMode();
                } else {
                    enterDrawingMode();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds canvasView to draw on
     */
    private void enterDrawingMode() {
        if (!drawingMode) {
            drawBtn.setIcon(R.drawable.backbtn);
            layout.addView(canvasView);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.drawingModeEnabled), Toast.LENGTH_LONG).show();
            drawingMode = true;
        }
    }

    /**
     * Removes canvasView
     */
    private void exitDrawingMode() {
        if (drawingMode) {
            drawBtn.setIcon(R.drawable.drawbtn);
            canvasView.clear();
            layout.removeView(canvasView);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.drawingModeDisabled), Toast.LENGTH_LONG).show();
            drawingMode = false;
        }
    }

    @Override
    public void setCode(String extractedText) {

        if (!extractedText.equals("")) {
            int cursorPosition = 0;
            String[] text = extractedText.split("\n");
            for (int i = 1; i < text.length-1; i++) {
                editorText.getText().insert(editorText.getSelectionStart(), text[i].trim() + '\n');
                cursorPosition = editorText.getSelectionStart();
            }

            sortCode();
        }
        canvasView.clear();
    }

    /**
     * Structures the code
     */
    private void sortCode() {
        int cursorPosition = 0;
        int indent = 0;
        String[] codeLines = editorText.getText().toString().split("\n");
        for (int i = 0; i < codeLines.length; i++) {
            //if closing method, decrease the indent
            if (codeLines[i].contains("}")) {
                indent--;
            }

            //add current indent (here because method closing moves left whereas starting indent will STAY at previous indent
            String indentation = "";
            for (int ind = 0; ind < indent*4; ind++) {
                indentation += " ";
            }

            //if opening method, increase the indent for next line
            if (codeLines[i].contains("{")) {
                indent++;
            }

            //sets length to move code
            String selectedText = editorText.getText().toString().substring(cursorPosition, Math.min(cursorPosition + indentation.length(), cursorPosition + codeLines[i].length()));
            int indentationLength = 0;
            if (!selectedText.equals(indentation)) {
                indentationLength = indentation.length();
                editorText.getText().insert(cursorPosition, indentation);
            }

            //change positions
            cursorPosition += codeLines[i].length() + indentationLength + 1;
            editorText.setSelection(cursorPosition);
        }
    }
}
