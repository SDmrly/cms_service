package com.cms.mapper;

import com.cms.domain.entity.Project;
import com.cms.dto.request.ProjectRequest;
import com.cms.dto.response.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project entity) {
        if (entity == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<ProjectResponse> toResponseList(List<Project> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Project toEntity(ProjectRequest request) {
        if (request == null) {
            return null;
        }

        return Project.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .active(true)
                .build();
    }

    public void updateEntityFromRequest(ProjectRequest request, Project entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
    }
}
