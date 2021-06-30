package com.sondre.kjokkenkompis.shoppinglists;

public class Item {
    String name;
    Boolean ingredient;
    Boolean ischecked;

    public Item(String name, Boolean ingredient,Boolean ischecked) {
        this.name = name;
        this.ingredient = ingredient;
        this.ischecked = ischecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIngredient() {
        return ingredient;
    }

    public void setIngredient(Boolean ingredient) {
        this.ingredient = ingredient;
    }

    public Boolean getischecked() {
        return ischecked;
    }

    public void setischecked(Boolean ischecked) {
        this.ischecked = ischecked;
    }
}
