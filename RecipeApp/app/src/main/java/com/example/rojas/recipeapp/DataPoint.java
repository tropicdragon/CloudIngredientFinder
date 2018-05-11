package com.example.rojas.recipeapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class DataPoint implements Parcelable{
    int id;
    String name;
    String contact;
    String info = "";
    double latitude;
    double longitude;
    ArrayList<String> itemNames = new ArrayList<>();
    ArrayList<Integer> itemCounts = new ArrayList<>();
    ArrayList<Double> itemPrice = new ArrayList<>();
    ArrayList<Integer> itemIDs = new ArrayList<>();
    public DataPoint(int id, String name, String contact, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public void itemAppend(String str, double price){
        info += str + ": " + price+ "; ";
    }

    protected DataPoint(Parcel in) {
        id = in.readInt();
        name = in.readString();
        info = in.readString();
        contact = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        itemNames = in.createStringArrayList();
    }

    public static final Creator<DataPoint> CREATOR = new Creator<DataPoint>() {
        @Override
        public DataPoint createFromParcel(Parcel in) {
            return new DataPoint(in);
        }

        @Override
        public DataPoint[] newArray(int size) {
            return new DataPoint[size];
        }
    };

    public void addItem(int id, String name, double price, int count){
        itemIDs.add(id);
        itemNames.add(name);
        itemPrice.add(price);
        itemCounts.add(count);


    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(info);
        dest.writeString(contact);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(itemNames);
    }
}
