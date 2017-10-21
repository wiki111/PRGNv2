package com.example.maciejwikira.prgnv2;

/**
 * Created by Maciej on 2017-10-21.
 */

public class Paragon {

    private int databaseId;
    private String name;
    private String category;
    private String value;
    private String date;
    private String img;
    private String text;

    public Paragon(int id, String name, String category, String value, String date, String img, String text){
        this.databaseId = id;
        this.name = name;
        this.category = category;
        this.value = value;
        this.date = date;
        this.img = img;
        this.text = text;
    }

    public int getDbId(){
        return this.databaseId;
    }

    public String getName(){
        return this.name;
    }

    public String getCategory(){
        return this.category;
    }

    public String getDate(){
        return this.date;
    }

    public String getValue(){
        return this.value;
    }

    public String getImg(){
        return this.img;
    }

    public String getText(){
        return this.text;
    }

}
