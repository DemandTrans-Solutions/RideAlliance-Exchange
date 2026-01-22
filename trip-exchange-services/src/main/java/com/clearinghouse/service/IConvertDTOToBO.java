/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;


/**
 *
 * @author manisha
 */
public interface IConvertDTOToBO {
    Object toBO(Object dto);

    Object toDTOCollection(Object boCollection);
} 
