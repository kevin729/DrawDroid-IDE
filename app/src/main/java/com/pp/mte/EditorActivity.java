package com.pp.mte;


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

/**
 * Created by kevin on 11/02/18.
 */

public class EditorActivity extends AppCompatActivity {

    private boolean drawingMode = false;
    private MenuItem drawBtn;
    private EditText editorText;
    private CanvasView canvasView;
    private ViewGroup layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.backbtn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawingMode) {
                    exitDrawingMode();
                } else {
                    startActivity(new Intent(EditorActivity.this, ProjectsActivity.class));
                }
            }
        });

        editorText = findViewById(R.id.editorText);

        editorText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editorText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        //sets up drawing view
        canvasView = new CanvasView(this);
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
                    feedForward();
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
            drawBtn.setIcon(R.drawable.gobtn);
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

    private void feedForward() {
        canvasView.feedForward();
        canvasView.clear();
    }
}
