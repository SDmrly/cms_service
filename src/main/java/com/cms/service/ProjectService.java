package com.cms.service;

import com.cms.domain.entity.Project;
import com.cms.dto.request.ProjectRequest;
import com.cms.dto.response.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(UUID projectId, ProjectRequest request);
    ProjectResponse getProject(UUID projectId);
    ProjectResponse getProjectByCode(String code);
    List<ProjectResponse> getActiveProjects();
    ProjectResponse toggleProjectStatus(UUID projectId);
    void deleteProject(UUID projectId);
    Project getProjectEntity(UUID projectId);
}
