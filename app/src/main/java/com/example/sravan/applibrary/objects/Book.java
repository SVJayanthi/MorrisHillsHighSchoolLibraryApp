package com.example.sravan.applibrary.objects;

/**
 * Created by Sravan on 1/17/2018.
 */

public class Book {
    public String author;
    public int bookNumbers;
    public int bookOut;
    public int bookReviews;
    public int bookStars;
    public String description;
    public int targetGrade;
    public int timestamp;
    public String title;

    //Create a book object to store all information for displaying and updating a book
    //Server public no argument constructor
    public Book () {

    }

    public Book(String author, int bookNumbers, int bookOut, int bookReviews, int bookStars, String description, int targetGrade, String title) {
        this.author = author;
        this.bookNumbers = bookNumbers;
        this.bookOut = bookOut;
        this.bookReviews = bookReviews;
        this.bookStars = bookStars;
        this.description = description;
        this.targetGrade = targetGrade;
        this.timestamp = timestamp;
        this.title = title;

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getBookNumbers() {
        return bookNumbers;
    }

    public void setBookNumbers(int bookNumbers) {
        this.bookNumbers = bookNumbers;
    }

    public int getBookOut() {
        return bookOut;
    }

    public void setBookOut(int bookOut) {
        this.bookOut = bookOut;
    }

    public int getBookReviews() {
        return bookReviews;
    }

    public void setBookReviews(int bookReviews) {
        this.bookReviews = bookReviews;
    }

    public int getBookStars() {
        return bookStars;
    }

    public void setBookStars(int bookStars) {
        this.bookStars = bookStars;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTargetGrade() {
        return targetGrade;
    }

    public void setTargetGrade(int targetGrade) {
        this.targetGrade = targetGrade;
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
