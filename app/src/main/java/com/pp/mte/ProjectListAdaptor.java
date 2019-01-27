package com.pp.mte;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 11/02/18.
 */

class ProjectListAdaptor extends ArrayAdapter<String> {

    private List<String> names;
    private List<Boolean> checked;

    public ProjectListAdaptor(Context ctx, List res) {
        super(ctx, R.layout.row, res);

        this.names = res;
        this.checked = new ArrayList<>();
        for(int i = 0; i < this.names.size(); i++) checked.add(false);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.row, parent, false);
        String projectText = getItem(position);
        TextView projectTextView = (TextView) view.findViewById(R.id.projectsListText);
        projectTextView.setText(projectText);

        final CheckBox checkbox = view.findViewById(R.id.checkBox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checked.set(position, checkbox.isChecked());
            }
        });

        return view;
    }

    public String getName(int position) {
        return names.get(position);
    }

    public List<Boolean> getChecked() {
        return checked;
    }

    public void add(String name) {
        names.add(name);
        checked.add(false);
        notifyDataSetChanged();
    }

    public void delete(int position) {
        names.remove(position);
        checked.remove(position);
        notifyDataSetChanged();
    }
}
