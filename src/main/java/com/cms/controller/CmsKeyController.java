package com.cms.controller;

import com.cms.dto.request.CmsKeyRequest;
import com.cms.dto.response.CmsKeyResponse;
import com.cms.service.CmsKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CmsKeyController {

    private final CmsKeyService cmsKeyService;

    @PostMapping("/projects/{projectId}/keys")
    @ResponseStatus(HttpStatus.CREATED)
    public CmsKeyResponse createKey(
            @PathVariable UUID projectId,
            @Valid @RequestBody CmsKeyRequest request) {
        return cmsKeyService.createKey(projectId, request);
    }

    @GetMapping("/projects/{projectId}/keys")
    public List<CmsKeyResponse> getKeysByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String category) {
        return cmsKeyService.getKeysByProject(projectId, category);
    }

    @GetMapping("/keys/{keyId}")
    public CmsKeyResponse getKey(@PathVariable UUID keyId) {
        return cmsKeyService.getKey(keyId);
    }

    @PutMapping("/keys/{keyId}")
    public CmsKeyResponse updateKey(
            @PathVariable UUID keyId,
            @Valid @RequestBody CmsKeyRequest request) {
        return cmsKeyService.updateKey(keyId, request);
    }

    @PatchMapping("/keys/{keyId}/toggle")
    public CmsKeyResponse toggleKeyStatus(@PathVariable UUID keyId) {
        return cmsKeyService.toggleKeyStatus(keyId);
    }

    @DeleteMapping("/keys/{keyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKey(@PathVariable UUID keyId) {
        cmsKeyService.deleteKey(keyId);
    }
}
