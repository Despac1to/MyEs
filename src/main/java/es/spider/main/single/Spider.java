package es.spider.main.single;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.spider.util.SongUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.spider.dao.SongInfoDao;
import es.spider.model.Constants;
import es.spider.model.SongComment;
import es.spider.model.SongInfo;
import es.spider.model.SongInfoModel;
import es.spider.util.HtmlFetcher;
import es.spider.util.HtmlParser;
import es.spider.util.SongQueueUtil;

public class Spider implements Runnable {

	private static final Log logger = LogFactory.getLog(Spider.class);
	private SongInfoDao songInfoDao;
	private String songListPrefix = Constants.SONG_LIST_PREFIX;
	private int totalSongList = 100;
	private int limit = Constants.PAGE_SIZE;
	private int offSet = Constants.OFF_SET;
	private final List<SongInfo> songInfos = new ArrayList<>();

	@Override
	public void run() {
		try {
			logger.info("Spider start ...");
			int count = 0;
			// 初始化待爬取歌单
			SongUtil.initSongList(totalSongList, limit, offSet, songListPrefix);
			while (!SongQueueUtil.isSongListQueueEmpty()) {
				// 填充待爬取歌曲队列
				fillSongQueue(SongQueueUtil.take());
				logger.info("====>Crwal SongList: " + SongQueueUtil.take());
				while (!SongQueueUtil.isUncrawledSongQueueEmpty()) {
					String songId = SongQueueUtil.takeUncrawledSong();
					if (!SongQueueUtil.isSongCrawled(songId)) {
						logger.info("Crwal SongId: " + songId);
						SongInfo songInfo = SongUtil.crawlSongById(songId);
						if(songInfo != null){
							songInfos.add(songInfo);
							if(songInfos.size() % 20 == 0){
								songInfoDao.saveSongInfoModels(SongUtil.genSongInfoModels(songInfos));
								songInfos.clear();
								logger.info("Insert songInfo: 20");
							}
						}
						// 只爬取一次
						SongQueueUtil.addCrawledSong(songId);
						count ++;
					}
				}
			}
			// 如果剩余
			if(songInfos.size() > 0){
				songInfoDao.saveSongInfoModels(SongUtil.genSongInfoModels(songInfos));
				logger.info("Insert songInfo: " + songInfos.size());
			}
			logger.info("Crwal end: " + count);
		} catch (Exception e) {
			logger.error("Generate Song Info Error!", e);
		}
	}

	private void fillSongQueue(String url) {
		if (StringUtils.isNotBlank(url)) {
			try {
				HtmlParser.putSong(url);
			} catch (IOException | InterruptedException e) {
				
			}
		}
	}

	public void setSongInfoDao(SongInfoDao songInfoDao) {
		this.songInfoDao = songInfoDao;
	}

	@Override
	public String toString() {
		return "Spider [songListPrefix=" + songListPrefix + ", limit=" + limit + ", offSet=" + offSet + ", songInfos="
				+ songInfos + "]";
	}

}
