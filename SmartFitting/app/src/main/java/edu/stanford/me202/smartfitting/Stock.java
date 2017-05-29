package edu.stanford.me202.smartfitting;

/**
 * Created by Jang Won Suh on 5/20/2017.
 */

public class Stock {

    private String catalog;
    private String color;
    private String size;

    public Stock(){}

    public Stock(String catalog, String color, String size){
        this.catalog = catalog;
        this.color = color;
        this.size = size;
    }

    public Stock(String key, String size){
        String[] s = key.split("_");
        this.catalog = s[0];
        this.color = s[1];
        this.size = size;
    }

    public String getKey() {
        return catalog + "_" + color;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
