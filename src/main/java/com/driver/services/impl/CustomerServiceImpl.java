package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		List<Customer> customers = customerRepository2.findAll();
		for(Customer customer : customers){
			if(customer.getCustomerId() == customerId){
				customers.remove(customer);
				return;
			}
		}
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

			Driver driver = null;
		    Cab cab = null;
			int min = Integer.MAX_VALUE;
			List<Driver> drivers = driverRepository2.findAll();
			for(Driver driver1 : drivers){
				if(driver1.getDriverId() < min && driver1.getCab().getAvailable() == true){
					min = driver1.getDriverId();
					cab = driver1.getCab();
					driver = driver1;
				}
			}
			if(cab == null){
				throw new Exception("No cab available!");
			}

			cab.setAvailable(false); //cab booked

			TripBooking tripBooking = new TripBooking();
			tripBooking.setFromLocation(fromLocation);
			tripBooking.setToLocation(toLocation);
			tripBooking.setDistanceInKm(distanceInKm);
			tripBooking.setStatus(TripStatus.CONFIRMED);
		    tripBooking.setDriver(driver);  //setting driver for trip
			Customer customer = customerRepository2.findById(customerId).get();
			tripBooking.setCustomer(customer);  //setting customer for trip

			driver.setCab(cab);  //setting cab for driver
			driver.getTripBookings().add(tripBooking);
			driverRepository2.save(driver);

			customer.getTripBookings().add(tripBooking);
			customerRepository2.save(customer);  //saves customer and trip booking
			return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setFromLocation(null);
		tripBooking.setToLocation(null);
		tripBooking.setDistanceInKm(0);
		tripBooking.setStatus(TripStatus.CANCELED);

		Customer customer = customerRepository2.findById(tripBooking.getCustomer().getCustomerId()).get();
		customer.getTripBookings().remove(tripBooking);
		tripBooking.setCustomer(null);

		Driver driver = driverRepository2.findById(tripBooking.getDriver().getDriverId()).get();
		driver.getTripBookings().remove(tripBooking);
		driver.getCab().setAvailable(true);
		driverRepository2.save(driver);
		tripBooking.setDriver(null);
		customerRepository2.save(customer);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);
	}
}
