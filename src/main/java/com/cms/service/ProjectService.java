package com.cms.service;

import com.cms.domain.entity.Project;
import com.cms.dto.request.ProjectRequest;
import com.cms.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(@NonNull UUID projectId, ProjectRequest request);
    ProjectResponse getProject(@NonNull UUID projectId);
    ProjectResponse getProjectByCode(String code);
    List<ProjectResponse> getActiveProjects();
    ProjectResponse toggleProjectStatus(@NonNull UUID projectId);
    void deleteProject(@NonNull UUID projectId);
    Project getProjectEntity(@NonNull UUID projectId);
}
