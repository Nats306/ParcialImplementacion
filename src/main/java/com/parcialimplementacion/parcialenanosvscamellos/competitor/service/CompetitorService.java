package com.parcialimplementacion.parcialenanosvscamellos.competitor.service;

import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ConflictException;
import com.parcialimplementacion.parcialenanosvscamellos.common.exceptions.ResourceNotFoundException;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorRequest;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.dto.CompetitorResponse;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.Competitor;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorStatus;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.entity.CompetitorType;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.mapper.CompetitorMapper;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.repository.ICompetitorRepository;
import com.parcialimplementacion.parcialenanosvscamellos.competitor.specification.CompetitorSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetitorService {
    private final ICompetitorRepository competitorRepository;

    @Transactional(readOnly = true)
    public Page<CompetitorResponse> getCompetitors(CompetitorType type, CompetitorStatus status, Pageable pageable) {
        Specification<Competitor> spec = CompetitorSpecification.withFilters(type, status);
        return competitorRepository.findAll(spec, pageable).map(CompetitorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompetitorResponse getCompetitorById(UUID id) {
        Competitor competitor = competitorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Competitor not found with id: " + id));
        return CompetitorMapper.toResponse(competitor);
    }

    @Transactional
    public CompetitorResponse addCompetitor(CompetitorRequest competitorRequest){
        if (competitorRepository.existsByNickname(competitorRequest.nickname())) {
            throw new ConflictException("Nickname already in use: " + competitorRequest.nickname());
        }
        Competitor competitor = CompetitorMapper.toEntity(competitorRequest);
        Competitor savedCompetitor = competitorRepository.save(competitor);
        return CompetitorMapper.toResponse(savedCompetitor);
    }

    @Transactional
    public CompetitorResponse updateCompetitor(UUID id, CompetitorRequest competitorRequest){
        Competitor existingCompetitor = competitorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Competitor not found with id: " + id));
        boolean nicknameChanged = !existingCompetitor.getNickname().equals(competitorRequest.nickname());
        if(nicknameChanged && competitorRepository.existsByNickname(competitorRequest.nickname())){
            throw new ConflictException("Nickname already in use: " + competitorRequest.nickname());
        }

        CompetitorMapper.updateEntity(existingCompetitor, competitorRequest);
        Competitor updatedCompetitor = competitorRepository.save(existingCompetitor);
        return CompetitorMapper.toResponse(updatedCompetitor);
    }

    @Transactional
    public CompetitorResponse updateStatus(UUID id, CompetitorStatus newStatus) {
        Competitor existingCompetitor = competitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competitor not found with id: " + id));

        existingCompetitor.setCurrentStatus(newStatus);
        Competitor updatedCompetitor = competitorRepository.save(existingCompetitor);
        return CompetitorMapper.toResponse(updatedCompetitor);
    }

    @Transactional
    public void deleteCompetitor(UUID id) {
        Competitor existingCompetitor = competitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competitor not found with id: " + id));

        // TODO: cuando exista RaceResult, validar si el competidor tiene resultados oficiales.
        // Si los tiene -> no permitir borrado físico, forzar retiro/desactivación (currentStatus = RETIRED).
        // Si no los tiene -> se podría permitir delete físico o mantener siempre soft-delete, según se decida.
        competitorRepository.delete(existingCompetitor);
    }
}
