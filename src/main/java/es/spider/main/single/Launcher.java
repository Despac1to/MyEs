package es.spider.main.single;

public class Launcher {
	
	private Spider spider;
	
	public void launch(){
//		new Thread(spider).start();
		System.out.println("Single Launcher,spider: " + spider.toString());
	}

	public void setSpider(Spider spider) {
		this.spider = spider;
	}
}
