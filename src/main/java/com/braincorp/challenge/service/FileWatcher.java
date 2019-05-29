package com.braincorp.challenge.service;

import com.braincorp.challenge.model.UnixGroup;
import com.braincorp.challenge.model.UnixGroups;
import com.braincorp.challenge.model.UnixUser;
import com.braincorp.challenge.model.UnixUsers;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FileWatcher {
	private Thread thread;
	private WatchService watchService;

	public static void watchFile(Path file, Runnable callback) throws IOException {
		FileWatcher fileWatcher = new FileWatcher();
		fileWatcher.start(file, callback);
		Runtime.getRuntime().addShutdownHook(new Thread(fileWatcher::stop));
	}

	public void start(Path file, Runnable callback) throws IOException {
		watchService = FileSystems.getDefault().newWatchService();
		Path parent = file.getParent();
		parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
		thread = new Thread(() -> {
			while (true) {
				WatchKey wk = null;
				try {
					wk = watchService.take();
					Thread.sleep(500);
					for (WatchEvent<?> event : wk.pollEvents()) {
						Path changed = parent.resolve((Path) event.context());
						if (Files.exists(changed) && Files.isSameFile(changed, file)) {
							callback.run();
							break;
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				} catch (Exception e) {
				} finally {
					if (wk != null) {
						wk.reset();
					}
				}
			}
		});
		thread.start();
	}

	public void stop() {
		thread.interrupt();
		try {
			watchService.close();
		} catch (IOException e) {
		}
	}
}
