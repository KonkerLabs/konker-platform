package com.konkerlabs.platform.registry.web.services.api;

import java.io.InputStream;

import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;

public interface UploadService {

    enum Validations {

        INVALID_FILE_SIZE("service.upload.file.size.invalid"),
        INVALID_PATH("service.upload.file.path.invalid"),
        INVALID_TYPE("service.upload.file.type.invalid");

        private String code;

        public String getCode(){
            return this.code;
        }

        Validations(String code) {
            this.code = code;
        }
    }


    /**
     * Download file from bucket
     * @param filePath
     * @return Inputstream
     * @throws Exception
     */
    ServiceResponse<InputStream> downloadFile(String filePath) throws Exception;

    /**
     * Download file from bucket
     * @param filePath
     * @return Base64 String serialization
     * @throws Exception
     */
    ServiceResponse<String> downloadAsBase64(String filePath) throws Exception;

    /**
     * Upload a file based on base64 string serialization
     * @param base64File
     * @param fileName
     * @param isPublic
     * @return
     */
    ServiceResponse<String> upload(String base64File, String fileName, String ext, boolean isPublic);



    /**
     * Upload a file based on base64 string serialization
     * this method use the img html tag (data:image/EXTENSION;base64,)
     * @param base64File
     * @return
     */
    ServiceResponse<String> uploadBase64Img(String base64File, boolean isPublic);

    /**
     * Upload a file based on inputstream serialization
     * @param file
     * @param fileName
     * @param isPublic
     * @return
     */
    ServiceResponse<String> upload(InputStream file, String fileName, String ext, boolean isPublic);

    /**
     * Delete the file from repository
     * @param path
     * @return ServiceResponse
     */
    ServiceResponse<Void> delete(String path);


}
