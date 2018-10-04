package com.fabiosomaglia.onlinebookstore.model;

import com.fabiosomaglia.onlinebookstore.model.Book;

import java.util.Date;

public class Booking {

    private int bookingId;
    private Book book;
    private String userId;
    private String startBooking;
    private String endBooking;

    public Booking(int bookingId, Book book, String userId, String startBooking, String endBooking) {
        this.bookingId = bookingId;
        this.book = book;
        this.userId = userId;
        this.startBooking = startBooking;
        this.endBooking = endBooking;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartBooking() {
        return startBooking;
    }

    public void setStartBooking(String startBooking) {
        this.startBooking = startBooking;
    }

    public String getEndBooking() {
        return endBooking;
    }

    public void setEndBooking(String endBooking) {
        this.endBooking = endBooking;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", book=" + book +
                ", userId='" + userId + '\'' +
                ", startBooking=" + startBooking +
                ", endBooking=" + endBooking +
                '}';
    }
}