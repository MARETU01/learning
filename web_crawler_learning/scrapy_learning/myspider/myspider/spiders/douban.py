import scrapy
from ..items import MyspiderItem

class DoubanSpider(scrapy.Spider):
    name = "douban"
    allowed_domains = ["movie.douban.com"]
    start_urls = ["https://movie.douban.com/top250"]

    def parse(self, response):
        name = response.xpath('//*[@id="content"]/div/div[1]/ol/li/div/div[2]/div[1]/a/span[1]/text()').getall()
        score = response.xpath('//*[@id="content"]/div/div[1]/ol/li/div/div[2]/div[2]/div/span[2]/text()').getall()
        link = response.xpath('//*[@id="content"]/div/div[1]/ol/li/div/div[2]/div[1]/a/@href').getall()
        for i in range(25):
            item = MyspiderItem()
            item['name'] = name[i]
            item['score'] = score[i]

            yield scrapy.Request(link[i], callback=self.parse_data, meta={'item2': item})
            # yield item

        next_url = response.xpath('//*[@id="content"]/div/div[1]/div[2]/span[3]/a/@href').get()
        yield response.follow(next_url, callback=self.parse)


    def parse_data(self, response):
        item = response.meta['item2']
        con = response.xpath('//*[@property="v:summary"]/text()').get()
        # print(content)
        item['con'] = con
        yield item
