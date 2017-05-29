package edu.stanford.me202.smartfitting;

import java.util.List;
import java.util.Map;

/**
 * Created by Jang Won Suh on 5/20/2017.
 */

public class Catalog {

    private String name;
    private String type;
    private Double price;
    private List<String> colors;
    private Map<String, String> imageURLs;

    public Catalog(){}

    public Catalog(String name, String type, Double price, List<String> colors, Map<String, String> imageURLs) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.colors = colors;
        this.imageURLs = imageURLs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public Map<String, String> getImageURLs() {
        return imageURLs;
    }

    public void setImageURLs(Map<String, String> imageURLs) {
        this.imageURLs = imageURLs;
    }
}
