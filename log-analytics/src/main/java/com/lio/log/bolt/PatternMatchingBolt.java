package com.lio.log.bolt;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lio.log.db.DerbyDBUtil;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

/**
 * PatternMatchingBolt.java
 * Purpose: Matches the error,Exception Patterns if any and then store them into DB as exception messages that will be notified later when the end of line is reached by the LogParserBolt. 
 * */
public class PatternMatchingBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(PatternMatchingBolt.class);
	private OutputCollector collector;
	
	
	private final String dbErrorExp = "^.*[0-9,:]\\s+Connection not available\\s+[a-z,\\s,A-Z]+jdbc\\/LEDataSource.";
	private final String runtimeExceptionExp = "^.[0-9.\\/,\\s,:,A-Z,]+\\]\\s+[A-Z,0-9,a-z]+\\s[A-Z,a-z]+\\s+[A-Z,a-z]+\\s+The RuntimeException could not be mapped to a response, re-throwing to the HTTP container";
	private final String classLoadingErrorExp = "";

	public void execute(Tuple input) {

		String server = (String) input.getValue(0);
		String node = (String) input.getValue(1);
		String line = (String) input.getValue(2);
		//System.out.println("Parser olt:"+line);
		
		
		Pattern dbErrorPattern = Pattern.compile(dbErrorExp);
		Pattern runtimeExpPattern = Pattern.compile(runtimeExceptionExp);

		Matcher dbMatcher = dbErrorPattern.matcher(line);
		Matcher runtimeMatcher = runtimeExpPattern.matcher(line);
		// System.out.println(line);
		if (dbMatcher.find()) {
			try {
				DerbyDBUtil util = new DerbyDBUtil(); 
				util.addExceptionMessage(server,node,line.substring(line.indexOf("["),line.indexOf("]")+1),line,true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("DB error has been found.  Line : " + line);
		}

		else if (runtimeMatcher.find()) {
			try {
				DerbyDBUtil util = new DerbyDBUtil(); 
				System.out.println("Runtime error found. Storing to DB"+line);
				util.addExceptionMessage(server,node,line.substring(line.indexOf("["),line.indexOf("]")+1),line,true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.debug("Runtime error has occured. Line : " + line);
		}
		collector.ack(input);
	}

	public void prepare(Map arg0, TopologyContext arg1, OutputCollector oc) {
		this.collector = oc;

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("server", "node", "line"));


	}

}
