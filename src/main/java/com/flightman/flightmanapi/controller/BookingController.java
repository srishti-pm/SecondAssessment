package com.flightman.flightmanapi.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flightman.flightmanapi.model.Booking;
import com.flightman.flightmanapi.services.BookingService;
import com.flightman.flightmanapi.utils.ClassToJsonString;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RequestMapping("/api")
@RestController
@Api("Set of endpoints for Creating, Finding, and Deleting Bookings.")
public class BookingController {
        @Autowired
        private BookingService bookingService;

        /*
         * Method to retrieve bookings present in the database for a userId.
         * If userId is not supplied, all bookings are returned.
         */
        @ApiOperation(value = "Get All Bookings for a given user ID", notes = "Returns all the bookings for a user")
        @ApiResponses({ @ApiResponse(code = 200, message = "Booking details are successfully retrieved"),
                        @ApiResponse(code = 400, message = "No bookings were found for this user"),
                        @ApiResponse(code = 500, message = "There was an unexpected problem during booking detail retrieval") })
        @GetMapping("/bookings")
        public ResponseEntity<List<Booking>> getBookings(@RequestParam(required = false) final UUID userId) {
                List<Booking> bookingsList = bookingService.get(userId);
                return new ResponseEntity<>(bookingsList, HttpStatus.OK);
        }

        /*
         * Method that creates a new record in the Booking table by associating the
         * supplied userId and flightId.
         * If failure occurs during booking, returns HTTP NO_CONTENT
         */
        @ApiOperation(value = "Create Bookings", notes = "Takes in the user ID, flight ID, seat number, and the date of the flight. It books the flight if seats are available and returns the booking details.")
        @ApiResponses({ @ApiResponse(code = 201, message = "The created booking is successfully returned. If there are no bookings, an empty list is returned."),
                        @ApiResponse(code = 500, message = "There was an unexpected problem while creating bookings") })
        @PostMapping("/bookings")
        public ResponseEntity<String> createBooking(final String userId, final String flightId,
                        @RequestParam(required = false) final String seatNumber, final String date,
                        final Boolean useRewardPoints)
                        throws JsonProcessingException {
                Date d;
                if (Boolean.FALSE.equals(this.bookingService.validateUser(userId))) {
                        return new ResponseEntity<>("Invalid User ID", HttpStatus.BAD_REQUEST);
                }
                if (Boolean.FALSE.equals(this.bookingService.validateFlight(flightId))) {
                        return new ResponseEntity<>("Invalid Flight ID", HttpStatus.BAD_REQUEST);
                }
                if (useRewardPoints == null) {
                        return new ResponseEntity<>("Invalid Reward Point Flag", HttpStatus.BAD_REQUEST);
                }
                try {
                        SimpleDateFormat dateFor = new SimpleDateFormat("MM-dd-yyyy");
                        d = dateFor.parse(date);
                        if (d.before(new Date(new Date().getTime() - (1000 * 60 * 60 * 24)))) {
                                return new ResponseEntity<>(
                                                "The date of booking is in the past! Please supply a valid date.",
                                                HttpStatus.BAD_REQUEST);
                        }
                } catch (Exception e) {
                        return new ResponseEntity<>("Invalid date supplied", HttpStatus.BAD_REQUEST);
                }
                Booking booking = this.bookingService.book(userId, flightId, seatNumber, date, useRewardPoints);
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                ClassToJsonString cls = new ClassToJsonString(booking);
                return new ResponseEntity<>(booking != null ? cls.getJsonString() : "Not enough reward points!",
                                httpHeaders,
                                booking != null ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
        }

        @PostMapping("/bookings/id/{id}/usercheckin")
        public ResponseEntity<String> userCheckIn(@PathVariable("id") final UUID bookingId) {
                String checkedIn = this.bookingService.checkInUser(bookingId);
                return new ResponseEntity<>(checkedIn, HttpStatus.OK);
        }

        /*
         * Method that checks-in a user's luggage for a specific flight booking.
         * Returns 400 if input params are invalid.
         * If failure occurs during check-in despite input fields being valid, returns
         * HTTP 503
         */
        @ApiOperation(value = "Luggage Check-In", notes = "Takes in the booking ID, luggage count, and total weight of the luggage and checks-in the same.")
        @ApiResponses({ @ApiResponse(code = 200, message = "Luggage check-in was successful."),
                        @ApiResponse(code = 400, message = "The supplied paramters were invalid"),
                        @ApiResponse(code = 503, message = "The check-in service is temporarily unavailable") })
        @PostMapping("/bookings/id/{id}/luggagecheckin")
        public ResponseEntity<String> luggageCheckIn(@PathVariable("id") final String bookingId,
                        @RequestParam(required = true) final Integer count,
                        @RequestParam(required = true) final float totalWeight) {
                if (bookingId == null || bookingId.equals("")
                                || Boolean.TRUE.equals(!this.bookingService.validateBooking(bookingId))) {
                        return new ResponseEntity<>("Invalid Booking ID", HttpStatus.BAD_REQUEST);
                }
                if (Boolean.FALSE.equals(this.bookingService.validateCheckInTime(bookingId))) {
                        return new ResponseEntity<>("Check in is only allowed two hours before flight departure",
                                        HttpStatus.BAD_REQUEST);
                }
                if (Boolean.TRUE.equals(this.bookingService.getLuggageCheckInStatus(bookingId))) {
                        return new ResponseEntity<>("Luggage has been already checked in!",
                                        HttpStatus.BAD_REQUEST);
                }
                if (count <= 0 || count > 2) {
                        return new ResponseEntity<>("Only 1 to 2 luggages are allowed",
                                        HttpStatus.BAD_REQUEST);
                }
                if (totalWeight <= 0 || totalWeight > 46) {
                        return new ResponseEntity<>("Both luggages can weigh only upto 46 kgs",
                                        HttpStatus.BAD_REQUEST);
                }
                if (Boolean.TRUE.equals(this.bookingService.checkInLuggage(bookingId, count, totalWeight))) {
                        return new ResponseEntity<>("Luggage checked In Successfully!", HttpStatus.OK);
                }
                return new ResponseEntity<>("Unable to check in luggage!", HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ApiOperation(value = "Delete a booking", notes = "Delete a booking for a user")
        @ApiResponses({ @ApiResponse(code = 200, message = "Booking was successfully deleted"),
                        @ApiResponse(code = 400, message = "Incorrect or invalid data"),
                        @ApiResponse(code = 500, message = "There was an unexpected problem during booking deletion") })
        @Transactional
        @DeleteMapping("/bookings")
        public ResponseEntity<String> deleteBooking(final String bookingId, final String userId) {
                if (bookingId == null || bookingId.equals("")
                                || Boolean.TRUE.equals(!bookingService.validateBooking(bookingId))) {
                        return new ResponseEntity<>("Invalid Booking ID", HttpStatus.BAD_REQUEST);
                }
                if (Boolean.FALSE.equals(bookingService.validateUser(userId))) {
                        return new ResponseEntity<>("Invalid User ID", HttpStatus.BAD_REQUEST);
                }
                Boolean ret = bookingService.deleteBooking(bookingId, userId);
                return new ResponseEntity<>(Boolean.TRUE.equals(ret) ? "Successfully cancelled booking"
                                : "Could not cancel booking", HttpStatus.OK);
        }
}
