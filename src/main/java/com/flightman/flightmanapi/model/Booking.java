package com.flightman.flightmanapi.model;
import java.util.UUID;

import javax.persistence.*;

@Entity
@Table(name = "booking")
public class Booking {
        @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private UUID bookingId;

        @OneToOne
        @JoinColumn(name = "user_id")
        private User user;

        @OneToOne
        @JoinColumn(name = "flight_id")
        private Flight flight;

        @Column(name = "seatNumber")
        private String seatNumber;

        @Column(name = "paymentStatus")
        private Boolean paymentStatus;
        
        public Booking(){}

        public Booking(User user, Flight flight, String seatNumber, Boolean paymentStatus){
                this.user = user;
                this.flight = flight;
                this.seatNumber = seatNumber;
                this.paymentStatus = paymentStatus;
        }

        public UUID getBookingId() {
                return this.bookingId;
        }

        public void setBookingId(UUID bookingId) {
                this.bookingId = bookingId;
        }

        public User getUser() {
                return this.user;
        }

        public void setUser(User user) {
                this.user = user;
        }

        public Flight getFlight() {
                return this.flight;
        }

        public void setFlight(Flight flight) {
                this.flight = flight;
        }

        public String getSeatNumber() {
                return this.seatNumber;
        }

        public void setSeatNumber(String seatNumber) {
                this.seatNumber = seatNumber;
        }

        public Boolean getPaymentStatus() {
                return this.paymentStatus;
        }

        public void setPaymentStatus(Boolean paymentStatus) {
                this.paymentStatus = paymentStatus;
        }

}