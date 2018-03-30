package com.pp.folderwatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Incorrect Arguments!!!!");
			System.out.println("Please provide path for temp , source and archive folder..");
			System.out.println("java -jar folderwatcher <temp-foler-path> <source-folder-path> <archive-folder-path>");
			return;
		}

		if (Files.notExists(Paths.get(args[0]))) {
			System.out.println("Temp folder doesn't exist.. Please verify path provided");
			return;
		}

		if (Files.notExists(Paths.get(args[1]))) {
			System.out.println("Source folder doesn't exist.. Creating folder now");
			try {
				Files.createDirectory(Paths.get(args[1]));
			} catch (IOException e) {
				System.err.println("Error occured while creating Source Folder.");
				return;
			}
		}

		if (Files.notExists(Paths.get(args[2]))) {
			System.out.println("Archive folder doesn't exist.. Creating folder now");
			try {
				Files.createDirectory(Paths.get(args[2]));
			} catch (IOException e) {
				System.err.println("Error occured while creating Archive Folder.");
				return;
			}
		}

		/**
		 * Copy files from temp to source folder
		 */
		try {
			FileUtils.copyDirectory(new File(args[0]), new File(args[1]));
		} catch (IOException e) {
			System.err.println("Error While Copying files from temp to source folder");
			return;
		}

		/**
		 * Start Scheduling
		 */
		TaskExecutor task = new TaskExecutor();
		task.initialize().setData(args[1], args[2]);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
	}

}
