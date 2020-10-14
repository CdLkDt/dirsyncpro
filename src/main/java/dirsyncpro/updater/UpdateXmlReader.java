/*
 * XmlReader.java
 * 
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2002, 2003, 2005, 2006, 2008 F. Gerbig
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dirsyncpro.updater;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dirsyncpro.DirSyncPro;
import dirsyncpro.exceptions.WarningException;

/**
 * Reads a XML update url. The data can be retrieved via the various getter
 * methods.
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */
public class UpdateXmlReader extends DefaultHandler {

	private boolean updateable = false;
	private String newVersion = "";
	private String updateURL = "";
	private String changelogURL = "";
	private boolean contacted = false;
	boolean inBackground = true; 
	// changes is for backward compatibility for version <= 1.43.
	// not uses in version >= 1.44
	// can be deleted as of 01-01-2012
	private String changes = "";


	private static final String TAGRoot = "dirsyncproupdate";
	private static final String ATTRUpdateable = "updateable";
	private static final String ATTRVersion = "version";
	private static final String ATTRUrl = "url";
	private static final String ATTRChanges = "changes";
	private static final String ATTRChangelog = "changelog";
	
	/**
	 * Reads data from the given Update XML url. The data can be retreived via the
	 * various getter methods.
	 * 
	 * @param url
	 *            The url of the update XML to read the data from.
	 * @throws Exception
	 */
	public UpdateXmlReader(String url, boolean inB) throws WarningException, SAXException{
		inBackground = inB;
		SAXParserFactory factory = SAXParserFactory.newInstance();

		factory.setNamespaceAware(false);
		factory.setValidating(true);

		URLConnection urlConnection = null;
		try {
			URL updateURL = new URL (url);
			urlConnection = updateURL.openConnection();	
		}catch (IOException e){
			if (!inBackground) DirSyncPro.displayError("Unable set up the connection to the update server!");
		}
		
		try{
			SAXParser parser = factory.newSAXParser();
			parser.parse(urlConnection.getInputStream(), this);
			contacted = true;
		}catch (IOException e){
			if (!inBackground) throw new WarningException("Unable to contact the update server!");
		}catch (ParserConfigurationException e){
			if (!inBackground) throw new WarningException("Unable to parse the update XML!");
		}
	}

	/**
	 * SAX method called for every XML element start.
	 * 
	 * @param namespaceURI
	 *            The namespace of this XML element (if the parser is namespace
	 *            aware).
	 * @param localname
	 *            The localname of this XML element (if the parser is namespace
	 *            aware).
	 * @param qName
	 *            The name of this XML element (if the parser is NOT namespace
	 *            aware).
	 * @param atts
	 *            The XML elements attributes.
	 * @throws SAXException
	 */
	public void startElement(String namespaceURI, String localname,
			String qName, Attributes atts) throws SAXException {

		if (qName.equals(TAGRoot)) {

			updateable = Boolean.valueOf(atts.getValue(ATTRUpdateable)).booleanValue();
			newVersion = String.valueOf(atts.getValue(ATTRVersion));
			updateURL  = String.valueOf(atts.getValue(ATTRUrl));
			changes = String.valueOf(atts.getValue(ATTRChanges));
			changelogURL = String.valueOf(atts.getValue(ATTRChangelog));
		}
	}

	/**
	 * 
	 * @return <code>boolean</code> if contacting the update server has succeeded.
	 */
	public boolean contacted(){
		return contacted;
	}
	
	/**
	 * @return the updateable
	 */
	public boolean isUpdateable() {
		return updateable;
	}

	/**
	 * @return the newVersion
	 */
	public String getNewVersion() {
		return newVersion;
	}

	/**
	 * @return the changelog URL
	 */
	public String getChangeLogURL() {
		return changelogURL;
	}

	/**
	 * @return the updateURL
	 */
	public String getUpdateURL() {
		return updateURL;
	}
}