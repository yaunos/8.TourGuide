package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.domain.NearbyAttractionListDTO;
import com.openclassrooms.tourguide.domain.NearbyAttractionDTO;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	// private final GpsUtil gpsUtil;
	//
	private GpsUtilService gpsUtilService;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	// public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
	public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService) {
		// this.gpsUtil = gpsUtil;
		this.gpsUtilService = gpsUtilService;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

// 	public VisitedLocation getUserLocation(User user) {
	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {

	VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

//	public VisitedLocation trackUserLocation(User user) {
public VisitedLocation trackUserLocation(User user) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();

		Map<Attraction, Double > map = new HashMap<>();

		for (Attraction attraction : gpsUtilService.getAttractions()) {
		/*
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		*/


			map.put(attraction, rewardsService.getDistance(attraction, visitedLocation.location));
		}
		Map<Attraction, Double> mapSortedByDistance = map.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		mapSortedByDistance.forEach((k,v)->{
			if (nearbyAttractions.size()< 5){
				nearbyAttractions.add(k);
			}
		});

		return nearbyAttractions;
	}

	/**
	 * As requested in the TODO in the TourGuideController
	 *
	 * @param userName
	 * @return attractionName, attractionPosition, DistanceFromUser, RewardsPoints
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */

	public NearbyAttractionListDTO getNearByAttractionsWithInfos (String userName) throws ExecutionException, InterruptedException {

		NearbyAttractionListDTO nearbyAttractionsListDTO = new NearbyAttractionListDTO();

		VisitedLocation visitedLocation = getUserLocation(getUser(userName));
		nearbyAttractionsListDTO.setUserLocation(visitedLocation);

		List<NearbyAttractionDTO> listAttractionsDTO  =  new ArrayList();
		List<Attraction> listAttractions = getNearByAttractions(visitedLocation);
		listAttractions.forEach(attraction -> {
			NearbyAttractionDTO attractionToAdd = new NearbyAttractionDTO();
			attractionToAdd.setAttractionName(attraction.attractionName);
			attractionToAdd.setAttractionPosition(new Location(attraction.latitude,attraction.longitude));
			attractionToAdd.setDistanceFromUser(rewardsService.getDistance(attraction, visitedLocation.location));
			// attractionToAdd.setRewardPoints(rewardCentral.getAttractionRewardPoints(attraction.attractionId, getUser(userName).getUserId()));
			listAttractionsDTO.add(attractionToAdd);
		});
		nearbyAttractionsListDTO.setAttractions(listAttractionsDTO);

		return nearbyAttractionsListDTO;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
