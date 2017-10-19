package es.spider.model;

public class SongComment {

	private String scNickName;
	private String scCommentTime;
	private String scContent;
	private String scAppreciation; // 点赞数
	private String scType; // 是否热门评论

	public SongComment() {}
	
	public SongComment(String scNickName, String scCommentTime, String scContent, String scAppreciation,
			String scType) {
		this.scNickName = scNickName;
		this.scCommentTime = scCommentTime;
		this.scContent = scContent;
		this.scAppreciation = scAppreciation;
		this.scType = scType;
	}

	public String getScNickName() {
		return scNickName;
	}

	public void setScNickName(String scNickName) {
		this.scNickName = scNickName;
	}

	public String getScCommentTime() {
		return scCommentTime;
	}

	public void setScCommentTime(String scCommentTime) {
		this.scCommentTime = scCommentTime;
	}

	public String getScContent() {
		return scContent;
	}

	public void setScContent(String scContent) {
		this.scContent = scContent;
	}

	public String getScAppreciation() {
		return scAppreciation;
	}

	public void setScAppreciation(String scAppreciation) {
		this.scAppreciation = scAppreciation;
	}

	public String getScType() {
		return scType;
	}

	public void setScType(String scType) {
		this.scType = scType;
	}

	@Override
	public String toString() {
		return "SongComment [scNickName=" + scNickName + ", scCommentTime=" + scCommentTime + ", scContent=" + scContent
				+ ", scAppreciation=" + scAppreciation + ", scType=" + scType + "]";
	}

}
