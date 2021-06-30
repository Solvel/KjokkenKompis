package com.sondre.kjokkenkompis.recipes;

import java.util.Comparator;

public class Recipe {
    String name;
    int full;
    int favorite;

    public Recipe(String name, int full, int favorite) {
        this.name = name;
        this.full = full;
        this.favorite = favorite;
    }

    /*Comparator for sorting the list by Student Name*/
    public static Comparator<Recipe> RecipeFavorite = new Comparator<Recipe>() {
        public int compare(Recipe s1, Recipe s2) {
            int Recipe1 = s1.getFavorite();
            int Recipe2 = s2.getFavorite();
            //ascending order
            return Recipe2 - Recipe1;
        }};

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getfull() {
        return full;
    }

    public void setfull(int full) {
        this.full = full;
    }

    public int getFavorite() { return favorite;}

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}