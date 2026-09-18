package com.example.po.pocase.service;

import com.example.po.pocase.entity.POCase;
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