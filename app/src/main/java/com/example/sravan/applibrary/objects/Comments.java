package com.example.sravan.applibrary.objects;

/**
 * Created by Sravan on 1/17/2018.
 */

public class Comments {
    public String author;
    public String book;
    public int bookStars;
    public String review;
    public int timestamp;
    public String title;

    //Create a comment object to store all information for displaying and updating a comment
    //Server public no argument constructor
    public Comments() {

    }

    public Comments(String author, String book, int bookStars, String review, int timestamp, String title) {
        this.author = author;
        this.book = book;
        this.bookStars = bookStars;
        this.review = review;
        this.timestamp = timestamp;
        this.title = title;

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public int getBookStars() {
        return bookStars;
    }

    public void setBookStars(int bookStars) {
        this.bookStars = bookStars;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
