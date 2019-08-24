package com.droidoxy.easymoneyrewards.model;

/**
 * Created by DroidOXY
 */
 
public class News {

	private String newsId,Title, Contents,Amount, AmountPremium,image, Status;

	public News() {
	}

	public News(String newsId, String Title, String Contents, String Amount, String AmountPremium, String image, String Status) {

		this.Title = Title;
		this.Contents = Contents;
		this.Amount = Amount;
		this.AmountPremium = AmountPremium;
		this.newsId = newsId;
		this.image = image;
		this.Status = Status;
	}

	public String getNewsId() {
		return newsId;
	}

	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String Title) {
		this.Title = Title;
	}

	public String getAmount() {
		return Amount;
	}

	public void setAmount(String Amount) {
		this.Amount = Amount;
	}

	public String getAmountPremium() {
		return AmountPremium;
	}

	public void setAmountPremium(String AmountPremium) {
		this.AmountPremium = AmountPremium;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String Status) {
		this.Status = Status;
	}

	public String getContents() {
		return Contents;
	}

	public void setContents(String SubTitle) {
		this.Contents = SubTitle;
	}


}