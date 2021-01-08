package com.berryspace.conjure.models;

public class Album {
    private String name;
    private String year;
    private String imageUrl;
    private String id;

    public void setId(String id) { this.id = id;}

    public void setName(String name){
        this.name = name;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public void setYear(String year){
        this.year = year;
    }


    public String getName() {
        return name;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public String getYear() {
        return year;
    }

    public String getId() { return id; }

   }
