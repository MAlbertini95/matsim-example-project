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

package org.matsim.Generator.Demand;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.util.random.RandomUtils;
import org.matsim.contrib.util.random.UniformRandom;
import org.matsim.contrib.zone.Zone;
import org.matsim.contrib.zone.util.RandomPointUtils;
import org.matsim.core.network.NetworkUtils;

public class DefaultActivityCreator implements ODDemandGenerator.ActivityCreator<Zone> {
	protected final UniformRandom uniform = RandomUtils.getGlobalUniform();
	protected final Network network;
	protected final PopulationFactory pf;

	protected final GeometryProvider geometryProvider;
	protected final PointAcceptor pointAcceptor;

	public DefaultActivityCreator(Scenario scenario) {
		this(scenario, DEFAULT_GEOMETRY_PROVIDER, DEFAULT_POINT_ACCEPTOR);
	}

	public DefaultActivityCreator(Scenario scenario, GeometryProvider geometryProvider, PointAcceptor pointAcceptor) {
		this.network = scenario.getNetwork();
		this.pf = scenario.getPopulation().getFactory();
		this.geometryProvider = geometryProvider;
		this.pointAcceptor = pointAcceptor;
	}

	@Override
	public Activity createActivity(Zone zone, String actType) {
		Geometry geometry = geometryProvider.getGeometry(zone, actType);
		Point p = null;
		do {
			p = RandomPointUtils.getRandomPointInGeometry(geometry);
		} while (!pointAcceptor.acceptPoint(zone, actType, p));

		Coord coord = new Coord(p.getX(), p.getY());
		Link link = NetworkUtils.getNearestLink(network, coord);

		Activity activity = pf.createActivityFromCoord(actType, coord);
		activity.setLinkId(link.getId());
		return activity;
	}

	public static final GeometryProvider DEFAULT_GEOMETRY_PROVIDER = new GeometryProvider() {
		public Geometry getGeometry(Zone zone, String actType) {
			return zone.getMultiPolygon();
		}
	};

	public interface GeometryProvider {
		Geometry getGeometry(Zone zone, String actType);
	}

	public static final PointAcceptor DEFAULT_POINT_ACCEPTOR = new PointAcceptor() {
		public boolean acceptPoint(Zone zone, String actType, Point point) {
			return true;
		}
	};

	public interface PointAcceptor {
		boolean acceptPoint(Zone zone, String actType, Point point);
	}
}