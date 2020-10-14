/*
 *
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2012 Mihail V. Beloshapkin
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

package dirsyncpro.sync;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class RealtimeListener {
	
    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private RealtimeChangeListener mListener = new RealtimeChangeListener() {
		@Override
		public void change(WatchEvent<Path> e) {
		}
	};
	
	public RealtimeListener(String dir) throws IOException
	{
	    this.watcher = FileSystems.getDefault().newWatchService();
	    this.keys = new HashMap<WatchKey,Path>();
	    
	    registerAll(Paths.get(dir));
	}
	
    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

	
    /**
     * Process all events for keys queued to the watcher
     */
    private Thread t = null;
    public void stop(){
    	if (t != null && t.isAlive() && !t.isInterrupted())
    		t.interrupt();
    	
    	//close the watcher to release resource
    	try {
			watcher.close();
		} catch (IOException e) {
		}
    }
    
    public void start() {
    	t  = new Thread(new Runnable() {
    		@Override
    		public void run() {
    			startInternal();
    		}
    	});
    	t.start();
    }
    public void startInternal() {
        for (;;) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            } catch (ClosedWatchServiceException e) {
            	//if watcher was closed in stop() method
				return;
			}

            Path dir = keys.get(key);
            if (dir == null) {
                //WatchKey not recognized!!
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                //System.out.format("%s: %s\n", event.kind().name(), child);
                mListener.change(ev);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        //if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        if (Files.isDirectory(child)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    public static interface RealtimeChangeListener
    {
    	public void change(WatchEvent<Path> e);
    }
    
    public void setChangeListener(RealtimeChangeListener l)
    {
    	mListener = l;
    }
    
    public RealtimeChangeListener getChangeListener()
    {
    	return mListener;
    }
    
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
}
