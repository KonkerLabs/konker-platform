package com.konkerlabs.platform.registry.business.repositories.upload;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;

import java.io.InputStream;

/**
 * Created by andre on 28/12/16.
 */
public interface UploadRepository {

    enum Validations {
        INVALID_PATH("service.upload.s3.file.delete.path.invalid"),
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
     * Delete a file from bucket
     * @param path
     * @throws BusinessException
     */
    void delete(String path) throws BusinessException;

    /**
     * Download file in base64 serialization
     * @param filePath
     * @return String
     * @throws Exception
     */
    String downloadAsBase64(String filePath) throws Exception;

    /**
     * Download a file from bucket
     * @param filePath
     * @return InputStream file
     * @throws Exception
     */
    InputStream downloadFile(String filePath) throws BusinessException;

    /**
     * Upload a file to bucket
     * @param base64
     * @param fileName
     * @param suffix
     * @param isPublic
     * @return String path generated
     * @throws Exception
     */
    String upload(String base64, String fileName, String suffix, boolean isPublic) throws Exception;

    /**
     * Validate the file
     * @param is
     * @param type {the file extension}
     * @throws BusinessException
     */
    void validateFile(InputStream is, String type) throws BusinessException;
}
