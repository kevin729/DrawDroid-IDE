package com.pp.mte;

import android.content.Intent;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kevin on 11/02/18.
 **/

public class ProjectsActivity extends AppCompatActivity {

    private AlertDialog newProjectDialog;

    private ProjectListAdaptor projectsAdaptor;
    private File projectsFile;
    private List<String> projects;

    private ListView projectsList;
    private Button newProjectBtn;

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        projectsFile = new File(getFilesDir(), "projects");

        if (!projectsFile.exists()) {
            projectsFile.mkdir();
        }

        String[] projectsArray = projectsFile.list();
        projects = new ArrayList<>(Arrays.asList(projectsArray));

        newProjectBtn = (Button) findViewById(R.id.newProjectBtn);

        newProjectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(ProjectsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.dialog_newproject, null);

                final EditText newProjectName = (EditText) view.findViewById(R.id.newProjectName);

                newProjectName.setImeOptions(EditorInfo.IME_ACTION_DONE);
                newProjectName.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

                Button createProjectBtn = (Button) view.findViewById(R.id.createProjectBtn);

                createProjectBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String newProjectNameText = newProjectName.getText().toString();
                        File newProject = new File(getFilesDir()+"/projects", newProjectNameText);
                        newProject.mkdir();

                        projectsAdaptor.add(newProjectNameText);

                        Toast.makeText(ProjectsActivity.this, newProjectNameText + " " + getResources().getString(R.string.project_created), Toast.LENGTH_LONG).show();
                        newProjectDialog.dismiss();

                    }
                });

                builder.setView(view);
                newProjectDialog = builder.create();
                newProjectDialog.show();
            }
        });

        projectsAdaptor = new ProjectListAdaptor(this, projects);
        projectsList = (ListView) findViewById(R.id.projectsList);
        projectsList.setAdapter(projectsAdaptor);

        projectsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(ProjectsActivity.this, EditorActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_project:

                for (int i = projectsAdaptor.getChecked().size()-1; i >= 0; i--) {
                    if (projectsAdaptor.getChecked().get(i)) {
                        File project = new File(getFilesDir()+"/projects", projectsAdaptor.getName(i));
                        project.delete();
                        projectsAdaptor.delete(i);
                    }
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
