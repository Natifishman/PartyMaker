package com.example.partymaker.utilities.groupBuilder;

public class GroupDateTime {
  private final String day;
  private final String month;
  private final String year;
  private final String time;

  public GroupDateTime(String day, String month, String year, String time) {
    this.day = day;
    this.month = month;
    this.year = year;
    this.time = time;
  }

  // Getters
  public String getDay() {
    return day;
  }

  public String getMonth() {
    return month;
  }

  public String getYear() {
    return year;
  }

  public String getTime() {
    return time;
  }
}
