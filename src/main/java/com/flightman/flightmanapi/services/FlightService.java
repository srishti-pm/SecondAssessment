package com.flightman.flightmanapi.services;

import java.sql.Time;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightman.flightmanapi.model.Airport;
import com.flightman.flightmanapi.model.Flight;
import com.flightman.flightmanapi.model.FlightModel;
import com.flightman.flightmanapi.repositories.AirportRepository;
import com.flightman.flightmanapi.repositories.BookingRepository;
import com.flightman.flightmanapi.repositories.FlightModelRepository;
import com.flightman.flightmanapi.repositories.FlightRepository;

@Service
public class FlightService {
        @Autowired
        private FlightRepository flightRepository;

        @Autowired
        private FlightModelRepository flightModelRepository;

        @Autowired
        private AirportRepository airportRepository;

        @Autowired
        private BookingRepository bookingRepository;

        public List<Flight> getAllFlights() {
                return flightRepository.findAll();
        }

        public Boolean validateAirport(final UUID airportId) {
                Airport a = airportRepository.findByAirportId(airportId);
                if (a != null) {
                        return true;
                }
                return false;
        }

        public Boolean validateFlightModel(final Integer modelId) {
                FlightModel m = flightModelRepository.findByFlightModelId(modelId);
                if (m != null) {
                        return true;
                }
                return false;
        }

        public List<Flight> getFlights(final String sourceAbv, final String destAbv) {
                List<Flight> flightList;
                if (sourceAbv == null && destAbv == null) {
                        flightList = flightRepository.findAll();
                }

                else if (destAbv == null) {
                        flightList = flightRepository.findBySourceAirportAirportAbvName(sourceAbv);
                }

                else if (sourceAbv == null) {
                        flightList = flightRepository.findByDestAirportAirportAbvName(destAbv);
                }

                else {
                        flightList = flightRepository.findBySourceAirportAirportAbvNameAndDestAirportAirportAbvName(
                                        sourceAbv, destAbv);
                }
                return flightList;
        }

        public Flight save(final Flight flight) {
                return flightRepository.save(flight);
        }

        public Flight update(final UUID flightId, final Time departureTime, final Time estArrivalTime,
                        final Integer flightModelId) {
                Flight f = flightRepository.findByFlightId(flightId);
                if (f != null) {
                        if (departureTime != null) {
                                f.setDepartureTime(departureTime);
                        }
                        if (estArrivalTime != null) {
                                f.setEstArrivalTime(estArrivalTime);
                        }
                        if (flightModelId != null) {
                                FlightModel m = flightModelRepository.findByFlightModelId(flightModelId);
                                if (m != null) {
                                        f.setFlightModel(m);
                                }
                        }
                        return flightRepository.save(f);
                }
                return null;

        }

        public Integer deleteFlightById(final UUID id) {
                Flight f = this.flightRepository.findByFlightId(id);
                this.bookingRepository.deleteByFlight(f);
                return this.flightRepository.deleteByFlightId(id);
        }
}
