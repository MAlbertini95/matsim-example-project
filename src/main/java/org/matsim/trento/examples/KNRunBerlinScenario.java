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
/*
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.analysis.kai.KaiAnalysisListener;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.gbl.Gbl;

class KNRunBerlinScenario {
	private enum MyScenario { bln1pct, bln10pct, equil } ;
	private static MyScenario myScenario = MyScenario.bln1pct ;
	
	public static void main(String[] args) {
		String configFileName = "scenarios/berlin-v5.1-10pct/input/berlin-v5.1-10pct.config.xml" ;
//		String configFileName = "scenarios/berlin-v5.0-1pct-2018-06-18/input/berlin-5.1_config.xml";
//		String configFileName = "scenarios/berlin-v5.0-1pct/input/berlin-5.1_config_reduced.xml";
		
		// "overridingConfig.xml" is taken as an argument now which was hard-coded in there. Amit July'18
//		final RunBerlinScenario berlin = new RunBerlinScenario( configFileName);
		final RunBerlinScenario berlin = new RunBerlinScenario( configFileName, "overridingConfig.xml" );
		
		final Config config = berlin.prepareConfig() ;
		
		config.global().setNumberOfThreads( 6 );
		config.qsim().setNumberOfThreads( 6 );
		config.parallelEventHandling().setNumberOfThreads( 1 );
		
		config.controler().setLastIteration( 100 );

		config.strategy().setFractionOfIterationsToDisableInnovation( 0.8 );
		// 50 w/ 500
		// 20 w/ 100
		
		
		
		//		config.controler().setWritePlansInterval( 10 );
		config.controler().setOutputDirectory( "./output" );
		config.controler().setRunId( null );
		config.planCalcScore().setWriteExperiencedPlans( true );
//		{
//			final StrategySettings stratSets = new StrategySettings() ;
//			stratSets.setStrategyName( DefaultStrategy.ChangeSingleTripMode );
//			stratSets.setWeight( 0.2 );
//			stratSets.setSubpopulation( "person" );
//			config.strategy().addStrategySettings( stratSets );
//
//			config.changeMode().setModes( config.subtourModeChoice().getModes() );
//		}
//		for ( StrategySettings settings : config.strategy().getStrategySettings() ) {
//			if ( settings.getStrategyName().equals( DefaultStrategy.SubtourModeChoice ) ) {
//				settings.setWeight( 0.0 );
//			}
//		}
		//		config.transit().setUsingTransitInMobsim( false );
		
		switch( myScenario ) {
			case bln1pct:
			case bln10pct:
				break;
			case equil:
				config.global().setNumberOfThreads( 1 );
				config.qsim().setNumberOfThreads( 1 );
				config.parallelEventHandling().setNumberOfThreads( 1 );

				config.network().setInputFile( "../../equil/network.xml" );
//				config.plans().setInputFile( "../../equil/plans100.xml" );
				config.plans().setInputFile( "../../equil/plans2000.xml.gz" );
				
				config.qsim().setFlowCapFactor( 1.0 );
				config.qsim().setStorageCapFactor( 1.0 );

				config.transit().setUseTransit( false );
			{
				final ModeRoutingParams modeRoutingParams = new ModeRoutingParams( TransportMode.pt ) ;
				modeRoutingParams.setTeleportedModeFreespeedFactor(3. );
				config.plansCalcRoute().addModeRoutingParams( modeRoutingParams );
			}
			{
//			final ModeParams params = config.planCalcScore().getScoringParametersPerSubpopulation().get( "person" ).getModes().get( TransportMode.pt );
				final ModeParams params = config.planCalcScore().getModes().get( TransportMode.pt );
				params.setMarginalUtilityOfDistance( -0.0001 ); // yyyy should rather be a distance cost rate
			}
			{
				final ActivityParams params = config.planCalcScore().getActivityParams( "work_28800.0" );;
				Gbl.assertNotNull( params );
				params.setOpeningTime( 8*3600. );
				params.setLatestStartTime( 8*3600. );
			}
			config.planCalcScore().setLateArrival_utils_hr( -6 );
//			{
//				final ActivityParams actParams = new ActivityParams( "w" ) ;
//				actParams.setTypicalDuration( 8.*3600. );
//				actParams.setOpeningTime( 6.*3600. );
//				actParams.setClosingTime( 18.*3600. );
//				config.planCalcScore().getScoringParametersPerSubpopulation().get("person").addActivityParams( actParams );
//			}
//			{
//				final ActivityParams actParams = new ActivityParams( "h" ) ;
//				actParams.setTypicalDuration( 16.*3600. );
//				config.planCalcScore().getScoringParametersPerSubpopulation().get("person").addActivityParams( actParams );
//			}
				break;
		}
		
		final Scenario scenario = berlin.prepareScenario() ;
		
		switch ( myScenario ) {
			case bln1pct:
			case bln10pct:
				break;
			case equil:
				for ( Link link : scenario.getNetwork().getLinks().values() ) {
					final Set<String> allowedModes = new HashSet<>( link.getAllowedModes() ) ;
					allowedModes.add( "freight") ;
					allowedModes.add( "ride") ;
					link.setAllowedModes( allowedModes ) ;
				}
				for ( Person person : scenario.getPopulation().getPersons().values()  ) {
					person.getAttributes().putAttribute( config.plans().getSubpopulationAttributeName(), "person" ) ;
				}
				break;
		}
		
		List<AbstractModule> overridingModules = new ArrayList<>() ;
		
//		overridingModules.add( new AbstractModule() {
//			@Override public void install() {
//				DiversityGeneratingPlansRemover.Builder builder = new DiversityGeneratingPlansRemover.Builder() ;
//				final double ccc = 0.03 ;
//				builder.setSameLocationPenalty( ccc ) ;
//				builder.setSameActivityTypePenalty( ccc ) ;
//				builder.setSameActivityEndTimePenalty( ccc ) ;
//				builder.setSameModePenalty( ccc ) ;
//				builder.setSameRoutePenalty( ccc ) ;
////				builder.setStageActivityTypes( tripRouter.getStageActivityTypes() ) ;
//				this.bindPlanSelectorForRemoval().toProvider( builder ) ;
//			}
//		} );
		
		overridingModules.add( new KaiAnalysisListener.Module() );
		
		berlin.prepareControler( overridingModules.toArray( new AbstractModule[0] ) );
		
		berlin.run() ;
	}
	
} */