package es.spider.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.google.common.collect.ImmutableMap;

import es.spider.model.Constants;
import es.spider.model.SongComment;
import es.spider.model.SongInfo;

public class HtmlParser {

	private static String domain = Constants.DOMAIN;

	// 通过html将歌单添加到list中
	public static void putSongList(String html) throws InterruptedException, IOException {
		Document doc = Jsoup.parse(html);
		Element content = doc.getElementById("m-pl-container");
		Elements as = content.select("li > div > a.msk");
		
		for (Element a : as) {
			SongQueueUtil.addSongList(Constants.DOMAIN + a.attr("href"));
		}
	}
	
	// 通过url将歌曲添加到list中
	public static void putSong(String url) throws IOException, InterruptedException{
		Document doc = Jsoup.connect(url).get();
		Element content = doc.getElementById("song-list-pre-cache");
		Elements as = content.select("ul.f-hide li a");
		
		for (Element a : as) {
			String suffix = a.attr("href");
			SongQueueUtil.addUncrawledSong(suffix.substring(suffix.indexOf("id=") + 3));
		}
	}
	
	// 通过url获取歌单内歌曲
	public static List<String> genSong(String url) throws IOException{
		List<String> songs = new ArrayList<>();
		
		Document doc = Jsoup.connect(url).get();
		Element content = doc.getElementById("song-list-pre-cache");
		Elements as = content.select("ul.f-hide li a");
		for(Element a :as){
			String suffix = a.attr("href");
			songs.add(suffix.substring(suffix.indexOf("id=") + 3));
		}
		return songs;
	}

	public static SongInfo getSongInfoById(String id) throws Exception {
		String songUrl = domain + "/song?id=" + id;
		Document doc = Jsoup.parse(new URL(songUrl), 5000);

		String secKey = new BigInteger(100, new SecureRandom()).toString(32).substring(0, 16);
		String encText = Encryptor.aesEncrypt(Encryptor.aesEncrypt(Constants.TEXT, "0CoJUm6Qyw8W8jud"), secKey);
		String encSecKey = Encryptor.rsaEncrypt(secKey);
		Response response = Jsoup.connect(Constants.NET_EASE_COMMENT_API_URL + id + "/?csrf_token=")
				.method(Connection.Method.POST).header("Referer", domain)
				.data(ImmutableMap.of("params", encText, "encSecKey", encSecKey)).execute();

		Object res = JSON.parse(response.body());

		if (res == null) {
			return null;
		}

		SongInfo songInfo = new SongInfo();
		int commentCnt = (int) JSONPath.eval(res, "$.total");
		int hotCommentCnt = (int) JSONPath.eval(res, "$.hotComments.size()");
		int latestCommentCnt = (int) JSONPath.eval(res, "$.comments.size()");

		String originalTitle = doc.title();
		songInfo.setSiTitle(originalTitle.substring(0, originalTitle.indexOf("- 单曲 - 网易云音乐")));
		songInfo.setSiUrl(songUrl);
		songInfo.setSiCommentCnt(commentCnt);

		List<SongComment> scList = new ArrayList<>();

		// 热评
		if (hotCommentCnt > 0) {
			for (int i = 0; i < hotCommentCnt; i++) {
				String nickname = JSONPath.eval(res, "$.hotComments[" + i + "].user.nickname").toString();
				String time = Encryptor.stampToDate((long) JSONPath.eval(res, "$.hotComments[" + i + "].time"));
				String content = JSONPath.eval(res, "$.hotComments[" + i + "].content").toString();
				String appreciation = JSONPath.eval(res, "$.hotComments[" + i + "].likedCount").toString();
				scList.add(new SongComment(nickname, time, content, appreciation, "hotComment"));
			}
		} else if (commentCnt > 0) {
			for (int i = 0; i < latestCommentCnt; i++) {
				String nickname = JSONPath.eval(res, "$.comments[" + i + "].user.nickname").toString();
				String time = Encryptor.stampToDate((long) JSONPath.eval(res, "$.comments[" + i + "].time"));
				String content = JSONPath.eval(res, "$.comments[" + i + "].content").toString();
				String appreciation = JSONPath.eval(res, "$.comments[" + i + "].likedCount").toString();
				scList.add(new SongComment(nickname, time, content, appreciation, "latestCommentCount"));
			}
		}

		songInfo.setSongComments(scList);
		return songInfo;
	}

	// 加密class
	private static class Encryptor {

		public static String aesEncrypt(String value, String key) throws Exception {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("UTF-8"), "AES"),
					new IvParameterSpec("0102030405060708".getBytes("UTF-8")));
			return Base64.encodeBase64String(cipher.doFinal(value.getBytes()));
		}

		public static String rsaEncrypt(String value) throws UnsupportedEncodingException {
			value = new StringBuilder(value).reverse().toString();
			BigInteger valueInt = hexToBigInteger(stringToHex(value));
			BigInteger pubkey = hexToBigInteger("010001");
			BigInteger modulus = hexToBigInteger(
					"00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7");
			return valueInt.modPow(pubkey, modulus).toString(16);
		}

		public static BigInteger hexToBigInteger(String hex) {
			return new BigInteger(hex, 16);
		}

		public static String stringToHex(String text) throws UnsupportedEncodingException {
			return DatatypeConverter.printHexBinary(text.getBytes("UTF-8"));
		}

		public static String stampToDate(long stamp) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(stamp));
		}
	}
}
