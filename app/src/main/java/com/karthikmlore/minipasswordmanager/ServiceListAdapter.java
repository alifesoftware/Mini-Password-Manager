package com.karthikmlore.minipasswordmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceListAdapter extends BaseExpandableListAdapter {

    private List<Records> records;
    private List<Records> allRecords;
    private Context context;
    private Typeface typeFace;


    public ServiceListAdapter(List<Records> records, Context context, Typeface typeFace) {
        this.records = new ArrayList<>();
        this.allRecords = new ArrayList<>();
        this.context = context;
        this.typeFace = typeFace;
        populateList(records);
    }

    public void populateList(List<Records> records) {
        this.records.clear();
        this.allRecords.clear();
        Collections.sort(records, new RecordsSorter());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.records.addAll(records);
            this.allRecords.addAll(records);
        }
        else {
            for(Records r: records) {
                this.records.add(r);
                this.allRecords.add(r);
            }
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return records.get(groupPosition).getRecordData().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return records.get(groupPosition).getRecordData().get(childPosition).hashCode();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.service_record, parent, false);
        }

        TextView username = (TextView) v.findViewById(R.id.username);
        TextView notes = (TextView) v.findViewById(R.id.notes);

        RecordData data = records.get(groupPosition).getRecordData().get(childPosition);

        username.setText(data.getUsername());
        notes.setText(data.getNotes());
        final String password = data.getPassword();

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Password");
                builder.setMessage(password).setPositiveButton("Close", null).show();

            }
        });
        return v;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return records.get(groupPosition).getRecordData().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return records.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return records.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return records.get(groupPosition).hashCode();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.service_head, parent, false);
        }
        TextView title = (TextView) v.findViewById(R.id.title);
        TextView url = (TextView) v.findViewById(R.id.url);

        Records current_record = records.get(groupPosition);

        title.setTypeface(typeFace);
        title.setText(current_record.getTitle());
        url.setText(current_record.getUrl());

        return v;
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void filterData(String query){

        query = query.toLowerCase();
        records.clear();

        if(query.isEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                records.addAll(allRecords);
            }
            else {
                for (Records r : allRecords) {
                    records.add(r);
                }
            }
        }
        else {
            for(Records rec: allRecords){
                if(rec.getTitle().toLowerCase().startsWith(query)){
                    records.add(rec);
                }
            }
            for(Records rec: allRecords){
                if(rec.getTitle().toLowerCase().contains(query) && !(records.contains(rec))){
                    records.add(rec);
                }
            }
        }
        notifyDataSetChanged();
    }

}