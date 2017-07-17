package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdDockImages;
import com.asiainfo.comm.module.models.query.QAdDockImages;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by yangry on 2016/6/16 0016.
 */
@Component
public class AdDockImagesDAO {
    public List<AdDockImages> getDockImagsByProjectName(String projectName) {
        return new QAdDockImages().projectName.eq(projectName).findList();
    }

    public void updateDockImagsByTag(AdDockImages adDockImages) {
        if (adDockImages != null) {
            adDockImages.save();
        }
    }

    public List<AdDockImages> getDockImagsByTag(String tagName) {
        return new QAdDockImages().tag.eq(tagName).findList();
    }

}
