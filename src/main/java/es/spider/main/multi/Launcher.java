package es.spider.main.multi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import es.spider.dao.SongInfoDao;
import es.spider.model.Constants;
import es.spider.model.SongInfoModel;
import es.spider.util.HtmlFetcher;
import es.spider.util.HtmlParser;
import es.spider.util.SongQueueUtil;

public class Launcher {

	private Log logger = LogFactory.getLog(Launcher.class);
	private ExecutorService executorService;
	private SongInfoDao songInfoDao;
	private String songListPrefix = Constants.SONG_LIST_PREFIX;
	private int totalSongList = 100;
	private int limit = Constants.PAGE_SIZE;
	private int offSet = Constants.OFF_SET;
	private int threadNum = Constants.THREAD_NUM;

	public Launcher() {
		executorService = Executors.newFixedThreadPool(8);
	}

	public void launch() {
		try {
			initSongList();
			List<SongInfoModel> songInfoModels = new ArrayList<>();
			List<String> songs = new ArrayList<>();
			BlockingQueue<String> songListQueue = SongQueueUtil.getSongListQueue();
			if (!CollectionUtils.isEmpty(songListQueue)) {
				for (String songList : songListQueue) {
					songs.addAll(HtmlParser.genSong(songList));
				}
			}
			List<Future<List<SongInfoModel>>> futures = new ArrayList<>();
			CountDownLatch cdl = new CountDownLatch(threadNum);
			// 前6000首
			songs = songs.subList(0, 6000);
			int songsCnt = songs.size();
			int per = songsCnt % threadNum == 0 ? songsCnt / threadNum : songsCnt / threadNum + 1;
			for (int i = 0; i < threadNum; i++) {
				futures.add(executorService.submit(new Crawler(
						songs.subList(i * per, (i + 1) * per > songsCnt ? songsCnt : (i + 1) * per), cdl)));
				logger.info("execute task-" + i);
			}

			logger.info("cdl await...");
			// 等待线程执行完毕
			cdl.await();
			for (Future<List<SongInfoModel>> future : futures) {
				// 超时30秒
				List<SongInfoModel> partSongInfoModels = future.get(30, TimeUnit.SECONDS);
				songInfoModels.addAll(partSongInfoModels);
			}
			int songInfoModelCnt = songInfoModels.size();
			int total = songInfoModelCnt % 500 == 0 ? songInfoModelCnt / 500 : songInfoModelCnt / 500 + 1;
			for(int m = 0; m < total; m ++){
				int start = m * 500;
				int end = (m + 1) * 500 > songInfoModelCnt ? songInfoModelCnt : (m + 1) * 500;
				songInfoDao.saveSongInfoModels(songInfoModels.subList(start, end));
				logger.info("insert num: " + (end - start)); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
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

}
