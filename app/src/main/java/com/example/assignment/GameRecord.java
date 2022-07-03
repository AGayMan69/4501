package com.example.assignment;

// class for store game record
public class GameRecord {
    private int id;
    private String date;
    private String time;
    private int duration;
    private String status;
    public static GameRecord[] records;

    public GameRecord(int id, String date, String time, int duration, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getDuration() {
        return duration;
    }

    public String getStatus() {
        return status;
    }
}
