package com.example.hackintosh.lexicon;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hackintosh on 2/25/17.
 */

public class NotificationLexicon implements Serializable {

    private List<String[]> lexicon;
    private int time = 0;

    public NotificationLexicon(List<String[]> lexicon, int time) {
        this.lexicon = lexicon;
        this.time = time;
    }

    public List<String[]> getLexicon() { return this.lexicon; }

    public void setLexicon(List<String[]> lexicon) { this.lexicon = lexicon; }


    public int getTime() { return this.time; }

    public void setTime(int time) { this.time = time; }
}
