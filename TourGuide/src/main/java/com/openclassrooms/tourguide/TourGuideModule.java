package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.domain.GpsUtilOptim;
import com.openclassrooms.tourguide.domain.RewardCentralOptim;
import gpsUtil.GpsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;

@Configuration
public class TourGuideModule {
	
	@Bean
	public GpsUtil getGpsUtil() {
		// return new GpsUtil();
		return new GpsUtilOptim();
	}
	
	// @Bean
	// public RewardsService getRewardsService() {
	// 	return new RewardsService(getGpsUtil(), getRewardCentral());

	// }
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentralOptim();
	}
	
}
