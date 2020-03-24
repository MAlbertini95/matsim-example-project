/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.Generator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.AVanalysis.AVUtils;
import org.matsim.vehicles.VehicleType;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;

public class ATPopDensityGenerator implements AVGenerator {
	static public final String TYPE = "PopulationDensity";

	private final int randomSeed;
	private final long numberOfVehicles;
	private final VehicleType vehicleType;
//	private final Id<AVOperator> operatorId ;

	private List<Link> linkList = new LinkedList<>();
	private Map<Link, Double> cumulativeDensity = new HashMap<>();

	public ATPopDensityGenerator(int numberOfVehicles, Network network, Population population, ActivityFacilities facilities, int randomSeed, VehicleType vehicleType) {
		this.randomSeed = randomSeed;
		this.numberOfVehicles = numberOfVehicles;
		this.vehicleType = vehicleType;
//		this.operatorId = operatorId;

		// Determine density
		double sum = 0.0;
		Map<Link, Double> density = new HashMap<>();

		for (Person person : population.getPersons().values()) {
			Activity act = (Activity) person.getSelectedPlan().getPlanElements().get(0);
			Id<Link> linkId = act.getLinkId() != null ? act.getLinkId()
					: facilities.getFacilities().get(act.getFacilityId()).getLinkId();
			Link link = network.getLinks().get(linkId);

			if (link != null) {
				if (density.containsKey(link)) {
					density.put(link, density.get(link) + 1.0);
				} else {
					density.put(link, 1.0);
				}

				if (!linkList.contains(link))
					linkList.add(link);
				sum += 1.0; // TODO This looks strange!
			}
		}

		// Compute relative frequencies and cumulative
		double cumsum = 0.0;

		for (Link link : linkList) {
			cumsum += density.get(link) / sum;
			cumulativeDensity.put(link, cumsum);
		}
	}

	@Override
	public List<AVVehicle> generateVehicles() {
		List<AVVehicle> vehicles = new LinkedList<>();
		Random random = new Random(randomSeed);

		int generatedNumberOfVehicles = 0;
		while (generatedNumberOfVehicles < numberOfVehicles) {
			generatedNumberOfVehicles++;

			// Multinomial selection
			double r = random.nextDouble();
			Link selectedLink = linkList.get(0);

			for (Link link : linkList) {
				if (r <= cumulativeDensity.get(link)) {
					selectedLink = link;
					break;
				}
			}

			Id<AVOperator> operatorId =AVOperator.createId("default") ;
			Id<DvrpVehicle> id = AVUtils.createId(operatorId, generatedNumberOfVehicles);
			vehicles.add(new AVVehicle(id, selectedLink, 0.0, Double.POSITIVE_INFINITY, vehicleType));
		}

		return vehicles;
	}

	static public class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject
		private Population population;

		@Inject
		private ActivityFacilities facilities;

		@Override
		public AVGenerator createGenerator( Network network, VehicleType vehicleType) {
//			GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();
			int numberOfVehicles = 12500 ;
			int randomSeed = 1234 ; //Integer.parseInt(generatorConfig.getParams().getOrDefault("randomSeed", "1234"));

			return new ATPopDensityGenerator( numberOfVehicles,
					network, population, facilities, randomSeed, vehicleType);
		}
	}
}