package es.index.service;

import java.util.List;

import es.spider.model.SongInfoModel;

public interface SongInfoService {

	List<SongInfoModel> getSongInfoModelByLimit(long start, long limit);
}
