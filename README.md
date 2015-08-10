# logAnalytics-Storm
Analyse Log data for errors/excpetions using Storm 

Application monitors the log files in a cluster for any sort of errors/runtime exceptions and report them to the user using their email address. 

Topology: 

1. LogGrabberSpout makes a call to the logViewerApplication for Log file over http and emits the lines one by one to the LogParserBolt. 

2. LogParserBolt checks for the last updated logLine, ignores the html tags, and emits the line to the PatternMatchingBolt. 

3. PatternMatchingBolt matches the line with errors/exceptions and stores the line in the database. 

4. After the end of file is reached, The LogParserBolt checks the db if there are any excpeions to be notified and sends an email about it. 

Technologies: 
  Apache storm, Apache Derby, Java. 
  
Usage: 
  
  Run the InsertScript which eill create the tables (LastLogTable and LogExceptionTable). 
  
  LastLogTable : Used to store the last log line (timestamp of each server/node)
  
  LogExceptionTable behaves like a queue that stores the Exception information and is then flushed once the user is notified. 
  
Algorithm: 
  
  At the start of the application, 
  
  LogGrabberSpout and LogParserBolt is initalized with the hostname and jvm for each server/node.
  
  LogParserBolt keeps a record of the last log line(timestamp) for each node inorder to start parsing from there instead of parsing all the lines. 
  
  Spout emits each line till the end of line is reached. 
  
  bolt1 checks the line(timestamp) if the line is the latest one. otherwise ignores it. 
  
  If it is the new line, Send the line to the bolt2 for pattern matching. 
  
  If the end of line is reached, Spout sends a sticky bit and the line (that it is the last line).
  
  Bolt1 checks if it is the html tag and ignores it. Otherwise, Updates the LastlogTable with the timestamp of the last log line timestamp. 
    and checks the logExceptiontable if there are any exceptions to be notified. and then deletes them once user is notified. 
    
  
  
