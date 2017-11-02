package com.example.maciejwikira.prgnv2;

/**
 * Created by Maciej on 2017-11-02.
 */

public class Card {

    private int databaseId;
    private String name;
    private String category;
    private String date;
    private String img;
    private String favorited;

    public Card(int id, String name, String category, String date, String img, String favorited){
        this.databaseId = id;
        this.name = name;
        this.category = category;
        this.date = date;
        this.img = img;
        this.favorited = favorited;
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

    public String getImg(){
        return this.img;
    }

    public boolean isFavorited(){
        if(favorited.equals("yes")){
            return true;
        }else {
            return false;
        }
    }

}
