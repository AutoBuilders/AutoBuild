package com.autobuild.pipeline.definiton.dto.mapper;

/**
 * Generic Mapping interface for dto to entity mapping and vice versa.
 * 
 * @author Suvabrata Chowdhury
 */
//TODO: move it to global scope of project as it's also used in executor
public interface Mapper<T, R> {
    public R dtoToEntity(T dto);
    public T entityToDto(R entity);
}
