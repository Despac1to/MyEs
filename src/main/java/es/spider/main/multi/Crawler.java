package es.spider.main.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

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
					SongInfo songInfo = crawlSongById(song);
					if(songInfo != null){
						songInfos.add(songInfo);
					}
					// 记录
					SongQueueUtil.addCrawledSong(song);
					logger.info("total: " + SongQueueUtil.getCrawledSongConut() + "," + Thread.currentThread().getName());
				}
			}
		}
		List<SongInfoModel> songInfosModels = genSongInfoModels(songInfos);
		countDownLatch.countDown();
		return songInfosModels;
	}

	private List<SongInfoModel> genSongInfoModels(List<SongInfo> songInfos) {
		List<SongInfoModel> songInfoModels = new ArrayList<>();
		for (SongInfo si : songInfos) {
			List<SongComment> songComments = si.getSongComments();
			for (SongComment sc : songComments) {
				SongInfoModel sim = new SongInfoModel();
				sim.setSong(si.getSiTitle().split("-")[0].trim());
				sim.setSinger(si.getSiTitle().split("-")[1].trim());
				sim.setCommentUser(sc.getScNickName());
				sim.setCommentType(sc.getScType());
				sim.setCommentTime(sc.getScCommentTime());
				// 过滤emoji表情
				String content = sc.getScContent().replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "");
				if (content.length() == 0) {
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
			return null;
		}
	}
}
