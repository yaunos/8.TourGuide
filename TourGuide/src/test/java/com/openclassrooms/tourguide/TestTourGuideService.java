package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTourGuideService {

	@Test
	// public void getUserLocation() {
		public void getUserLocation() throws ExecutionException, InterruptedException {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void addUser() {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	// public void trackUser() {
	public void trackUser() throws ExecutionException, InterruptedException {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	//@Disabled // Not yet implemented
	@Test
	// public void getNearbyAttractions() {
	public void getNearbyAttractions() throws ExecutionException, InterruptedException {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);

		tourGuideService.tracker.stopTracking();

		assertEquals(5, attractions.size());
	}

	// on ajoute @Test pour pouvoir faire tourner le test suivant
	// @Test
	public void getTripDeals() {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(new RewardCentral(), gpsUtilService);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);

		tourGuideService.tracker.stopTracking();

		assertEquals(10, providers.size());
	}

}
