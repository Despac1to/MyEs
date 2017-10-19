package es.spider.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SongQueueUtil {

	private static BlockingQueue<String> songListQueue = new ArrayBlockingQueue<>(2000);
	private static BlockingQueue<String> uncrawledSongQueue = new ArrayBlockingQueue<>(2000); // songs that haven't been crawled
	private static Set<String> crawledSongQueue = new HashSet<>(400000); // songs that have been crawled
	
	// songList operation
	public static BlockingQueue<String> getSongListQueue(){
		return songListQueue;
	}
	
	public static void addSongList(String songList) throws InterruptedException{
		// 超时3秒
		songListQueue.offer(songList, 3, TimeUnit.SECONDS);
	}
	
	public static String take() throws InterruptedException{
		String songList = null;
		if(!isSongListQueueEmpty()){
			songList = songListQueue.take();
		}
		return songList;
	}
	
	public static boolean isSongListQueueEmpty(){
		return songListQueue.size() == 0;
	}
	
	public static void addUncrawledSong(String song) throws InterruptedException{
		// 超时3秒
		uncrawledSongQueue.offer(song, 3, TimeUnit.SECONDS);
	}
	
	public static String takeUncrawledSong() throws InterruptedException{
		if(!isUncrawledSongQueueEmpty()){
			return uncrawledSongQueue.take();
		}
		return null;
	}
	
	
	
	public static void addCrawledSong(String song) throws InterruptedException{
		crawledSongQueue.add(song);
	}
	
	public static boolean isSongCrawled(String song){
		return crawledSongQueue.contains(song);
	}
	
	public static boolean isUncrawledSongQueueEmpty(){
		return uncrawledSongQueue.size() == 0;
	}
	
	public static int getCrawledSongConut(){
		return crawledSongQueue.size();
	}

	public static BlockingQueue<String> getUncrawledSongQueue() {
		return uncrawledSongQueue;
	}
}
