package com.example.sravan.applibrary.objects;

/**
 * Created by Sravan on 1/17/2018.
 */

public class User {
    public String email;
    public String password;
    public String name;
    public int grade;
    public int books;
    public int cart;
    public String id;

    //Create user object to store all information for displaying and updating the user
    //Server public no argument constructor
    public User (){

    }

    public User(String email, String password, String name, int grade, int books, int cart, String id){
        this.email = email;
        this.password = password;
        this.name = name;
        this.grade = grade;
        this.books = books;
        this.cart = cart;
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getBooks() {
        return books;
    }

    public void setBooks(int books) {
        this.books = books;
    }

    public int getCart() {
        return cart;
    }

    public void setCart(int cart) {
        this.cart = cart;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
