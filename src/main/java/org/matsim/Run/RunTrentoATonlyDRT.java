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
package org.matsim.Run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareConfigGroup;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.contrib.roadpricing.RoadPricingConfigGroup;
import org.matsim.contrib.roadpricing.RoadPricingModule;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

/**
 * @author MAlbertini 
 *
 */
public class RunTrentoATonlyDRT{
	public static void main(String[] args) {
		if ( args.length==0 ) {
			args = new String [] { "scenarios/Car2DRT/AT_OnlyDRT_Config.xml" } ;
		} else {
			Gbl.assertIf( args[0] != null && !args[0].equals( "" ) );
		}

		Config config = ConfigUtils.loadConfig( args, new DvrpConfigGroup(),new DrtConfigGroup(),
				new DrtFareConfigGroup(),new RoadPricingConfigGroup(), new SwissRailRaptorConfigGroup() ) ;
		
		// possibly modify config here----------------------------------------------------------------------------------------------
		
//		config.controler().setLastIteration( 1 );	

		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		config.plansCalcRoute().setInsertingAccessEgressWalk( true );
		config.checkConsistency();
		
		// ------------------------------------------------------------------------------------------------------------------------
		
		Scenario scenario = DrtControlerCreator.createScenarioWithDrtRouteFactory(config);		
		ScenarioUtils.loadScenario(scenario);

		// possibly modify scenario here-------------------------------------------------------------------------------------------

//		new NetworkCleaner().run( scenario.getNetwork() );
		
		Controler controler = new Controler( scenario ) ;
		
		String mode = DrtConfigGroup.getSingleModeDrtConfig(config).getMode();	
		
//		double flowEfficiencyFactor= 2.0;
//		String inputEvents="scenarios/Car2AT/Calibrated.output_events.xml.gz";
		
		// possibly modify controler here------------------------------------------------------------------------------------------

		// to speed up computations
//		final TravelTime initialTT = TravelTimeUtils.createTravelTimesFromEvents(controler.getScenario(), inputEvents);
//		controler.addOverridingModule(new AbstractModule() {
//			@Override
//			public void install() {
//				bind(TravelTime.class).annotatedWith(Names.named(DvrpTravelTimeModule.DVRP_INITIAL))
//						.toInstance(initialTT);
//			}
//		});		
		
		controler.addOverridingModule(new SwissRailRaptorModule());
//		controler.addOverridingModule( new OTFVisLiveModule() ) ;
		controler.addOverridingModule( new DrtFareModule() );
		controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule( new MultiModeDrtModule() );
//		controler.addOverridingModule(new AvIncreasedCapacityModule(flowEfficiencyFactor));
		controler.addOverridingModule( new RoadPricingModule() );
		controler.configureQSimComponents(DvrpQSimComponents.activateModes(mode));
             
		// ------------------------------------------------------------------------------------------------------------------------
		
		controler.run();
	}
	
}
