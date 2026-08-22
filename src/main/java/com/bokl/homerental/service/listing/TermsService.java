package com.bokl.homerental.service.listing;

import com.bokl.homerental.controller.dto.listing.TermsDefinitionResponse;
import com.bokl.homerental.controller.dto.unit.UpsertTermsRequest;
import com.bokl.homerental.entity.listing.TermsDefinition;
import com.bokl.homerental.repository.listing.TermsDefinitionRepository;
import com.bokl.homerental.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TermsService {

    private final TermsDefinitionRepository termsDefinitionRepository;
    private final MessageService msg;

    public TermsService(TermsDefinitionRepository termsDefinitionRepository, MessageService msg) {
        this.termsDefinitionRepository = termsDefinitionRepository;
        this.msg = msg;
    }

    @Transactional(readOnly = true)
    public List<TermsDefinitionResponse> getActiveTerms() {
        return termsDefinitionRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getActive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TermsDefinitionResponse> getAllTerms() {
        return termsDefinitionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TermsDefinitionResponse getTermsById(Long termsId) {
        TermsDefinition terms = termsDefinitionRepository.findById(termsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("listing.terms.not_found")));
        return toResponse(terms);
    }

    @Transactional
    public TermsDefinitionResponse createTerms(UpsertTermsRequest request) {
        if (termsDefinitionRepository.findAll().stream()
                .anyMatch(t -> t.getVersion().equals(request.getVersion()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Terms version already exists");
        }
        TermsDefinition terms = new TermsDefinition();
        apply(terms, request);
        if (Boolean.TRUE.equals(request.getActive())) {
            deactivateOthers(null, request.getTermsType());
        }
        return toResponse(termsDefinitionRepository.save(terms));
    }

    @Transactional
    public TermsDefinitionResponse updateTerms(Long termsId, UpsertTermsRequest request) {
        TermsDefinition terms = termsDefinitionRepository.findById(termsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        msg.get("listing.terms.not_found")));
        boolean versionClash = termsDefinitionRepository.findAll().stream()
                .anyMatch(t -> !t.getId().equals(termsId) && t.getVersion().equals(request.getVersion()));
        if (versionClash) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Terms version already exists");
        }
        apply(terms, request);
        if (Boolean.TRUE.equals(request.getActive())) {
            deactivateOthers(termsId, request.getTermsType());
        }
        return toResponse(termsDefinitionRepository.save(terms));
    }

    private void deactivateOthers(Long keepId, TermsDefinition.Type type) {
        for (TermsDefinition other : termsDefinitionRepository.findAll()) {
            if (keepId != null && other.getId().equals(keepId)) {
                continue;
            }
            if (other.getType() == type && Boolean.TRUE.equals(other.getActive())) {
                other.setActive(false);
            }
        }
    }

    private void apply(TermsDefinition terms, UpsertTermsRequest request) {
        terms.setVersion(request.getVersion());
        terms.setType(request.getTermsType());
        terms.setTitleEn(request.getTitleEn());
        terms.setTitleAr(request.getTitleAr());
        terms.setContentEn(request.getContentEn());
        terms.setContentAr(request.getContentAr());
        terms.setActive(Boolean.TRUE.equals(request.getActive()));
        if (terms.getEffectiveAt() == null) {
            terms.setEffectiveAt(Instant.now());
        }
    }

    private TermsDefinitionResponse toResponse(TermsDefinition terms) {
        return new TermsDefinitionResponse(
                terms.getId(),
                terms.getVersion(),
                terms.getType(),
                terms.getTitleAr(),
                terms.getTitleEn(),
                terms.getContentAr(),
                terms.getContentEn(),
                terms.getEffectiveAt(),
                Boolean.TRUE.equals(terms.getActive())
        );
    }
}
