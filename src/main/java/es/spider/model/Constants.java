package es.spider.model;
 
public class Constants {
	
	public static final String DOMAIN = "http://music.163.com";
	
	// 云音乐开放api
	public static final String NET_EASE_COMMENT_API_URL = "http://music.163.com/weapi/v1/resource/comments/R_SO_4_";

	// decode文本
	public static final String TEXT = "{\"username\": \"\", \"rememberLogin\": \"true\", \"password\": \"\"}";
	
	// 歌单url
	public static final String SONG_LIST_PREFIX = "http://music.163.com/discover/playlist/?order=hot&cat=全部&";
	
	public static final int PAGE_SIZE = 35;
	
	// 位偏移
	public static final int OFF_SET = 0;
	
	// 8线程
	public static final int THREAD_NUM = Runtime.getRuntime().availableProcessors();
}
