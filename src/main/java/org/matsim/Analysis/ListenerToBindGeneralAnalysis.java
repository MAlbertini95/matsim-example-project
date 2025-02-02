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

package org.matsim.Analysis;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import com.google.inject.Inject;

/**
 * Class to bind general analyze and writing tool to the simulation.
 * 
 * @author tthunig
 *
 */
public class ListenerToBindGeneralAnalysis implements IterationEndsListener {

	private static final Logger log = Logger.getLogger(ListenerToBindGeneralAnalysis.class);
	
	@Inject
	private Scenario scenario;
	
	@Inject
	private AnalyzedGeneralResultsWriter writer;

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		// write analyzed data
		writer.writeIterationResults(event.getIteration());
		runGnuplotScript("plot_iterationAnalysis", event.getIteration());
	
		// handle last iteration
		if (event.getIteration() == scenario.getConfig().controler().getLastIteration()) {
			// write spatial analysis
			writer.writeSpatialAnaylsis(event.getIteration());
			// close overall writing stream
			writer.closeAllStreams();
			// plot overall iteration results
			runGnuplotScript("plot_overallAnalysis", event.getIteration());
		}
	}
	
	/**
	 * starts the gnuplot script from the specific iteration directory
	 * 
	 * @param gnuplotScriptName
	 * @param iteration
	 */
	private void runGnuplotScript(String gnuplotScriptName, int iteration){
		String pathToSpecificAnalysisDir = scenario.getConfig().controler().getOutputDirectory() + "/ITERS/it." + iteration + "/analysis";		
		String relativePathToGnuplotScript = "../../../../../../../shared-svn/studies/tthunig/gnuplotScripts/" + gnuplotScriptName  + ".p";
		
		GnuplotUtils.runGnuplotScript(pathToSpecificAnalysisDir, relativePathToGnuplotScript);
//		log.info("execute command: cd " + pathToSpecificAnalysisDir);
//		log.info("and afterwards: gnuplot " + relativePathToGnuplotScript);
//		
//		try {
//			/*
//			 * start the command line ("cmd"),
//			 * execute the commands given by string and separated by "&" (or ";" on a linux machine, respectively),
//			 * terminate the command line afterwards ("/c").
//			 */
//			ProcessBuilder builder = new ProcessBuilder( "cmd", "/c", "cd", pathToSpecificAnalysisDir, "&", "gnuplot", relativePathToGnuplotScript);
//			Process p = builder.start();
//
//			// print command line infos and errors:
//			BufferedReader read = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String zeile;
//			while ((zeile = read.readLine()) != null) {
//				log.error("input stream: " + zeile);
//			}			
//			read = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			while ((zeile = read.readLine()) != null) {
//				log.error("error: " + zeile);
//			}
//		} catch (IOException e) {
//			log.error("ERROR while executing gnuplot command.");
//			e.printStackTrace();
//		}
	}

}