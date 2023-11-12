package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	// private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	//public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
	//	this.gpsUtil = gpsUtil;
	//	this.rewardsCentral = rewardCentral;
	// }

	private GpsUtilService gpsUtilService;

	/**
	 * Improvement with concurrent threads : multithreading features
	 * Use of Java ExecutorService to parallelize the tracking of user locations
	 */

	private ExecutorService executorService = Executors.newFixedThreadPool(1000);

	public RewardsService(RewardCentral rewardCentral, GpsUtilService gpsUtilService) {
	this.rewardsCentral = rewardCentral;
	this.gpsUtilService = gpsUtilService;
}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	private ConcurrentHashMap<User, List<UserReward>> userRewardsMap = new ConcurrentHashMap<>();

	// adding "synchronized" to the method, doesn't change the result of the test
	public synchronized void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		// List<Attraction> attractions = gpsUtil.getAttractions();
		List<Attraction> attractions = gpsUtilService.getAttractions();

		// Create a copy of the userRewards list to avoid concurrent modification on the original list
		List<UserReward> userRewardsCopy = new ArrayList<>(user.getUserRewards());

		List<UserReward> userRewards = userRewardsMap.getOrDefault(user, new CopyOnWriteArrayList<>());

		for (VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {

				// Synchronize the above section to avoid concurrent modification
				// synchronized (user.getUserRewards()) {
				//	boolean attractionAlreadyReceived = user.getUserRewards()
				//			.stream()
				//			.anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));

				// /**
				// * Still getting errors (but less frequently while playing test) on nearAllAttractions() with the above commented solution
				// */
				// boolean attractionAlreadyReceived = userRewards
				boolean attractionAlreadyReceived = userRewardsCopy
					.stream()
					.anyMatch(r -> r.attraction.attractionName.equals(attraction.attractionName));


				if (!attractionAlreadyReceived && nearAttraction(visitedLocation, attraction)) {
					synchronized (user.getUserRewards()) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private synchronized boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
