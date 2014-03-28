package com.eleks.rnd.warehouse.glass;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Item implements Serializable {
    private String id;
    private Date expiry;

    public Item() {
    }

    public Item(String id, Date expiry) {
        super();
        this.id = id;
        this.expiry = expiry;
    }

    public String getId() {
        return id;
    }

    public Date getExpiry() {
        return expiry;
    }

    public String getExpiryString() {
        return expiry == null ? "" : sdf.format(expiry);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    @Override
    public String toString() {
        return "Item [id=" + id + ", expiry=" + expiry + "]";
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Supported format: yyyy-MM-dd;id
     * 
     * @param data
     * @return item with id or item with id and date
     */
    public static Item parseFrom(String data) {
        Item result = new Item();

        String id = "";
        Date d = null;

        int split = data.indexOf(';');
        if (split == -1) {
            id = data.substring(0, data.length() > 8 ? 8 : data.length());
        } else {
            String date = data.substring(0, split);
            id = data.substring(split + 1);
            try {
                d = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        result.setId(id);
        result.setExpiry(d);
        return result;
    }

    public static Item withId(String data) {
        return new Item(data, null);
    }

    public void copyFrom(Item parseFrom) {
        this.setId(parseFrom.getId());
        this.setExpiry(parseFrom.getExpiry());
    }
}
