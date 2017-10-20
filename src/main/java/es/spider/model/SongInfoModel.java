package es.spider.model;

public class SongInfoModel {

	private Long id;
	private String song;
	private String singer;
	private String commentUser;
	private String commentType;
	private String commentTime;
	private String commentContent;
	private int commentAppreciation;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSong() {
		return song;
	}

	public void setSong(String song) {
		this.song = song;
	}

	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getCommentUser() {
		return commentUser;
	}

	public void setCommentUser(String commentUser) {
		this.commentUser = commentUser;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public String getCommentTime() {
		return commentTime;
	}

	public void setCommentTime(String commentTime) {
		this.commentTime = commentTime;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public int getCommentAppreciation() {
		return commentAppreciation;
	}

	public void setCommentAppreciation(int commentAppreciation) {
		this.commentAppreciation = commentAppreciation;
	}
}
