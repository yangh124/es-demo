package com.yh.esdemo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author : yh
 * @date : 2021/8/31 21:28
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer implements Serializable {

    private String uuid;

    private String name;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
