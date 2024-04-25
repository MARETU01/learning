class UrlManager():
    def __init__(self):
        self.new_urls = set()
        self.old_urls = set()
    
    def add_new_url(self, url):
        if url == None or len(url) == 0:
            return
        self.new_urls.add(url)
    
    def add_new_urls(self, urls):
        if urls == None or len(urls) == 0:
            return
        
        for url in urls:
            self.add_new_url(url)
    
    def get_url(self):
        if self.has_new_url():
            url = self.new_urls.pop()
            self.old_urls.add(url)
            return url
        else:
            return None
    
    def has_new_url(self):
        return len(self.new_urls) > 0


if __name__ == "__main__":
    url_manager = UrlManager()
    
    url_manager.add_new_url("url1")
    url_manager.add_new_urls(["url1", "url2"])
    print(url_manager.new_urls, url_manager.old_urls)
    print('----------------------------------')
    
    url_manager.get_url()
    print(url_manager.new_urls, url_manager.old_urls)
    print('----------------------------------')
    
    url_manager.get_url()
    print(url_manager.new_urls, url_manager.old_urls)
    print('----------------------------------')
    
    print(url_manager.has_new_url())


