package com.cms.service;

import com.cms.domain.entity.Project;
import com.cms.dto.request.ProjectRequest;
import com.cms.dto.response.ProjectResponse;
import com.cms.exception.DuplicateResourceException;
import com.cms.exception.ResourceNotFoundException;
import com.cms.mapper.ProjectMapper;
import com.cms.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectManager implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        log.info("Creating new project with code: {}", request.getCode());
        
        if (projectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Project with code '" + request.getCode() + "' already exists");
        }

        Project project = projectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        log.info("Updating project with id: {}", projectId);
        
        Project project = getProjectEntity(projectId);

        // Check code uniqueness if code is changed
        if (!project.getCode().equals(request.getCode()) && projectRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Project with code '" + request.getCode() + "' already exists");
        }

        projectMapper.updateEntityFromRequest(request, project);
        Project updatedProject = projectRepository.save(project);
        
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId) {
        log.info("Fetching project by id: {}", projectId);
        return projectMapper.toResponse(getProjectEntity(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByCode(String code) {
        log.info("Fetching project by code: {}", code);
        Project project = projectRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + code));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getActiveProjects() {
        log.info("Fetching all active projects");
        return projectMapper.toResponseList(projectRepository.findByActiveTrue());
    }

    @Override
    @Transactional
    public ProjectResponse toggleProjectStatus(UUID projectId) {
        log.info("Toggling status for project: {}", projectId);
        Project project = getProjectEntity(projectId);
        project.setActive(!project.isActive());
        
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId) {
        log.info("Deleting project: {}", projectId);
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }

    @Override
    public Project getProjectEntity(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }
}
