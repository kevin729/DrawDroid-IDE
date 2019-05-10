package com.pp.mte.view;


import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;

import android.os.Debug;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pp.mte.EditorI;
import com.pp.mte.R;
import com.pp.mte.presenter.EditorPresenter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevin on 11/02/18.
 */

public class EditorActivity extends AppCompatActivity implements EditorI.View{

    public EditorPresenter presenter;

    private boolean drawingMode = false;
    private MenuItem drawBtn;
    private EditText editorText;
    private ArrayList<String[]> editTextHistory = new ArrayList<>();
    private CanvasView canvasView;
    private ViewGroup layout;

    private KeyboardView keyboardView;
    private Keyboard mainKeyboard;
    private Keyboard methodKeyboard;
    private Keyboard variableKeyboard;
    private Keyboard keyKeyboard;
    private Keyboard operatorsKeyboard;
    private Keyboard dataTypeKeyboard;

    private int methodAmount = 0;
    private int variableAmount = 0;

    private int newCodeIndex = 0;
    private int previousCodeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        String[] empty = {"", "0"};
        editTextHistory.add(empty);

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

        //create keyboards
        mainKeyboard = new Keyboard(this, R.xml.main_keyboard);
        methodKeyboard = new Keyboard(this, R.xml.method_keyboard);
        variableKeyboard = new Keyboard(this, R.xml.variables_keyboard);
        keyKeyboard = new Keyboard(this, R.xml.key_keyboard);
        operatorsKeyboard = new Keyboard(this, R.xml.operators_keyboard);
        dataTypeKeyboard = new Keyboard(this, R.xml.datatype_keyboard);

        keyboardView = (KeyboardView)findViewById(R.id.keyboardview);
        keyboardView.setKeyboard(mainKeyboard);

        keyboardView.setOnKeyboardActionListener(new KeyboardService());

        //create editor
        editorText = findViewById(R.id.editorText);

        editorText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editorText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        editorText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openKeyboard(view);
            }
        });

        //sets up drawing view
        canvasView = new CanvasView(this, this);
        layout = findViewById(R.id.editorLayout);
        canvasView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) canvasView.getLayoutParams();
        lp.addRule(RelativeLayout.BELOW, R.id.app_bar);
    }

    public void openKeyboard(View v)
    {
        keyboardView.setVisibility(View.VISIBLE);
        keyboardView.setEnabled(true);
        if (v != null) {
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void closeKeyboard() {
        keyboardView.setKeyboard(mainKeyboard);
        keyboardView.setVisibility(View.GONE);
        keyboardView.setEnabled(false);
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
            case R.id.undoBtn:
                undo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds canvasView to draw on
     */
    private void enterDrawingMode() {
        if (!drawingMode) {
            closeKeyboard();
            drawBtn.setIcon(R.drawable.exitdrawingbtn);
            layout.addView(canvasView);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.drawingModeEnabled), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.drawingModeDisabled), Toast.LENGTH_SHORT).show();
            drawingMode = false;
            openKeyboard(editorText);
        }
    }

    @Override
    public void setCode(String extractedText) {

        if (!extractedText.equals("")) {
            int cursorPosition = editorText.getSelectionStart();
            String[] text = extractedText.split("\n");
            for (int i = 1; i < text.length-1; i++) {
                editorText.getText().insert(editorText.getSelectionStart(), text[i].trim() + '\n');

            }
            editorText.setSelection(cursorPosition);

            sortCode();
        }
        canvasView.clear();

        exitDrawingMode();
    }

    /**
     * Structures the code and records methods and variables
     */
    private void sortCode() {
        int start = editorText.getSelectionStart();
        int cursorPosition = 0;
        int endPosition = 0;
        int positionIndentation = 0;
        int indent = 0;

        int methods = 0;
        int variables = 0;

        if (editorText.getText().toString().matches("")) {
            return;
        }

        String[] codeLines = editorText.getText().toString().split("\n");

        for (int m = 0; m < methodAmount; m++) {
            methodKeyboard.getKeys().get(m).label = "";
        }

        for (int v = 0; v < variableAmount; v++) {
            variableKeyboard.getKeys().get(v).label = "";
        }

        for (int i = 0; i < codeLines.length; i++) {
            //Add methods to keyboard
            Pattern pattern = Pattern.compile(".(\\()");
            Matcher method = pattern.matcher(codeLines[i]);

            if (method.find() && codeLines[i].contains("{") && methods < 12) {
                String methodText = codeLines[i];
                methodText = methodText.replaceAll("\\(.*", "");
                String[] methodTextArray = methodText.split(" ");
                String methodName = methodTextArray[methodTextArray.length - 1];
                methodKeyboard.getKeys().get(methods).label = methodName;
                methods++;
            }
            methodAmount = methods;

            //Adds variables to keyboard
            if (codeLines[i].contains(";") && variables < 12) {

                String variableText = codeLines[i];
                String[] variableTextArray;
                String variableName;
                if (codeLines[i].contains("=")) {
                    variableText = variableText.replaceAll("\\=.*", "");
                    variableTextArray = variableText.split(" ");
                    variableName = variableTextArray[variableTextArray.length - 1];
                    variableKeyboard.getKeys().get(variables).label = variableName;
                    variables++;
                } else if (!codeLines[i].contains(".") && !codeLines[i].contains("(") && !codeLines[i].contains(")")) {
                    variableText = variableText.replaceAll("\\;", "");
                    variableTextArray = variableText.split(" ");
                    variableName = variableTextArray[variableTextArray.length - 1];

                    variableKeyboard.getKeys().get(variables).label = variableName;
                    System.out.println(variableName);
                    variables++;
                }


            }
            variableAmount = variables;

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
            int oldPosition = cursorPosition;
            cursorPosition += codeLines[i].length() + indentationLength + 1;
            if (cursorPosition <= editorText.getText().length()) {
                editorText.setSelection(cursorPosition);
            }

            if (start >= oldPosition && start <= cursorPosition) {
                positionIndentation = indent * 4;
                endPosition = editorText.getSelectionStart();
            }
        }

        editorText.setSelection(endPosition + positionIndentation);

        newCodeIndex = start;

        String[] code = {editorText.getText().toString(), Integer.toString(editorText.getSelectionStart())};
        editTextHistory.add(code);
    }

    public int getLine() {
        return editorText.getLayout().getLineForOffset(editorText.getSelectionStart());
    }

    public void undo() {
        if (editTextHistory.size() != 1) {
            String[] text = editTextHistory.get(editTextHistory.size() - 2);
            editorText.setText(text[0]);
            editTextHistory.remove(editTextHistory.size() - 1);

            editorText.setSelection(Integer.parseInt(text[1]));
        }
    }

    public class KeyboardService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

        private int selectionStart = 0;
        private int selectionEnd = 0;

        public final static int delete = -5; //delete
        public final static int left = 55002; //cursor left
        public final static int right = 55003; //cursor right
        public final static int tab = 55004;

        public final static int mainBtn = 50000;
        public final static int methodBtn = 60000;
        public final static int variableBtn = 70000;
        public final static int keyBtn = 80000;
        public final static int operatorBtn = 90000;
        public final static int dataTypeBtn = 100000;

        public KeyboardService() {
            super();
        }

        @Override
        public void onPress(int i) {

        }

        @Override
        public void onRelease(int i) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            int start = editorText.getSelectionStart();

            if (primaryCode==tab) {
                do {
                    selectionStart = newCodeIndex;
                    String text = editorText.getText().toString().substring(newCodeIndex, editorText.getText().length());
                    String word = text.split(" ")[0].replaceAll("\\s.", "");

                    newCodeIndex += word.length() + 1;
                    selectionEnd = newCodeIndex - 1;
                    if (word.contains("(")) {
                        selectionStart++;
                    }

                    if (word.contains(")")) {
                        selectionEnd--;
                    }

                    if (word.contains(";")) {
                        selectionEnd--;
                    }

                    if (word.contains(System.getProperty("line.separator"))) {
                        selectionEnd--;
                    }

                    editorText.setSelection(selectionStart, selectionEnd);

                    if (editorText.getText().length() - selectionEnd < 2) {
                        newCodeIndex = 0;
                    }

                    if (word.matches("\n")) {
                        System.out.println("next Line");
                        selectionEnd = selectionStart;
                        editorText.setSelection(selectionStart, selectionEnd);
                        break;
                    }



                } while (selectionStart == selectionEnd);

                return;
            } else if (primaryCode==left) {
                if (start > 0) {
                    editorText.setSelection(start - 1);
                }

                return;
            } else if (primaryCode==right) {
                if (start < editorText.length()) {
                    editorText.setSelection(start + 1);
                }

                return;
            } else if (primaryCode==mainBtn) {
                keyboardView.setKeyboard(mainKeyboard);

                return;
            } else if (primaryCode==methodBtn) {
                keyboardView.setKeyboard(methodKeyboard);
                sortCode();
                editorText.setSelection(start);

                return;
            } else if (primaryCode==variableBtn) {
                keyboardView.setKeyboard(variableKeyboard);
                sortCode();
                editorText.setSelection(start);

                return;
            } else if (primaryCode==keyBtn) {
                keyboardView.setKeyboard(keyKeyboard);

                return;
            } else if (primaryCode==operatorBtn) {
                keyboardView.setKeyboard(operatorsKeyboard);

                return;
            } else if (primaryCode==dataTypeBtn) {
                keyboardView.setKeyboard(dataTypeKeyboard);

                return;
            }



            editorText.getText().replace(selectionStart, selectionEnd, "");
            selectionStart = 0;
            selectionEnd = 0;

            if (primaryCode > methodBtn && primaryCode < variableBtn) {
                editorText.getText().insert(start, methodKeyboard.getKeys().get(primaryCode - methodBtn - 1).label);
            } else if (primaryCode > variableBtn && primaryCode < keyBtn) {
                editorText.getText().insert(start, variableKeyboard.getKeys().get(primaryCode - variableBtn - 1).label);
            } else if (primaryCode > operatorBtn && primaryCode < dataTypeBtn) {
                editorText.getText().insert(start, operatorsKeyboard.getKeys().get(primaryCode - operatorBtn - 1).label);
            } else if (primaryCode > dataTypeBtn && primaryCode < 110000) {
                editorText.getText().insert(start, dataTypeKeyboard.getKeys().get(primaryCode - dataTypeBtn - 1).label);
            } else if (primaryCode==delete) {
                if (editorText.getText() != null && start > 0) {
                    editorText.getText().delete(start - 1, start);
                }
            } else {
                String character = Character.toString((char) primaryCode);

                if (primaryCode == 61) {
                    character = " " + character + " ";
                }

                if (primaryCode == 59) {
                    keyboardView.setKeyboard(mainKeyboard);
                }

                editorText.getText().insert(start, character);


                if (keyboardView.getKeyboard() == dataTypeKeyboard) {
                    if (primaryCode == 32) {
                        keyboardView.setKeyboard(keyKeyboard);
                    }
                }

                if (primaryCode == 10) {
                    String codeLines[] = editorText.getText().toString().split("\n");
                    System.out.println(codeLines.length);
                    if (!(getLine() > codeLines.length)) {
                        String codeLine = codeLines[getLine() - 1];
                        if (codeLine.contains("{")) {
                            editorText.getText().insert(editorText.getSelectionStart(), "    ");
                        }
                        String indentation = codeLine.replaceAll("\\S.*", "");
                        editorText.getText().insert(editorText.getSelectionStart(), indentation);
                    }
                }
            }

            String[] code = {editorText.getText().toString(), Integer.toString(editorText.getSelectionStart())};
            editTextHistory.add(code);
        }

        @Override
        public void onText(CharSequence charSequence) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }
}
