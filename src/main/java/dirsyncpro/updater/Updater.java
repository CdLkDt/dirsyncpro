package dirsyncpro.updater;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.xml.sax.SAXException;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.exceptions.WarningException;

public class Updater {

	UpdateXmlReader uxmlr;
	boolean inBackground = true; 
	
	public Updater(boolean inB){
		inBackground = inB;
		String url = Const.UPDATEURL;
		String urlParams = "";
		try{
			urlParams += "version=" + URLEncoder.encode(Const.VERSION, "UTF-8");
		}catch (UnsupportedEncodingException e){
			DirSyncPro.getLog().printThrowableStack(e);
		}
		
		url += "?" + urlParams;

		try {
			uxmlr = new UpdateXmlReader(url, inBackground);
		}catch (SAXException e1){
			// TODO: remove this after a while when almost everybody is migrated to >= 1.53
			// Also change the update url in Const.java to point to https 
			// inserted: 06-12-2017
			try {
				url = url.replace("http", "https");
				uxmlr = new UpdateXmlReader(url, inBackground);
			}catch (SAXException e2){
				if (!inBackground) DirSyncPro.displayWarning("Unable to generate XML parser to read the update script!");
			}catch(WarningException e) {
				DirSyncPro.displayWarning(e.getMessage());
			}
		}catch (WarningException e) {
			DirSyncPro.displayWarning(e.getMessage());
		}
	}
	
	public boolean updateable(){
		return uxmlr.isUpdateable();
	}
	
	public void openDownloadURLinBrowser(){
		this.openBrowser(this.getUpdateURL());
	}
	
	public void openChangelogURLinBrowser(){
		this.openBrowser(this.getChangelogURL());
	}
	
	private void openBrowser(String url){
		try{
			Desktop.getDesktop().browse(new URI(url));
		}catch (URISyntaxException e){
			DirSyncPro.displayError("Update URL syntax error!");
		}catch (IOException e){
			DirSyncPro.displayError("Unable to start the default internet browser!");
		}
	}
	
	/**
	 * 
	 * @return <code>String</code> the URL from which the update is to be downloaded.
	 */
	public String getUpdateURL(){
		return uxmlr.getUpdateURL();
	}
	
	/**
	 * 
	 * @return <code>String</code> the new version which is available for download
	 */
	public String getNewVersion(){
		return uxmlr.getNewVersion();
	}
	
	/**
	 * 
	 * @return <code>String</code> the major changes in this update
	 */
	public String getChangelogURL(){
		String s = uxmlr.getChangeLogURL();
		return (s.equals("null") ? getUpdateURL() : s);
	}

	/**
	 * 
	 * @return <code>boolean</code> whether there is a new update available 
	 */
	public boolean isUpdateable(){
		return uxmlr.isUpdateable();
	}

	/**
	 * 
	 * @return <code>boolean</code> if contacting the update server has succeeded.
	 */
	public boolean contacted(){
		return uxmlr.contacted();
	}

}
