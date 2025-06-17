package alzlaz.hearthstone.watcher;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.nio.file.WatchEvent;


import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractFolderWatcher{
	
    private final WatchService watchService;
    protected final Map<WatchKey, Path> keys;
    
    public AbstractFolderWatcher(Path dir) throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();
        registerAll(dir);
    }
    
    //https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
    private void register(Path path) throws IOException {
    	WatchKey key = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    	keys.put(key, path);
    }
    
    protected void registerAll(Path path) throws IOException {
    	register(path);
    	try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
    		for (Path entry: stream) {
    			if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
    				register(entry);
    			}
    		}
    	}
    }


    
    public void runFolderWatcher() {
    	
		while (true) {
            WatchKey key;
            try {
                // Wait for a key to be available
                key = watchService.take();
            } catch (InterruptedException ex) {
                System.out.println("Directory watching interrupted");
                return;
            }
            
            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!");
                continue;
            }

            // Loop through the events
            for (WatchEvent<?> event : key.pollEvents()) {
             
                WatchEvent<Path> ev = cast(event);
                Path fileName = (Path) ev.context();
                Path child = dir.resolve(fileName);

                handleStandardWatchEventKinds(event, child, key);
            }

            // Reset the key to receive further watch events
            boolean valid = key.reset();
            if (!valid) {
            	keys.remove(key);
                System.out.println("WatchKey no longer valid");
                
                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
    
    public abstract void handleStandardWatchEventKinds(WatchEvent<?> event, Path child, WatchKey key);

}