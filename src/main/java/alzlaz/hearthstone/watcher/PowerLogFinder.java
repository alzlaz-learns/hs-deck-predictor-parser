package alzlaz.hearthstone.watcher;


import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alzlaz.hearthstone.GameObjects.GameInfo;
import alzlaz.hearthstone.LogReader.HandleJson;
import alzlaz.hearthstone.LogReader.LineParser;
import alzlaz.hearthstone.LogReader.StandardLogReader;

import static java.nio.file.StandardWatchEventKinds.*;

public class PowerLogFinder extends AbstractFolderWatcher implements Runnable {


	private static final Pattern DATE_PATTERN = Pattern.compile("Hearthstone_(\\d{4})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})_(\\d{2})");
	private Path mostRecentFolder;
	private final LineParser lineParser;


	public Map<Path, Long> lastModifiedTimestamps;
	private final Set<Path> activeLogFiles = Collections.synchronizedSet(new HashSet<>());
    private final AtomicBoolean running = new AtomicBoolean(true);
	

	public PowerLogFinder(Path dir, LineParser parser) throws IOException {
		super(dir);

		this.lineParser = parser;


		this.lastModifiedTimestamps = new HashMap<>();
		setInitialMostRecentFolder();
		if (mostRecentFolder != null) {
			// Resolve the path to power.log within the most recent folder
			Path powerLogPath = mostRecentFolder.resolve("power.log");

			// Check if power.log exists and the parsing thread is not running
			if (Files.exists(powerLogPath)) {
				System.out.println("power.log exists in the most recent folder. Starting parsing thread.");
                startLogParsing(powerLogPath);
				
			} else {
				System.out.println("power.log does not exist in the most recent folder.");
			}
		} else {
			System.out.println("No most recent folder found. Waiting for power.log to be created.");
		}

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false);
            System.out.println("Shutting down PowerLogFinder...");
        }));
	}

    private void startLogParsing(Path path) {
        if (!activeLogFiles.contains(path)) {
            activeLogFiles.add(path);
            new Thread(() -> {
                try {
                    System.out.println("Starting to parse: " + path);
                    StandardLogReader reader = new StandardLogReader(lineParser);
                    reader.readLog(path.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    activeLogFiles.remove(path);
                }
            }).start();
        }
    }

	public boolean creationDateCheck(Path folderName) {

		Matcher matcher = DATE_PATTERN.matcher(folderName.toString());
		return matcher.matches();
	}

	protected void setInitialMostRecentFolder() {
        PathSort pathSort = new PathSort();
		for (Path val : keys.values()) {
			if (creationDateCheck(val.getFileName())) {
				if (mostRecentFolder == null || pathSort.compare(val, mostRecentFolder) > 0) {
					mostRecentFolder = val;
				}
			}
		}
		if (mostRecentFolder != null) {
			System.out.format("Initial most recent folder set to: %s%n", mostRecentFolder);
		}
	}

	@Override
	public void handleStandardWatchEventKinds(WatchEvent<?> event, Path child, WatchKey key) {
		// TODO Auto-generated method stub
		WatchEvent.Kind<?> kind = event.kind();
		//
		String filePath = child.toAbsolutePath().toString();
		if(kind == OVERFLOW) {
			System.err.println("Overflow");
			return;
		}
		
		if(kind == ENTRY_CREATE) {
			if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS) && creationDateCheck(child.getFileName())) {
				// System.out.format("New Folder detected: %s"+ System.getProperty("line.separator"), child.getFileName().toString());
					
				setInitialMostRecentFolder();
				try {
					registerAll(child);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS) &&
					child.getFileName().toString().equalsIgnoreCase("power.log")) {
				// System.out.format("New power.log detected: %s" + System.getProperty("line.separator"), child.getFileName().toString());
				// System.out.println(filePath);
				startLogParsing(child);
			}
			} else if (kind == ENTRY_DELETE) {
			if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
				// System.out.format("Removing watch for directory: %s%n", child);
				keys.remove(key);
			} else if (child.getFileName().toString().equalsIgnoreCase("power.log")) {
				// System.out.format("power.log file deleted: %s%n", child);
			}
			// System.out.format("Entry deleted: %s%n", child.getFileName().toString());
			} else if (kind == ENTRY_MODIFY) {
			if (child.getFileName().toString().equalsIgnoreCase("power.log")) {
				// System.out.format("power.log file modified: %s", child);
			}

		}
	}

    @Override
    public void run() {
        while (running.get()) {
            runFolderWatcher();
        }
    }

    public void stop() {
        running.set(false);
    }

	class PathSort implements Comparator<Path> {
		@Override
		public int compare(Path o1, Path o2) {
			LocalDateTime date1 = extractDateTime(o1);
			LocalDateTime date2 = extractDateTime(o2);
			return date1.compareTo(date2);
		}

		private LocalDateTime extractDateTime(Path name) {
			Matcher matcher = DATE_PATTERN.matcher(name.toString());
			if (matcher.find()) {
				int year = Integer.parseInt(matcher.group(1));
				int month = Integer.parseInt(matcher.group(2));
				int day = Integer.parseInt(matcher.group(3));
				int hour = Integer.parseInt(matcher.group(4));
				int minute = Integer.parseInt(matcher.group(5));
				int second = Integer.parseInt(matcher.group(6));
				return LocalDateTime.of(year, month, day, hour, minute, second);
			}
			return LocalDateTime.MIN; // Return the minimum value if parsing fails
		}
	}

  public static void main(String[] args) throws IOException {
        
        Path folder = Paths.get("C:\\Program Files (x86)\\Hearthstone\\Logs");
		HandleJson cardLookup = new HandleJson();
		GameInfo gameInfo = new GameInfo();
		LineParser lineParser = new LineParser(cardLookup, gameInfo);



        PowerLogFinder folderWatcher = new PowerLogFinder(folder, lineParser);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(folderWatcher);

        // Optional: Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered.");
            folderWatcher.stop(); // Stop the watcher loop
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }));
	}
}

