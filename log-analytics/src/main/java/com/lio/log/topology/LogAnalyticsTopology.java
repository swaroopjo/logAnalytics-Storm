package com.lio.log.topology;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lio.log.bolt.LogParserBolt;
import com.lio.log.bolt.PatternMatchingBolt;
import com.lio.log.spout.LogGrabberSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * LogAnalyticsTopology.java
 * Purpose: Initialize and Start topologies based on the command line arguments. 
 * CLI: servers=termspmr04.mayo.edu:9083-WebServiceREF1_pmr04,termspmr01.mayo.edu:9082-WebServiceREF1_pmr01,termspmr04.mayo.edu:9083-WebServiceREF2_pmr04,termspmr01.mayo.edu:9082-WebServiceREF2_pmr01
 * Each topology consists of one spout and two bolts. 
 * Spout: calls the Server for log file using http, read each line from the file and emit it to the LogParserBolt
 * LogParserBolt: parses each line, emit it to the PatternMatchingBolt.
 * PatternMatchingBolt parses the line for any errors or exceptions. 
 * 
 * */
public class LogAnalyticsTopology {

	private static final Logger logger = LoggerFactory
			.getLogger(LogAnalyticsTopology.class);
	
	public static void main(String[] args){
		
		TopologyBuilder builder = new TopologyBuilder();
		for(String arg : args){
			if(arg.contains("=")){
				if(arg.subSequence(0,arg.indexOf("=")).equals("servers")){
					
					String[] servers = arg.substring(arg.indexOf('=') + 1).split(",");
					for(String server: servers){
						
						String[] srvJvm = server.trim().split("-");
						String host = srvJvm[0];
						String jvm = srvJvm[1];
						logger.info("Building Topology...:"+ host.split(":")[0].substring(5,10)+host.split(":")[1]+" "+jvm);
				        builder.setSpout( host.split(":")[0].substring(5,10)+host.split(":")[1]+jvm+"logAnalyticsSpout", new LogGrabberSpout(host, jvm) );
				        builder.setBolt( host.split(":")[0].substring(5,10)+host.split(":")[1]+jvm+"logParserBolt",new LogParserBolt(host,jvm)).shuffleGrouping(host.split(":")[0].substring(5,10)+host.split(":")[1]+jvm+"logAnalyticsSpout");
				        builder.setBolt(host.split(":")[0].substring(5,10)+host.split(":")[1]+jvm+"patternMatcherBolt", new PatternMatchingBolt())
				                .shuffleGrouping(host.split(":")[0].substring(5,10)+host.split(":")[1]+jvm+"logParserBolt");
				        
				 	}
				}
			}
		}
		Config conf = new Config();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());
		
	        //Utils.sleep(12000);
	        //cluster.killTopology("test");
	       // cluster.shutdown();
	}
}
