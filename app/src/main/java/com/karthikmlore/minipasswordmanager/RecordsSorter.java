package com.karthikmlore.minipasswordmanager;

import java.util.Comparator;


public class RecordsSorter implements Comparator {

    @Override
    public int compare(Object a, Object b) {
        Records r1 = (Records) a;
        Records r2 = (Records) b;
        return r1.getTitle().toLowerCase().compareTo(r2.getTitle().toLowerCase());
    }
}
