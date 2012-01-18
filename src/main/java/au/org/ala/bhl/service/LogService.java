/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl.service;

/**
 * Static service for logging messages. Should be fleshed out to use log4j or some other framework. For the moment 
 * dumps messages to std out
 * 
 * @author baird
 *
 */
public class LogService {

	
    public static void log(Class<?> source, String format, Object ... args) {        
        String message = (args.length == 0 ? format : String.format(format, args));       
        System.out.println(String.format("[%s-%s] %s", source.getSimpleName(), Thread.currentThread().getName(), message));        
    }

}
