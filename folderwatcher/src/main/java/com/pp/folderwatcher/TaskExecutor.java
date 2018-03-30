/**
 * 
 */
package com.pp.folderwatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class performs periodic operations to be executed
 * 
 * @author Pritam Prasad
 *
 */
public class TaskExecutor implements Runnable {

	private String sourceFolderPath;

	private String archiveFolderPath;

	private final long maxFolderSizeInBytes = 100 * 1024 * 1024;

	private Logger logger = null;

	public TaskExecutor initialize() {
		logger = Logger.getLogger("FolderWatcher");
		FileHandler fh;
		try {
			fh = new FileHandler("./OperationLogs.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			System.err.println("Logger Initialization failed with Security Exception");
		} catch (IOException e) {
			System.err.println("Logger Initialization failed with IOException");
		}
		return this;
	}

	public void setData(String _sourceFolderPath, String _archiveFolderPath) {
		this.sourceFolderPath = _sourceFolderPath;
		this.archiveFolderPath = _archiveFolderPath;
	}

	public void run() {
		System.out.println("Analyzing source folder for changes...");
		File sourceFolder = new File(sourceFolderPath);
		File[] allFiles = sourceFolder.listFiles();
		long sourceFolderSizeInBytes = 0;

		for (File file : allFiles) {
			/**
			 * Delete script files
			 */
			if (file.getName().contains(".bat") || file.getName().contains(".sh")) {
				try {
					Files.delete(file.toPath());
					logger.info("Deleted Script file : " + file.getName());
				} catch (IOException e) {
					System.err.println("Error while deleting script files..");
					return;
				}
			} else {
				sourceFolderSizeInBytes += file.length();
			}
		}
		logger.info("Current size of Source folder (in Bytes) : "+ sourceFolderSizeInBytes );

		if (sourceFolderSizeInBytes > maxFolderSizeInBytes) {
			long filesSizeToBeMoved = sourceFolderSizeInBytes - maxFolderSizeInBytes;
			Arrays.sort(allFiles, new Comparator<File>() {
				public int compare(File o1, File o2) {
					int i = 0;
					try {
						i = Long.compare(
								Files.readAttributes(o1.toPath(), BasicFileAttributes.class).creationTime().toMillis(),
								Files.readAttributes(o2.toPath(), BasicFileAttributes.class).creationTime().toMillis()
								);
					} catch (IOException e) {
						System.err.println("Error while getting creation time of file");
					}
					return i;
				}
			});

			for (File file : allFiles) {
				if (filesSizeToBeMoved > 0) {
					filesSizeToBeMoved -= file.length();
					try {
						Files.move(file.toPath(), Paths.get(archiveFolderPath, file.getName()));
						logger.info("Moved file : " + file.getName() + " to archived folder");
					} catch (IOException e) {
						System.err.println("Exception while Moving file to Archived folder");
						return;
					}
				} else {
					break;
				}
			}

		}

	}

}
