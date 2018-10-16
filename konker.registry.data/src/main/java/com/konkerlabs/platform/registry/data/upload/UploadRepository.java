package com.konkerlabs.platform.registry.data.upload;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;

import java.io.InputStream;

public interface UploadRepository {

    enum Validations {
        INVALID_S3_BUCKET_CREDENTIALS("service.upload.s3.bucket.invalid"),
        INVALID_PARAMETERS("service.upload.s3.parameters.invalid"),
        INVALID_FILE_TYPE("service.user.validation.upload.type.invalid"),
        INVALID_FILE_SIZE("service.user.validation.upload.size.invalid");

        private String code;

        Validations(String code) {
            this.code = code;
        }

        public String getCode(){
            return code;
        }
    }

    /**
     * Upload a file to bucket
     * @param inputStream
     * @param fileKey
     * @param fileName
     * @param isPublic
     * @return String path generated
     * @throws Exception
     */
    String upload(InputStream inputStream, String fileKey, String fileName, boolean isPublic) throws Exception;

    /**
     * Validate the file
     * @param is
     * @param type {the file extension}
     * @throws BusinessException
     */
    void validateFile(InputStream is, String type) throws BusinessException;

}
