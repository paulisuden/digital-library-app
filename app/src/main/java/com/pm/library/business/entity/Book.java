package com.pm.library.business.entity;

public class Book {
    private int id;
    private String title, isbn, author;
    private int publicationYear;
    private double price;
    private String image;

    public Book(int id, String title, String isbn, String author, int publicationYear, double price, String image) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.publicationYear = publicationYear;
        this.price = price;
        this.image = image;
    }

    public Book() {
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", author='" + author + '\'' +
                ", publicationYear=" + publicationYear +
                ", price=" + price +
                '}';
    }
}