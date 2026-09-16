package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.pocase.entity.POCase;
import com.example.po.PoApplication.pocase.entity.POStatus;
import com.example.po.PoApplication.pocase.repository.POCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class POProcessingStateService {

    private final POCaseRepository poCaseRepository;

    public POProcessingStateService(
            POCaseRepository poCaseRepository) {

        this.poCaseRepository =
                poCaseRepository;
    }

    @Transactional
    public void markFlexProcessing(
            POCase poCase) {

        poCase.setStatus(
                POStatus.FLEX_PROCESSING
        );

        poCase.setUpdatedAt(
                LocalDateTime.now()
        );

        poCaseRepository.save(poCase);
    }

    @Transactional
    public void markPOCreated(
            POCase poCase,
            String poNumber) {

        poCase.setPoNumber(poNumber);

        poCase.setStatus(
                POStatus.PO_CREATED
        );

        poCase.setUpdatedAt(
                LocalDateTime.now()
        );

        poCaseRepository.save(poCase);
    }
}