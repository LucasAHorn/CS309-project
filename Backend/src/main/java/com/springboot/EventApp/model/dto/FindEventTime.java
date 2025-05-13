//package com.springboot.EventApp.model.dto;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
///**
// * This is used ot
// */
//public class FindEventTime {
//
//    private String startTime;
//
//    private String endTime;
//
//    private int lengthInMinutes;
//
//    private Long groupId;
//
//
//    public FindEventTime() {
//    }
//
//    public void setStartTime(String startTime) {
//        this.startTime = startTime;
//    }
//
//    public void setEndTime(String endTime) {
//        this.endTime = endTime;
//    }
//
//    public void setGroupId(Long groupId) {
//        this.groupId = groupId;
//    }
//
//    public Long getGroupId() {
//        return groupId;
//    }
//
//    public int getLengthInMinutes() {
//        return lengthInMinutes;
//    }
//
//    public void setLengthInMinutes(int lengthInMinutes) {
//        this.lengthInMinutes = lengthInMinutes;
//    }
//
//    public LocalDateTime getStartTime() {
//        return LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//    }
//
//    public LocalDateTime getEndTime() {
//        return LocalDateTime.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//    }
//}
