package es.spider.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;
import org.springframework.util.CollectionUtils;

import com.ibatis.sqlmap.client.SqlMapExecutor;

import es.spider.model.SongInfo;
import es.spider.model.SongInfoModel;

@SuppressWarnings({"deprecation", "unchecked"})
public class SongInfoDaoImpl extends SqlMapClientDaoSupport implements SongInfoDao{

	private Log logger = LogFactory.getLog(SongInfoDaoImpl.class);
	
	@Override
	public void saveSongInfoModels(final List<SongInfoModel> songInfoModels) {
		if(!CollectionUtils.isEmpty(songInfoModels)){
			try {
				getSqlMapClientTemplate().execute(new SqlMapClientCallback<SongInfo>() {
					
					@Override
					public SongInfo doInSqlMapClient(SqlMapExecutor executor) throws SQLException {
						executor.startBatch();
						for(SongInfoModel sim : songInfoModels){
							executor.insert("saveSongInfoModel", sim);
						}
						executor.executeBatch();
						return null;
					}
				});
			} catch (Exception e) {
				logger.error("Insert error!", e);
			}
		}
	}

	@Override
	public List<SongInfoModel> getSongInfoModelByLimit(long start, long limit) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("start", start);
		paramMap.put("limit", limit);
		return getSqlMapClientTemplate().queryForList("getSongInfoModelByLimit", paramMap);
	}
}
