package com.twotiger.library.widget.contactselector;

public class SortModel extends Contact {
    public String sortLetters; //显示数据拼音的首字母
    public SortToken sortToken = new SortToken();

    public SortModel(String name, String number, String sortKey) {
        super(name, number, sortKey);
    }
}
