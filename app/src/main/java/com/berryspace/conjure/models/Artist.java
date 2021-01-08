package com.berryspace.conjure.models;

public class Artist {
    private String name;
    private String id;
    private String imageUrl;
    private String genres;
    private String followers;

    public void setName(String name){
        this.name = name;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public void setGenres(String genres){
        this.genres = genres;
    }

    public void setFollowers(String followers){
        this.followers = followers;
    }

    public void setId(String id){
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public String getGenres(){
        return genres;
    }

    public String getFollowers(){
        return followers;
    }

    public String getId(){
        return id;
    }
}
