package com.vysk.armcctoolchain;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ARMCCErrorParser implements IErrorParser {

	private static class RegexParser {

		static class RegexParserMatch {
	        public final String fileName;
	        public final int lineNumber;
	        public final String message;
	        public final int severity;
	        
	        RegexParserMatch(String fileName, int lineNumber, String message, int severity) {
	        	this.fileName = fileName;
	        	this.lineNumber = lineNumber;
	        	this.message = message;
	        	this.severity = severity;
	        }
		}
		
		private final Pattern parserPattern;
		private final int severity;
		
		RegexParser(String expression, int severity) {
			this.parserPattern = Pattern.compile(expression);
			this.severity = severity;
		}
		
		RegexParserMatch getMatch(String line) {
			Matcher matcher = parserPattern.matcher(line);
			
			if (matcher.matches()) {
		        String fileName = matcher.group(1);
		        int lineNumber  = Integer.parseInt(matcher.group(2));
		        String message  = matcher.group(3);
		        return new RegexParserMatch(fileName, lineNumber, message, severity);
			}
			return null;
		}
		
	}
	
    private static RegexParser[] parsers = {
    	new RegexParser("\"(.*)\".*line\\s*(\\d*).*Error:\\s*(.*)", IMarkerGenerator.SEVERITY_ERROR_RESOURCE),
    	new RegexParser("\"(.*)\".*line\\s*(\\d*).*Warning:\\s*(.*)", IMarkerGenerator.SEVERITY_WARNING),
    	new RegexParser("\"(.*)\".*line\\s*(\\d*).*Info:\\s*(.*)", IMarkerGenerator.SEVERITY_INFO)
    };

	@Override
	public boolean processLine(String line, ErrorParserManager eoParser) {
		
		for (RegexParser parser : parsers) {
			RegexParser.RegexParserMatch match = parser.getMatch(line);
			if (match != null) {
		        IResource resource = eoParser.findFileName(match.fileName);
		        if(resource == null)
		            resource = eoParser.getProject(); // file not found in workspace, attach problem to project

		        // create a problem marker that will show up in the problems view
		        eoParser.generateMarker(resource, match.lineNumber, match.message, match.severity, null);
		        return true;
			}
		}
		
    	return false;
	}	
	
}
