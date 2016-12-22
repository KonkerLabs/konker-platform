package com.konkerlabs.platform.registry.business.services;

import com.konkerlabs.platform.registry.business.repositories.AwsUploadRepository;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.business.services.api.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UploadServiceImpl implements UploadService {


    @Autowired
    private AwsUploadRepository repository;

    public UploadServiceImpl() {
    }

    public UploadServiceImpl(AwsUploadRepository repository) {
        this.repository = repository;
    }


    @Override
    public ServiceResponse<String> downloadAsBase64(String filePath) throws Exception {
        String downloadResult = repository.downloadAsBase64(filePath);
        return ServiceResponseBuilder.<String>ok().withResult(downloadResult).build();
    }

    @Override
    public ServiceResponse<InputStream> downloadFile(String filePath) throws Exception {
        InputStream downloadResult = repository.downloadFile(filePath);
        return ServiceResponseBuilder.<InputStream>ok().withResult(downloadResult).build();
    }


    @Override
    public ServiceResponse<String> upload(String base64File, String fileName, String ext, boolean isPublic) {
        try {
            String uploadResult = repository.upload(base64File, fileName, ext, isPublic);
            return ServiceResponseBuilder
                    .<String>ok()
                    .withResult(uploadResult)
                    .build();
        } catch (Exception e) {
            return ServiceResponseBuilder
                    .<String>error()
                    .withMessage("Error in upload process")
                    .build();
        }
    }

    /**
     * Try to avoid name colisions
     *
     * @param source
     * @return String filepath
     * @throws NoSuchAlgorithmException
     */
    private String getUniqueFileName(String source) {
        return UUID.randomUUID().toString();
    }

    @Override
    public ServiceResponse<String> uploadBase64Img(String base64File, boolean isPublic) {
        String fileExt = base64File.split(",")[0].split("/")[1].split(";")[0];
        String fileBase64 = base64File.split(",")[1];
        return upload(fileBase64, getUniqueFileName(null), fileExt, isPublic);
    }

    @Override
    public ServiceResponse<String> upload(InputStream file, String fileName, String ext, boolean isPublic) {
        try {
            String uploadResult = repository.upload(file, getUniqueFileName(null), ext, isPublic);
            return ServiceResponseBuilder
                    .<String>ok()
                    .withResult(uploadResult)
                    .build();
        } catch (Exception e) {
            return ServiceResponseBuilder
                    .<String>error()
                    .withMessage("Error in upload process")
                    .build();
        }
    }

    @Override
    public ServiceResponse<Void> delete(String path) {
        return null;
    }

    public AwsUploadRepository getRepository() {
        return repository;
    }

    public void setRepository(AwsUploadRepository repository) {
        this.repository = repository;
    }
}
