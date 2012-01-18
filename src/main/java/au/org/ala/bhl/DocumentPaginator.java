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
package au.org.ala.bhl;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class that converts verbatim OCR XML file into seperate pages of OCR text.
 * 
 * It parses the XML using an XMLStreamReader, so overheads are kept to a minimum.
 * 
 * @author baird
 *
 */
public class DocumentPaginator {
    
    public void paginateText(String text, PageHandler handler) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {            
            XMLStreamReader parser = factory.createXMLStreamReader(new StringReader(text));
            paginateImpl(parser, handler);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to paginate text" , ex);
        }       
    }
    
    public void paginate(Reader reader, PageHandler handler) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {            
            XMLStreamReader parser = factory.createXMLStreamReader(reader);
            paginateImpl(parser, handler);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to paginate from reader" , ex);
        }
        
    }

    public void paginate(String filename, PageHandler handler) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            File f = new File(filename);            
            if (f.length() == 0) {
                // empty file, can't paginate...
               return; 
            }            
            XMLStreamReader parser = factory.createXMLStreamReader(new FileInputStream(f), "cp1252");
            paginateImpl(parser, handler);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to paginate " + filename, ex);
        }

    }
    
    private void paginateImpl(XMLStreamReader parser, PageHandler handler) throws Exception {
        
        if (parser == null) {
            return;
        }
        
        StringBuilder buffer = new StringBuilder();

        String currentPage = null;

        while (true) {
            int event = parser.next();
            if (event == XMLStreamConstants.END_DOCUMENT) {
                parser.close();
                break;
            }

            if (event == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals("PARAM")) {
                    String attrName = parser.getAttributeValue("", "name");
                    if (attrName.equals("PAGE")) {

                        if (!StringUtils.isEmpty(currentPage)) {
                            if (handler != null) {
                                handler.handlePage(currentPage, buffer.toString());
                            }
                        }

                        buffer = new StringBuilder();
                        currentPage = parser.getAttributeValue("", "value");
                    }
                }
            }

            if (event == XMLStreamConstants.CHARACTERS) {
                String value = StringUtils.trim(parser.getText());
                if (!StringUtils.isEmpty(value)) {
                    buffer.append(value).append(" ");
                }
            }
        }
        
    }

}
