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

package org.matsim.trento.examples;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.analysis.kai.KaiAnalysisListener;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.DefaultTeleportationEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimBuilder;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFClientLive;
import org.matsim.vis.otfvis.OTFVisConfigGroup;
import org.matsim.vis.otfvis.OnTheFlyServer;
import sun.util.logging.resources.logging;

class KNControler {
	
	public static void main ( String[] args ) {
		OutputDirectoryLogging.catchLogEntries();
		
		Logger.getLogger("blabla").warn("here") ;
		
		// prepare the config:
		Config config ;
		if ( args.length>0 ) {
			config = ConfigUtils.loadConfig( args[0] ) ;
		} else {
			config = ConfigUtils.loadConfig("examples/equil/config.xml") ;
		}

//		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		
		config.plans().setRemovingUnneccessaryPlanAttributes(true) ;
		config.controler().setOverwriteFileSetting( OverwriteFileSetting.deleteDirectoryIfExists );
		
		// prepare the scenario
		Scenario scenario = ScenarioUtils.loadScenario( config ) ;
		
//		Random rnd = new Random(4711) ;
//		final Map<Id<Person>, ? extends Person> pop = scenario.getPopulation().getPersons();
//		Iterator<Id<Person>> it = pop.keySet().iterator() ;
//		while ( it.hasNext() ) {
//			it.next() ;
//			if ( rnd.nextDouble() < 0.9 ) {
//				it.remove();
//			}
//		}

		// prepare the control(l)er:
		Controler controler = new Controler( scenario ) ;
		controler.addControlerListener(new KaiAnalysisListener()) ;
//		controler.addOverridingModule(new OTFVisLiveModule());
//		controler.setMobsimFactory(new OldMobsimFactory()) ;
		
//		controler.addOverridingModule(new AbstractModule(){
//			@Override public void install() {
//				this.addEventHandlerBinding().toInstance(new BasicEventHandler(){
//					@Override public void reset(int iteration) { }
//					@Override public void handleEvent(Event event) {
//						if ( event instanceof HasPersonId ) {
//							if ( ((HasPersonId)event).getPersonId().equals( Id.createPersonId("5441604") ) ) {
//								Logger.getLogger(getClass()).warn( event );
//							}
//						}
//					}
//				});
//			}
//		});
		

		// run everything:
		controler.run();
	
	}
	
//	static class OldMobsimFactory implements MobsimFactory {
//		@Override
//		public Mobsim createMobsim( Scenario sc, EventsManager eventsManager ) {
//			return new QueueSimulation(sc, eventsManager);
//		}
//	}
	
	static class PatnaMobsimFactory implements MobsimFactory {
		private boolean useOTFVis = false ;

		@Override
		public Mobsim createMobsim(Scenario sc, EventsManager eventsManager) {
			
	        QSimConfigGroup conf = sc.getConfig().qsim();
	        if (conf == null) {
	            throw new NullPointerException("There is no configuration set for the QSim. Please add the module 'qsim' to your config file.");
	        }

	        // construct the QSim:
			QSim qSim = new QSimBuilder(sc.getConfig()).useDefaults().build(sc, eventsManager);

			// add the actsim engine:
			//ActivityEngine activityEngine = new ActivityEngine(eventsManager, qSim.getAgentCounter());
			//qSim.addMobsimEngine(activityEngine);
			//qSim.addActivityHandler(activityEngine);

			// add the netsim engine:

			//QNetsimEngine netsimEngine = new QNetsimEngine(qSim) ;
//			QNetsimEngine netsimEngine = netsimEngFactory.createQSimEngine(qSim, MatsimRandom.getRandom());
			//qSim.addMobsimEngine(netsimEngine);
			//qSim.addDepartureHandler(netsimEngine.getDepartureHandler());

			//DefaultTeleportationEngine teleportationEngine = new DefaultTeleportationEngine(sc, eventsManager);
			//qSim.addMobsimEngine(teleportationEngine);
	        
			//AgentFactory agentFactory;
//	        if (sc.getConfig().transit().isUseTransit()) {
//	            agentFactory = new TransitAgentFactory(qSim);
//	            TransitQSimEngine transitEngine = new TransitQSimEngine(qSim);
//	            transitEngine.setUseUmlaeufe(true);
//	            transitEngine.setTransitStopHandlerFactory(new ComplexTransitStopHandlerFactory());
//	            qSim.addDepartureHandler(transitEngine);
//	            qSim.addAgentSource(transitEngine);
//	            qSim.addMobsimEngine(transitEngine);
//	        } else {
			//agentFactory = new DefaultAgentFactory(qSim);
//	        }
//	        if (sc.getConfig().network().isTimeVariantNetwork()) {
//				qSim.addMobsimEngine(new NetworkChangeEventsEngine());		
//			}

	        //PopulationAgentSource agentSource = new PopulationAgentSource(sc.getPopulation(), agentFactory, qSim);
			//	        setter for ModeVehicleTypes to agent source is gone now. Amit May'17
//	        Map<String, VehicleType> modeVehicleTypes = new HashMap<String, VehicleType>();
//
//	        VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create("car", VehicleType.class));
//	        car.setMaximumVelocity(60.0/3.6);
//	        car.setPcuEquivalents(1.0);
//	        modeVehicleTypes.put("car", car);
//
//	        VehicleType bike = VehicleUtils.getFactory().createVehicleType(Id.create("bike", VehicleType.class));
//	        bike.setMaximumVelocity(60.0/3.6);
//	        bike.setPcuEquivalents(0.25);
//	        modeVehicleTypes.put("bike", bike);
//
//	        VehicleType bicycles = VehicleUtils.getFactory().createVehicleType(Id.create("bicycle", VehicleType.class));
//	        bicycles.setMaximumVelocity(15.0/3.6);
//	        bicycles.setPcuEquivalents(0.05);
//	        modeVehicleTypes.put("bicycle", bicycles);
//
//	        VehicleType walks = VehicleUtils.getFactory().createVehicleType(Id.create("walk", VehicleType.class));
//	        walks.setMaximumVelocity(1.5);
//	        walks.setPcuEquivalents(0.10);  			// assumed pcu for walks is 0.1
//	        modeVehicleTypes.put("walk", walks);
//
//			agentSource.setModeVehicleTypes(modeVehicleTypes);
	        
	        //qSim.addAgentSource(agentSource);
			
			if ( useOTFVis ) {
				// otfvis configuration.  There is more you can do here than via file!
				final OTFVisConfigGroup otfVisConfig = ConfigUtils.addOrGetModule(qSim.getScenario().getConfig(), OTFVisConfigGroup.GROUP_NAME, OTFVisConfigGroup.class);
				otfVisConfig.setDrawTransitFacilities(false) ; // this DOES work
				//				otfVisConfig.setShowParking(true) ; // this does not really work

				OnTheFlyServer server = OTFVis.startServerAndRegisterWithQSim(sc.getConfig(), sc, eventsManager, qSim);
				OTFClientLive.run(sc.getConfig(), server);
			}
			return qSim ;
		}
	}
}