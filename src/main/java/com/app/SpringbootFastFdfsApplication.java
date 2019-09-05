package com.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resources;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.proto.storage.DownloadByteArray;
import com.github.tobato.fastdfs.domain.upload.FastFile;
import com.github.tobato.fastdfs.service.FastFileStorageClient;

@SpringBootApplication
@Controller
public class SpringbootFastFdfsApplication {

	@Autowired
	private FastFileStorageClient storageClient;

	public static void main(String[] args) {
		SpringApplication.run(SpringbootFastFdfsApplication.class, args);
	}

	@RequestMapping("/fileForm")
	public String fileForm() {

		return "uploadForm";
	}

	@ResponseBody
	@RequestMapping("/fileUpload")
	public StorePath fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		
		String suffixName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
		 Set<MetaData> metaDataSet =new HashSet<>();
		 metaDataSet.add(new MetaData("email", "zhiyong.jin@aliyun.com")) ;
		FastFile fastFile = new FastFile(file.getInputStream(), file.getSize(), suffixName, metaDataSet);
		StorePath uploadFile = storageClient.uploadFile(fastFile);
		return uploadFile;
	}
	@ResponseBody
	@RequestMapping("/fileDownLoad/{fileName}")
	public ResponseEntity<Resource> fileDownLoad(@PathVariable(name = "fileName") String fileName) throws UnsupportedEncodingException {
		
		DownloadByteArray downloadByteArray = new DownloadByteArray();
		byte[] downloadFile = storageClient.downloadFile("group1", "M00/00/00/wKgDDl1xLVKAXA87AAAqMvlb9Ss429.jpg",downloadByteArray);
		Resource resource=new ByteArrayResource(downloadFile);
		//String filename = "wKgDDl1xLV你好KAXA87AAAqMvlb9Ss429.jpg";
        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName,"UTF-8") + "\"")
                .body(resource);
    }
	
	

}
