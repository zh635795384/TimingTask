package com.simatelex.admin.query;


import lombok.Data;

/**
 * @author nancy zhao
 * @version 1.0
 */
@Data
public class BaseQuery {
    private Integer page=1;
    private Integer limit=20;

}
