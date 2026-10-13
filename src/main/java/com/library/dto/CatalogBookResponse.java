package com.library.dto;

import com.library.model.BookItem;
import com.library.model.BookStatus;
import com.library.model.BookTitle;

import java.util.List;

public class CatalogBookResponse {

    private String isbn;
    private String title;
    private String author;
    private String subject;
    private Integer publicationYear;
    private String callNumber;
    private String description;
    private String coverImage;
    private BookStatus displayStatus;
    private String activeBarcode;
    private String shelfLocation;
    private int totalCopies;
    private int availableCopies;
    private List<BookItem> items;

    public CatalogBookResponse() {}

    public CatalogBookResponse(BookTitle title, List<BookItem> items) {
        this.isbn = title.getIsbn();
        this.title = title.getTitle();
        this.author = title.getAuthor();
        this.subject = title.getSubject();
        this.publicationYear = title.getPublicationYear();
        this.callNumber = title.getCallNumber();
        this.description = title.getDescription();
        this.coverImage = title.getCoverImage();
        this.items = items;
        this.totalCopies = items.size();

        long availCount = items.stream().filter(i -> i.getStatus() == BookStatus.AVAILABLE).count();
        this.availableCopies = (int) availCount;

        if (availCount > 0) {
            this.displayStatus = BookStatus.AVAILABLE;
            BookItem firstAvail = items.stream().filter(i -> i.getStatus() == BookStatus.AVAILABLE).findFirst().orElse(null);
            if (firstAvail != null) {
                this.activeBarcode = firstAvail.getBarcode();
                this.shelfLocation = firstAvail.getShelfLocation();
            }
        } else if (items.stream().anyMatch(i -> i.getStatus() == BookStatus.DIGITAL)) {
            this.displayStatus = BookStatus.DIGITAL;
            BookItem digitalItem = items.stream().filter(i -> i.getStatus() == BookStatus.DIGITAL).findFirst().orElse(null);
            if (digitalItem != null) {
                this.activeBarcode = digitalItem.getBarcode();
                this.shelfLocation = digitalItem.getShelfLocation();
            }
        } else if (items.stream().anyMatch(i -> i.getStatus() == BookStatus.RESERVED)) {
            this.displayStatus = BookStatus.RESERVED;
            BookItem reservedItem = items.stream().filter(i -> i.getStatus() == BookStatus.RESERVED).findFirst().orElse(null);
            if (reservedItem != null) {
                this.activeBarcode = reservedItem.getBarcode();
                this.shelfLocation = reservedItem.getShelfLocation();
            }
        } else if (!items.isEmpty()) {
            this.displayStatus = items.get(0).getStatus();
            this.activeBarcode = items.get(0).getBarcode();
            this.shelfLocation = items.get(0).getShelfLocation();
        } else {
            this.displayStatus = BookStatus.AVAILABLE;
        }
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public BookStatus getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(BookStatus displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getActiveBarcode() {
        return activeBarcode;
    }

    public void setActiveBarcode(String activeBarcode) {
        this.activeBarcode = activeBarcode;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public List<BookItem> getItems() {
        return items;
    }

    public void setItems(List<BookItem> items) {
        this.items = items;
    }
}
