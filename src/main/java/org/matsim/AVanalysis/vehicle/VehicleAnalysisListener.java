/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.AVanalysis.vehicle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.vehicles.Vehicle;

import org.matsim.AVanalysis.LinkFinder;
import org.matsim.AVanalysis.PassengerTracker;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.generator.AVUtils;

public class VehicleAnalysisListener implements PersonDepartureEventHandler, PersonArrivalEventHandler,
		ActivityStartEventHandler, ActivityEndEventHandler, LinkEnterEventHandler, PersonEntersVehicleEventHandler,
		PersonLeavesVehicleEventHandler {
	private final LinkFinder linkFinder;
	private final PassengerTracker passengers = new PassengerTracker();

	private final List<VehicleMovementItem> movements = new LinkedList<>();
	private final List<VehicleActivityItem> activities = new LinkedList<>();

	private final Map<Id<Vehicle>, VehicleMovementItem> currentMovements = new HashMap<>();
	private final Map<Id<Vehicle>, VehicleActivityItem> currentActivities = new HashMap<>();

	public VehicleAnalysisListener(LinkFinder linkFinder) {
		this.linkFinder = linkFinder;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getPersonId().toString().startsWith("AT_")) {
//			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleMovementItem movement = new VehicleMovementItem();
			movements.add(movement);

//			movement.operatorId = operatorId;
			movement.vehicleId = vehicleId;

			movement.originLink = linkFinder.getLink(event.getLinkId());
			movement.departureTime = event.getTime();

			currentMovements.put(vehicleId, movement);
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		if (event.getVehicleId().toString().startsWith("AT_")) {
			VehicleMovementItem movement = currentMovements.get(event.getVehicleId());

			if (movement == null) {
				throw new IllegalStateException("Found link enter event without departure");
			}

			movement.distance += linkFinder.getDistance(event.getLinkId());
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("AT_")) {
			if (event.getVehicleId().toString().startsWith("AT_")) {
				passengers.addPassenger(event.getVehicleId(), event.getPersonId());
			}
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("AT_")) {
			if (event.getVehicleId().toString().startsWith("AT_")) {
				passengers.removePassenger(event.getVehicleId(), event.getPersonId());
			}
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getPersonId().toString().startsWith("AT_")) {
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleMovementItem movement = currentMovements.remove(vehicleId);

			if (movement == null) {
				throw new IllegalStateException("Found arrival without departure");
			}

			movement.destinationLink = linkFinder.getLink(event.getLinkId());
			movement.arrivalTime = event.getTime();

			movement.numberOfPassengers = passengers.getNumberOfPassengers(vehicleId);
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (event.getPersonId().toString().startsWith("AT_")) {
//			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleActivityItem activity = new VehicleActivityItem();
			activities.add(activity);

//			activity.operatorId = operatorId;
			activity.vehicleId = vehicleId;

			activity.link = linkFinder.getLink(event.getLinkId());
			activity.type = event.getActType();

			activity.startTime = event.getTime();

			currentActivities.put(vehicleId, activity);
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (event.getPersonId().toString().startsWith("AT_")) {
//			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleActivityItem activity = currentActivities.remove(vehicleId);
			boolean isStarted = activity != null;

			if (!isStarted) {
				activity = new VehicleActivityItem();
				activities.add(activity);
			}

//			activity.operatorId = operatorId;
			activity.vehicleId = vehicleId;

			activity.link = linkFinder.getLink(event.getLinkId());
			activity.type = event.getActType();

			activity.endTime = event.getTime();
		}
	}

	@Override
	public void reset(int iteration) {
		passengers.clear();

		currentActivities.clear();
		currentMovements.clear();

		activities.clear();
		movements.clear();
	}

	public List<VehicleActivityItem> getActivities() {
		return activities;
	}

	public List<VehicleMovementItem> getMovements() {
		return movements;
	}
}