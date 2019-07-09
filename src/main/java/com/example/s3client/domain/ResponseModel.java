package com.example.s3client.domain;

import lombok.Builder;
import lombok.Data;

/**
 * @author Vyacheslav Aleferov
 */
@Data
@Builder
public class ResponseModel {

    private String nameDecoded;

    private String nameEncoded;

    private byte[] data;

}
