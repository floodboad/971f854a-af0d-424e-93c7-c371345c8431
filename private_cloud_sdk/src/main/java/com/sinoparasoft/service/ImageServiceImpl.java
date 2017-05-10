package com.sinoparasoft.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstack4j.model.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinoparasoft.openstack.CloudManipulator;
import com.sinoparasoft.openstack.CloudManipulatorFactory;
import com.sinoparasoft.openstack.type.CloudConfig;

@Service
public class ImageServiceImpl implements ImageService {
	@Autowired
	CloudConfig cloudConfig;

	public Map<String, Object> getList(int pageNo, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();

		int totalImageCount = 0;
		
		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<? extends Image> osImages = cloud.getImages();
		
		List<Image> images = new ArrayList<Image>();
		for (Image image : osImages) {
			if (image.isSnapshot() == true) {
				continue;
			}

			images.add(image);
		}

		// sort image by create time in desc order
		Collections.sort(images, new Comparator<Image>() {
			@Override
			public int compare(Image o1, Image o2) {
				return (-1) * o1.getCreatedAt().compareTo(o2.getCreatedAt());
			}
		});

		int recordCount = images.size();
		totalImageCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}

		int pageTotal = recordCount / pageSize + 1;

		if (pageNo <= 0) {
			pageNo = 1;
		} else if (pageNo > pageTotal) {
			pageNo = pageTotal;
		}

		List<Image> pageImages = new ArrayList<Image>();
		if (pageNo < pageTotal) {
			pageImages = images.subList((pageNo - 1) * pageSize, pageNo * pageSize);
		} else {
			pageImages = images.subList((pageNo - 1) * pageSize, images.size());
		}

		List<com.sinoparasoft.type.Image> imageList = new ArrayList<com.sinoparasoft.type.Image>();
		for (Image image : pageImages) {
			com.sinoparasoft.type.Image imageValue = buildImageValue(image);
			imageList.add(imageValue);
		}

		map.put("pageNo", pageNo);
		map.put("pageTotal", pageTotal);
		map.put("images", imageList);
		map.put("total_image_count", totalImageCount);
		return map;
	}

	public List<com.sinoparasoft.type.Image> getAllInOneList() {
		List<com.sinoparasoft.type.Image> imageList = new ArrayList<com.sinoparasoft.type.Image>();

		CloudManipulator cloud = CloudManipulatorFactory.createCloudManipulator(cloudConfig,
				cloudConfig.getAdminProjectId());
		List<? extends Image> osImages = cloud.getImages();
		for (Image image : osImages) {
			if (image.isSnapshot() == true) {
				continue;
			}
			com.sinoparasoft.type.Image imageValue = buildImageValue(image);
			imageList.add(imageValue);
		}

		return imageList;
	}

	private com.sinoparasoft.type.Image buildImageValue(Image image) {
		com.sinoparasoft.type.Image imageValue = new com.sinoparasoft.type.Image();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		imageValue.setId(image.getId());
		imageValue.setName(image.getName());
		imageValue.setOwner(image.getOwner());
		Image.Status status = image.getStatus();
		imageValue.setStatus(status.name());
		// getSize() return null if image is in SAVING status
		switch (status) {
		case ACTIVE:
			imageValue.setSize(image.getSize());
			break;
		default:
			imageValue.setSize(0);
			break;
		}
		imageValue.setMinDisk(image.getMinDisk());
		imageValue.setPublic(image.isPublic());
		imageValue.setCreateTime(dateFormatter.format(image.getCreatedAt()));
		imageValue.setContainerFormat(image.getContainerFormat().name());

		return imageValue;
	}
}
