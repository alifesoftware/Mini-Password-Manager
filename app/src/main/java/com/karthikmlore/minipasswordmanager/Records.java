package com.karthikmlore.minipasswordmanager;

import java.util.ArrayList;
import java.util.List;

public class Records{

    private String title;
    private String url;
    private String id;
    private List<RecordData> record_data = new ArrayList<>();

    public Records(String title, String url, String id) {
        this.title = title;
        this.url = url;
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public String getUrl() {
        return url;
    }
    public String getId() {
        return id;
    }
    public List<RecordData> getRecordData() {
        return record_data;
    }

    public void setRecordData(List<RecordData> record_data) {
        this.record_data = record_data;
    }


}