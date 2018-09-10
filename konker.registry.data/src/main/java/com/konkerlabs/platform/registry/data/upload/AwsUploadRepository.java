package com.konkerlabs.platform.registry.data.upload;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.data.core.config.S3BucketConfig;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class AwsUploadRepository implements UploadRepository {

    @Autowired
    private S3Client client;

    @Autowired
    private S3Credentials credentials;

    @Autowired
    private S3BucketConfig s3BucketConfig;

    private AmazonS3 s3Client;

    public AwsUploadRepository() {
    }

    @PostConstruct
    public void warmUp() {
        s3Client = client.getClient(credentials.getCredentials());
    }


    @Override
    public void validateFile(InputStream is, String type) throws BusinessException {
        validateFileType(type);
        try {
            validateFileSize(is.available());
        } catch (IOException e){
            throw new BusinessException(Validations.INVALID_FILE_SIZE.getCode());
        }

    }

    private void validateFileSize(Integer size) throws BusinessException {
        Integer maxSize = s3BucketConfig.getMaxSize();
        if (size > maxSize) {
            throw new BusinessException(Validations.INVALID_FILE_SIZE.getCode());
        }
    }

    private void validateFileType(String type) throws BusinessException {
        String fileTypes = s3BucketConfig.getFileTypes();
        List<String> types = null;
        if (Optional.ofNullable(fileTypes).isPresent()) {
            if (fileTypes.contains(",")) {
                types = Arrays.asList(fileTypes.split(","));
            } else {
                types = Collections.emptyList();
                types.add(fileTypes);
            }
        }
        boolean approved = false;
        for (String item : types) {
            if (type.equalsIgnoreCase(item)) {
                approved = true;
                break;
            }
        }
        if(!approved){
            throw new BusinessException(Validations.INVALID_FILE_TYPE.getCode());
        }
    }

    @Override
    public String upload(InputStream is, String fileKey, String fileName, boolean isPublic) throws Exception {
        String suffix = FilenameUtils.getExtension(fileName);
        return upload(is, fileKey, fileName, suffix, isPublic);
    }

    public String upload(InputStream is, String fileKey, String fileName, String suffix, Boolean isPublic) throws Exception {
        validateFile(is, suffix);
        if (isPublic == null) {
            isPublic = Boolean.TRUE;
        }
        if ((is != null) && (fileKey != null)) {
            try {
                byte[] bytes = IOUtils.toByteArray(is);
                s3Client.putObject(
                        new PutObjectRequest(
                        		s3BucketConfig.getName(),
                                fileKey,
                                new ByteArrayInputStream(bytes),
                                S3ObjectMetadata.getObjectMetadata(bytes)
                        ).withCannedAcl(isPublic ? CannedAccessControlList.PublicRead : CannedAccessControlList.AuthenticatedRead)
                );
                return fileName + '.' + suffix;
            } catch (AmazonServiceException | IOException e) {
                throw new BusinessException(Validations.INVALID_S3_BUCKET_CREDENTIALS.getCode());
            } finally {
                is.close();
            }
        } else {
            throw new BusinessException(Validations.INVALID_PARAMETERS.getCode());
        }
    }
}

@Repository
class S3Client {

    public AmazonS3 getClient(BasicAWSCredentials credentialsProvider) {
        AmazonS3 client = new AmazonS3Client(credentialsProvider);
        return client;
    }
}

@Repository
class S3Credentials {

    private static String key;
    private static String secret;

    @Autowired
    private S3BucketConfig s3BucketConfig;

    @PostConstruct
    public void init() {
        this.key = s3BucketConfig.getKey();
        this.secret = s3BucketConfig.getSecret();
    }

    public BasicAWSCredentials getCredentials() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(key, secret);
        return awsCredentials;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        S3Credentials.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        S3Credentials.secret = secret;
    }
}

@Repository
class S3ObjectMetadata {

    public ObjectMetadata getObjectMetadata(InputStream is) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        return objectMetadata;
    }

    public static ObjectMetadata getObjectMetadata(byte[] is) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(Long.valueOf((long) is.length));
        return objectMetadata;
    }
}