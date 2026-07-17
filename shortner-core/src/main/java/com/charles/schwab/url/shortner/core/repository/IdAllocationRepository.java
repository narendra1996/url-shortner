package com.charles.schwab.url.shortner.core.repository;

import com.charles.schwab.url.shortner.core.entity.IdAllocation;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface IdAllocationRepository extends CrudRepository<IdAllocation, String> {
    @Query("SELECT * FROM id_allocation WHERE allocator_name = :allocatorName FOR UPDATE")
    IdAllocation findByAllocatorNameForUpdate(@Param("allocatorName") String allocatorName);
}
