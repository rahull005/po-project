package com.example.po.PoApplication.pocase.service;

import com.example.po.PoApplication.pocase.entity.POCase;
import org.springframework.stereotype.Service;

@Service
public class ApprovalRuleService {

    public boolean isReviewRequired(POCase poCase) {

        /*
         * Temporary development rule.
         *
         * We will replace this with the actual
         * business rules later.
         */

        return true;
    }
}