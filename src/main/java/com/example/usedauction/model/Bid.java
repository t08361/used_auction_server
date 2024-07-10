package com.example.usedauction.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "bids")
public class Bid {
    @Id
    private String id;
    private String itemId;
    private String bidderId;
    private int bidAmount;
    private Date bidTime;

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getItemId() {return itemId;}

    public void setItemId(String itemId) {this.itemId = itemId;}

    public String getBidderId() {return bidderId;}

    public void setBidderId(String bidderId) {this.bidderId = bidderId;}

    public int getBidAmount() {return bidAmount;}

    public void setBidAmount(int bidAmount) {this.bidAmount = bidAmount;}

    public Date getBidTime() {return bidTime;}

    public void setBidTime(Date bidTime) {this.bidTime = bidTime;}
}
