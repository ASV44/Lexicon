package com.example.hackintosh.lexicon;

import java.util.List;

/**
 * Created by hackintosh on 2/28/17.
 */

public class ImageDataModel {

    private List<ImageDescription> hits;

    public class ImageDescription {
        private String tags;
        private String webformatURL;

        public String getTags() { return this.tags; }
        public String getWebformatURL() { return this.webformatURL; }
    }

    public List<ImageDescription> getHits() { return this.hits; }
}
