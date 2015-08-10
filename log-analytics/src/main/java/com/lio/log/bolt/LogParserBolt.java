package com.lio.log.bolt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lio.log.db.DerbyDBUtil;
import com.lio.service.EmailService;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
/**
 * LogParserBolt.java
 * Purpose: Parse each line for time recorded for each line, ignores the html tags,
 * Processing: Bolt is initialized with the last log time recorded (x) in the database. 
 * 			   As the spout emits the line,
 * 				2. Checks if the line is last, if yes, Check the time of this log and if it is > 0 which means it is a html start tag or end tag. not a proper log line.
 * 						 In this case, Store the previous log line date as the last log time
 * 				3. Otherwise, Record the time and store it in the DB that it was the last log time (x). 
 * 						And then check the DB if there are any Exception messages that need to be notified to the user. 
 * 			 	 4.   This bolt checks if the line date (y) > (x) which means the line on the log file is the new line and needs processing. 
 * 			   
 * */
public class LogParserBolt extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OutputCollector collector;
	private static final String dateMatcherExp = "^\\[[0-9,\\/+]+\\s+[0-9,:]+\\s[A-Z]+\\]";

	//private long lastLogTime = 1437500486720l;
	
	private static final Logger logger = LoggerFactory
			.getLogger(LogParserBolt.class);
	

	private long lastLogTime;
	
	public LogParserBolt(String server,String node){
		try {
			lastLogTime = new DerbyDBUtil().getLastLogTime(server,node);
			System.out.println("Last log time recorded: "+lastLogTime);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cound not get Last log time Exiting Program");
			System.exit(0);
		}
	}
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		System.out.println("Prepare Method called");
		this.collector = collector;

	}

	public void execute(Tuple input) {
		
		String server = (String) input.getValue(0);
		String node = (String) input.getValue(1);
		String line = (String) input.getValue(2);
		Boolean isLastLine = (Boolean) input.getValue(3);
		if(isLastLine){
			//System.out.println("Reporting last line"+line);
			try {
				// Last line has reached but the line may not be having proper date so it will return 0.
				if(getTimeFromLine(line) > 0){
					logger.info("Last line reached. Updating the last log time : "+getTimeFromLine(line)+",Last log time:"+lastLogTime);
					
					lastLogTime = getTimeFromLine(line);
					new DerbyDBUtil().updateLastLogTime(server,node,getTimeFromLine(line),line);
					
					
				}
				// Even though it returns 0, still the line is last one so update the lastLogTime to whatever was recorded previously at line 114.
				else{
					logger.info("Last line reached. Updating the last log time : "+lastLogTime);
					new DerbyDBUtil().updateLastLogTime(server,node,lastLogTime,line);
				}
				
				StringBuffer buffer = new DerbyDBUtil().getExceptionMessages(server,node);
				
				if(buffer.toString().trim().length()>0){
					System.out.println(buffer);
					logger.info("Sending Notification to User that the error has occured");
					new EmailService().sendEmail("Error logged on the server "+server, buffer.toString());
					new DerbyDBUtil().deleteMessagesAfterNotified(server,node);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			collector.ack(input);
			return;
		}
		// If it is not the last line, chechk if the time of log is greater than previou log // Sometimes storm sends lines twice. 
		// If it is greater, update the lastLogTime to the current one. 
		else if ( getTimeFromLine(line) > lastLogTime) {
			//System.out.println("Printing times getTime >lastLogTime"+getTimeFromLine(line)+","+lastLogTime);
			lastLogTime = getTimeFromLine(line);
			collector.emit(new Values(server, node, line));
		}

		collector.ack(input);
	}
	
	public long getTimeFromLine(String line){
		Pattern pattern = Pattern
				.compile(dateMatcherExp);
		Matcher matcher = pattern.matcher(line);
		long time = 0l;
		if (matcher.find()) {
			String dateString = line.substring(line.indexOf("[") + 1,
					line.indexOf("]") - 4);
			SimpleDateFormat outFormat = new SimpleDateFormat(
					"MM/dd/yy HH:mm:ss:SSS");
			Date d = null;
			try {
				d = outFormat.parse(dateString);
				time = d.getTime();
			} catch (ParseException e) {
				logger.error("Could not parse the Date Pattern Matching error");
			}
		}
		return time;
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("server", "node", "line"));

	}
}
