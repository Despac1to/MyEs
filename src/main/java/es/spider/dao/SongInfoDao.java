package es.spider.dao;

import java.util.List;

import es.spider.model.SongInfoModel;

public interface SongInfoDao {

	void saveSongInfoModels(List<SongInfoModel> songInfoModels);
}
