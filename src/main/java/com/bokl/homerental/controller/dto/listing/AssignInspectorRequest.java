package com.bokl.homerental.controller.dto.listing;

import jakarta.validation.constraints.NotNull;

public class AssignInspectorRequest {

    @NotNull
    private Long inspectorId;

    public AssignInspectorRequest() {
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(Long inspectorId) {
        this.inspectorId = inspectorId;
    }
}
