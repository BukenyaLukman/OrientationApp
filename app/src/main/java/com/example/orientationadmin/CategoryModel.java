package com.example.orientationadmin;

public class CategoryModel {

    private String name;
    private int Sets;
    private String url;


    public CategoryModel(String name, int sets, String url) {
        this.name = name;
        Sets = sets;
        this.url = url;

    }

    public CategoryModel() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSets() {
        return Sets;
    }

    public void setSets(int sets) {
        Sets = sets;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
