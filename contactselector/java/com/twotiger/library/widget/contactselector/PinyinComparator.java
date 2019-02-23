package com.twotiger.library.widget.contactselector;

import java.util.Comparator;

/**
 * Created by Makise on 2017/1/18.
 */
public class PinyinComparator implements Comparator<SortModel> {

    public int compare(SortModel o1, SortModel o2) {
        if (o1.sortLetters.equals("@") || o2.sortLetters.equals("#")) {
            return -1;
        } else if (o1.sortLetters.equals("#") || o2.sortLetters.equals("@")) {
            return 1;
        } else {
            return o1.sortLetters.compareTo(o2.sortLetters);
        }
    }

}
