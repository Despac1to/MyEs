package es.index.service;

import java.util.List;

import es.spider.dao.SongInfoDao;
import es.spider.model.SongInfoModel;

public class SongInfoServiceImpl implements SongInfoService{

	private SongInfoDao songInfoDao;
	
	@Override
	public List<SongInfoModel> getSongInfoModelByLimit(long start, long limit) {
		return null;
	}

	public void setSongInfoDao(SongInfoDao songInfoDao) {
		this.songInfoDao = songInfoDao;
	}

}
