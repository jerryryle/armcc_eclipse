package com.vysk.armcctoolchain;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IResource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ARMCCErrorParser implements IErrorParser {

	static String errorExp = "\"(.*)\".*line\\s*(\\d*).*Error:\\s*(.*)";
	static String warningExp = "\"(.*)\".*line\\s*(\\d*).*Warning:\\s*(.*)";
	static String infoExp = "\"(.*)\".*line\\s*(\\d*).*Info:\\s*(.*)";
    static Pattern errorPattern = Pattern.compile(errorExp);
    static Pattern warningPattern = Pattern.compile(warningExp);
    static Pattern infoPattern = Pattern.compile(infoExp);

	@Override
	public boolean processLine(String line, ErrorParserManager eoParser) {
		Matcher matcher = errorPattern.matcher(line);
        int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
        if(!matcher.matches()) {
            severity = IMarkerGenerator.SEVERITY_WARNING;
    		matcher = warningPattern.matcher(line);
            if(!matcher.matches()) {
                severity = IMarkerGenerator.SEVERITY_INFO;
        		matcher = infoPattern.matcher(line);
                if(!matcher.matches()) {
                	return false;
                }
            }
        }

        String fileName = matcher.group(1);
        int lineNumber  = Integer.parseInt(matcher.group(2));
        String message  = matcher.group(3);

        IResource resource = eoParser.findFileName(fileName);
        if(resource == null)
            resource = eoParser.getProject(); // file not found in workspace, attach problem to project

        // create a problem marker that will show up in the problems view
        eoParser.generateMarker(resource, lineNumber, message, severity, null);
        return true;
	}	
	
}
