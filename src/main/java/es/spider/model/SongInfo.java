package es.spider.model;

import java.util.List;

public class SongInfo {

	private String siTitle; // singer + title
	private String siUrl;
	private int siCommentCnt;
	private List<SongComment> songComments; // 评论

	public SongInfo() {}
	
	public SongInfo(String siTitle, String siUrl, int siCommentCnt, List<SongComment> songComments) {
		this.siTitle = siTitle;
		this.siUrl = siUrl;
		this.siCommentCnt = siCommentCnt;
		this.songComments = songComments;
	}

	public String getSiTitle() {
		return siTitle;
	}

	public void setSiTitle(String siTitle) {
		this.siTitle = siTitle;
	}

	public String getSiUrl() {
		return siUrl;
	}

	public void setSiUrl(String siUrl) {
		this.siUrl = siUrl;
	}

	public int getSiCommentCnt() {
		return siCommentCnt;
	}

	public void setSiCommentCnt(int siCommentCnt) {
		this.siCommentCnt = siCommentCnt;
	}

	public List<SongComment> getSongComments() {
		return songComments;
	}

	public void setSongComments(List<SongComment> songComments) {
		this.songComments = songComments;
	}

}
