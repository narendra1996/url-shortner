package com.charles.schwab.url.shortner.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("id_allocation")
public class IdAllocation {
    @Id
    private String allocatorName;
    private Long currentMax;

    public IdAllocation() {}

    public String getAllocatorName() { return allocatorName; }
    public void setAllocatorName(String allocatorName) { this.allocatorName = allocatorName; }
    public Long getCurrentMax() { return currentMax; }
    public void setCurrentMax(Long currentMax) { this.currentMax = currentMax; }
}
