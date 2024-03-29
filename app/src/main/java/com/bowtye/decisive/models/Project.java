package com.bowtye.decisive.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.bowtye.decisive.database.Converters;

import java.util.Date;


@Entity(tableName = "project")
public class Project implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private Boolean hasPrice;

    @TypeConverters(Converters.class)
    private Date dateCreated;

    @Ignore
    private String firebaseId;

    @Ignore
    public Project(){}

    public Project(int id, String name, Boolean hasPrice, Date dateCreated) {
        this.id = id;
        this.name = name;
        this.hasPrice = hasPrice;
        this.dateCreated = dateCreated;
    }

    @Ignore
    public Project(String name, Boolean hasPrice, Date date) {
        this.name = name;
        this.hasPrice = hasPrice;
        this.dateCreated = date;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHasPrice() {
        return hasPrice;
    }

    public void setHasPrice(Boolean hasPrice) {
        this.hasPrice = hasPrice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeValue(this.hasPrice);
        dest.writeLong(this.dateCreated != null ? this.dateCreated.getTime() : -1);
        dest.writeString(this.firebaseId);
    }

    @Ignore
    protected Project(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.hasPrice = (Boolean) in.readValue(Boolean.class.getClassLoader());
        long tmpDateCreated = in.readLong();
        this.dateCreated = tmpDateCreated == -1 ? null : new Date(tmpDateCreated);
        this.firebaseId = in.readString();
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel source) {
            return new Project(source);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}
