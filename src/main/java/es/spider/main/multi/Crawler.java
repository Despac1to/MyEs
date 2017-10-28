package es.spider.main.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import es.spider.util.SongUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import es.spider.model.SongComment;
import es.spider.model.SongInfo;
import es.spider.model.SongInfoModel;
import es.spider.util.HtmlParser;
import es.spider.util.SongQueueUtil;

public class Crawler implements Callable<List<SongInfoModel>> {

	private Log logger = LogFactory.getLog(Crawler.class);
	private List<String> songs;
	private CountDownLatch countDownLatch;

	public Crawler() {
	}

	public Crawler(List<String> songs, CountDownLatch countDownLatch) {
		this.songs = songs;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public List<SongInfoModel> call() throws Exception {
		List<SongInfo> songInfos = new ArrayList<>();
		if (!CollectionUtils.isEmpty(songs)) {
			for (String song : songs) {
				if (!SongQueueUtil.isSongCrawled(song)) {
					logger.info("Crawl songId: " + song);
					SongInfo songInfo = SongUtil.crawlSongById(song);
					if(songInfo != null){
						songInfos.add(songInfo);
					}
					// 记录
					SongQueueUtil.addCrawledSong(song);
					logger.info("total: " + SongQueueUtil.getCrawledSongConut() + "," + Thread.currentThread().getName());
				}
			}
		}
		List<SongInfoModel> songInfosModels = SongUtil.genSongInfoModels(songInfos);
		countDownLatch.countDown();
		return songInfosModels;
	}
}
