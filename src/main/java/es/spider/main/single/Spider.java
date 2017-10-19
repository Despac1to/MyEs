package es.spider.main.single;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			initSongList();
			while (!SongQueueUtil.isSongListQueueEmpty()) {
				// 填充待爬取歌曲队列
				fillSongQueue(SongQueueUtil.take());
				logger.info("====>Crwal SongList: " + SongQueueUtil.take());
				while (!SongQueueUtil.isUncrawledSongQueueEmpty()) {
					String songId = SongQueueUtil.takeUncrawledSong();
					if (!SongQueueUtil.isSongCrawled(songId)) {
						logger.info("Crwal SongId: " + songId);
						SongInfo songInfo = crawlSongById(songId);
						if(songInfo != null){
							songInfos.add(songInfo);
							if(songInfos.size() % 20 == 0){
								songInfoDao.saveSongInfoModels(genSongInfoModels(songInfos));
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
				songInfoDao.saveSongInfoModels(genSongInfoModels(songInfos));
				logger.info("Insert songInfo: " + songInfos.size());
			}
			logger.info("Crwal end: " + count);
		} catch (Exception e) {
			logger.error("Generate Song Info Error!", e);
		}
	}

	private List<SongInfoModel> genSongInfoModels(List<SongInfo> songInfos) {
		List<SongInfoModel> songInfoModels = new ArrayList<>();
		for(SongInfo si : songInfos){
			List<SongComment> songComments = si.getSongComments();
			for(SongComment sc : songComments){
				SongInfoModel sim = new SongInfoModel();
				sim.setSong(si.getSiTitle().split("-")[0].trim());
				sim.setSinger(si.getSiTitle().split("-")[1].trim());
				sim.setCommentUser(sc.getScNickName());
				sim.setCommentType(sc.getScType());
				sim.setCommentTime(sc.getScCommentTime());
				// 过滤emoji表情
				String content = sc.getScContent().replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
				if(content.length() == 0){
					continue;
				}
				sim.setCommentContent(content);
				sim.setCommentAppreciation(Integer.valueOf(sc.getScAppreciation()));
				songInfoModels.add(sim);
			}
		}
		return songInfoModels;
	}

	// 当限制爬取时,sleep一段时间
	private SongInfo crawlSongById(String songId) {
		try {
			SongInfo si = HtmlParser.getSongInfoById(songId);
			if (si == null) {
				logger.info("Interceptted by music.163.com server..");
				Thread.sleep((long) (Math.random() * 15000));

				// 递归
				return crawlSongById(songId);
			} else {
				return si;
			}
		} catch (Exception e) {
			logger.info("Refused by music.163.com server..");
			return crawlSongById(songId);
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

	private void initSongList() throws InterruptedException, IOException {
		if (totalSongList > limit) {
			int tmpLimit = limit;
			int tmpOffset = offSet;
			while (totalSongList > tmpOffset) {
				String suffix = "limit=" + tmpLimit + "&offset=" + tmpOffset;
				tmpOffset += tmpLimit;

				if (tmpOffset + tmpLimit > totalSongList) {
					tmpLimit = totalSongList - tmpOffset;
				}
				HtmlParser.putSongList(HtmlFetcher.fetchUrl(songListPrefix + suffix));
			}
		} else {
			String suffix = "limit=" + totalSongList + "&offset=" + offSet;
			HtmlParser.putSongList(HtmlFetcher.fetchUrl(songListPrefix + suffix));
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
