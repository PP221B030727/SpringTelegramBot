package com.example.TelegramBot.models;

import lombok.Data;

@Data
public class Currency {
    String fullname;
    String title;
    double description;
    int quant;
    public Currency(){

    }
    public Currency(String fullname , String title , double description , int quant){
        this.fullname = fullname;
        this.title = title;
        this.description = description;
        this.quant = quant;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getDescription() {
        return description;
    }
    public void setDescription(double description) {
        this.description = description;
    }

    public int getQuant() {
        return quant;
    }

    public void setQuant(int quant) {
        this.quant = quant;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
