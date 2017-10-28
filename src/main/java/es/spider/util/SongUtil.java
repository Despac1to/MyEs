package es.spider.util;

import es.spider.model.SongComment;
import es.spider.model.SongInfo;
import es.spider.model.SongInfoModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongUtil {

    private static final Log logger = LogFactory.getLog(SongUtil.class);

    public static List<SongInfoModel> genSongInfoModels(List<SongInfo> songInfos){
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
    public static SongInfo crawlSongById(String songId){
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

    public static void initSongList(int totalSongList, int limit, int offSet, String songListPrefix) throws InterruptedException, IOException{
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

}
