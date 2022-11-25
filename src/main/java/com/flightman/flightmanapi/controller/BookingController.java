package com.flightman.flightmanapi.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flightman.flightmanapi.model.Booking;
import com.flightman.flightmanapi.services.BookingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RequestMapping("/api")
@RestController
@Api(description = "Set of endpoints for Creating, Finding, and Deleting Bookings.")
public class BookingController {
        @Autowired
        private BookingService bookingService;

        /* 
         * Method to retrieve bookings present in the database for a userId. 
         * If userId is not supplied, all bookings are returned. 
        */
    @ApiOperation(value = "Get All Bookings for a given user ID", notes = "Returns all the bookings for a user")
    @ApiResponses({ @ApiResponse(code = 200, message = "Booking details are successfully retrieved"),
                    @ApiResponse(code = 400, message = "No bookings where found for this user"),
                    @ApiResponse(code = 500, message = "There was an unexpected problem during booking detail retrieval") })
	@GetMapping("/bookings")
	public ResponseEntity<List<Booking>> getBookings(@RequestParam(required = false) UUID userId) {
		try {
                        List<Booking> bookingsList = new ArrayList<Booking>();
                        bookingsList = bookingService.get(userId);
                        if(!bookingsList.isEmpty())
                                return new ResponseEntity<>(bookingsList, HttpStatus.OK);
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
                        e.printStackTrace(new java.io.PrintStream(System.err));
                        System.err.println(e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    /* 
     * Method that creates a new record in the Booking table by associating the supplied userId and flightId. 
     * If failure occurs during booking, returns HTTP NO_CONTENT 
    */
    @ApiOperation(value = "Create a new booking", notes = "Create a new booking for a user")
    @ApiResponses({ @ApiResponse(code = 200, message = "Booking was successfully created"),
                    @ApiResponse(code = 400, message = "Incorrect or invalid data"),
                    @ApiResponse(code = 500, message = "There was an unexpected problem during booking creation") })
    @PostMapping("/bookings")
	public ResponseEntity<Booking> createBooking(String userId, String flightId, @RequestParam(required = false) String seatNumber, @DateTimeFormat(pattern = "MM-dd-yyyy") Date date, Boolean useRewardPoints) {
		try {
                        Booking booking = this.bookingService.book(userId, flightId, seatNumber, date, true, useRewardPoints);
                        if (booking != null){
                                return new ResponseEntity<>(booking, HttpStatus.OK);
                        }
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
                        e.printStackTrace(new java.io.PrintStream(System.err));
                        System.err.println(e);
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @ApiOperation(value = "Delete a booking", notes = "Delete a booking for a user")
    @ApiResponses({ @ApiResponse(code = 200, message = "Booking was successfully deleted"),
                    @ApiResponse(code = 400, message = "Incorrect or invalid data"),
                    @ApiResponse(code = 500, message = "There was an unexpected problem during booking deletion") })
    @DeleteMapping("/bookings")
    public ResponseEntity<Boolean> deleteBooking(String bookingId, String userId) {
        try {
                        Boolean ret = this.bookingService.deleteBooking(bookingId, userId);
                        return new ResponseEntity<>(ret, HttpStatus.OK);
        } catch (Exception e) {
                        e.printStackTrace(new java.io.PrintStream(System.out));
                        System.out.println(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Check in a user for their booking", notes = "Check a user in")
    @ApiResponses({ @ApiResponse(code = 200, message = "User was successfully checked in"),
                    @ApiResponse(code = 400, message = "Incorrect or invalid data"),
                    @ApiResponse(code = 500, message = "There was an unexpected problem during check in") })
    @PostMapping("/bookings/id/{id}/usercheckin")
	public ResponseEntity<String> userCheckIn(@PathVariable("id") UUID bookingId) {
		try {
                        String checkedIn = this.bookingService.update(bookingId);
                        return new ResponseEntity<>(checkedIn, HttpStatus.OK);
                        
		} catch (Exception e) {
                        e.printStackTrace(new java.io.PrintStream(System.err));
                        System.err.println(e);
			return new ResponseEntity<>("There was a error with the checkin process!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
