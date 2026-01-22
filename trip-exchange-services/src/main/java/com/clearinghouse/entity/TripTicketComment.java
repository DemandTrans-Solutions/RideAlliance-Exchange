/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.entity;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 *
 * @author chaitanyaP
 */
@Entity
@Table(name = "tripticketcomment")
public class TripTicketComment extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TripTicketCommentID")
    private int id;

    // Default constructor
    public TripTicketComment() {
    }

    // Constructor with ID parameter
    public TripTicketComment(int id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TripTicketID")
    private TripTicket tripTicket;

    @OneToOne
    @JoinColumn(name = "UserID")
    private User user;

    @Column(name = "CommentText")
    private String body;

    @Column(name = "NameOfUser")
    private String userName;

    @Column(name = "NameOfProvider")
    private String nameOfProvider;

    public String getNameOfProvider() {
        return nameOfProvider;
    }

    public void setNameOfProvider(String nameOfProvider) {
        this.nameOfProvider = nameOfProvider;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TripTicket getTripTicket() {
        return tripTicket;
    }

    public void setTripTicket(TripTicket tripTicket) {
        this.tripTicket = tripTicket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public String toString() {
        return "TripTicketComment{" +
                "id=" + id +
                ", tripTicket=" + tripTicket +
                ", user=" + user +
                ", body='" + body + '\'' +
                ", userName='" + userName + '\'' +
                ", nameOfProvider='" + nameOfProvider + '\'' +
                '}';
    }
}