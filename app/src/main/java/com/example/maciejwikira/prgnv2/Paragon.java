package com.example.maciejwikira.prgnv2;

/**
 * Created by Maciej on 2017-10-21.
 */

public class Paragon extends Card {

    private String value;
    private String text;

    public Paragon(int id, String name, String category, String value, String date, String img, String text, String favorited){
        super(id, name, category, date, img, favorited);
        this.value = value;
        this.text = text;
    }

    public String getValue(){
        return this.value;
    }

    public String getText(){
        return this.text;
    }

}
