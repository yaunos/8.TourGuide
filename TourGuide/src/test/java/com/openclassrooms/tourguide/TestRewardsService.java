package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRewardsService {

	@Test
	// public void userGetRewards() {
	public void userGetRewards() throws ExecutionException, InterruptedException {
		// GpsUtil gpsUtil = new GpsUtil();
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();

		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		RewardsService rewardsService = new RewardsService(tourGuideModule.getRewardCentral(), gpsUtilService);

		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		//Attraction attraction = gpsUtil.getAttractions().get(0);
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		assertTrue(userRewards.size() == 1);
	}

	@Test
	public void isWithinAttractionProximity() {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();

		/**
		 *
		 */
		RewardsService rewardsService = new RewardsService(tourGuideModule.getRewardCentral(), gpsUtilService);

		Attraction attraction = gpsUtilService.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	//@Disabled // Needs fixed - can throw ConcurrentModificationException
	@Test
	public void nearAllAttractions() {
		// GpsUtil gpsUtil = new GpsUtil();
		// RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		TourGuideModule tourGuideModule = new TourGuideModule();
		GpsUtilService gpsUtilService = new GpsUtilService();

		/**
		 *
		 */
		RewardsService rewardsService = new RewardsService(tourGuideModule.getRewardCentral(), gpsUtilService);

		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);


		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());


	/*
		// Get the user for which you want to find the nearest attractions
		User user = tourGuideService.getAllUsers().get(0);

		// Calculate rewards for each attraction in parallel
		List<Attraction> attractions = gpsUtilService.getAttractions();
		List<UserReward> userRewards = attractions.parallelStream()
				.map(attraction -> {
					rewardsService.calculateRewards(user, attraction);
					return tourGuideService.getUserRewards(user, attraction);
				})
				.collect(Collectors.toList());

		tourGuideService.tracker.stopTracking();

		assertEquals(attractions.size(), userRewards.size());
	 */

	}

}
