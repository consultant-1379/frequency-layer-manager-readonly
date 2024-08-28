/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.testutils.profilechangecalculator;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.EnrichedPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;

public class EnrichedPolicyOutputEventPrototypeLbq extends EnrichedPolicyOutputEventPrototype {

    public EnrichedPolicyOutputEventPrototypeLbq(final EnrichedPolicyOutputEvent enrichedPolicyOutputEvent) {
        super(enrichedPolicyOutputEvent);
    }

    public EnrichedPolicyOutputEventPrototypeLbq usersToMoveAtTargetOne(final int number) {
        final int totalUsers = Integer.parseInt(enrichedPolicyOutputEvent.getPolicyOutputEvent()
                .getLoadBalancingQuanta().getSourceUsersMove());
        final int firstUsers = Integer.parseInt(enrichedPolicyOutputEvent.getTargetCells().get(0).getTargetUsersMove());
        final int newTotalUsers = totalUsers + number - firstUsers;
        final List<Integer> newUsers = new ArrayList<>();
        newUsers.add(number);
        if (enrichedPolicyOutputEvent.getTargetCells().size() > 1) {
            newUsers.add(Integer.parseInt(enrichedPolicyOutputEvent.getTargetCells().get(1).getTargetUsersMove()));
        }
        final ProposedLoadBalancingQuanta newLBQ = new ProposedLoadBalancingQuanta(InputDataBuilder.C1, InputDataBuilder.OSS_ID,
                String.valueOf(newTotalUsers), InputDataBuilder.buildTargetCells(newUsers));
        enrichedPolicyOutputEvent.getPolicyOutputEvent().setLoadBalancingQuanta(newLBQ);
        return this;
    }

    public EnrichedPolicyOutputEventPrototypeLbq usersToMoveAtTargetTwo(final int number) {
        if (enrichedPolicyOutputEvent.getTargetCells().size() > 1) {
            final int totalUsers = Integer.parseInt(enrichedPolicyOutputEvent.getPolicyOutputEvent()
                    .getLoadBalancingQuanta().getSourceUsersMove());
            final int secondUsers = Integer.parseInt(enrichedPolicyOutputEvent.getTargetCells().get(1).getTargetUsersMove());
            final int newTotalUsers = totalUsers + number - secondUsers;
            final List<Integer> newUsers = new ArrayList<>();
            newUsers.add(Integer.parseInt(enrichedPolicyOutputEvent.getTargetCells().get(0).getTargetUsersMove()));
            newUsers.add(number);
            final ProposedLoadBalancingQuanta newLBQ = new ProposedLoadBalancingQuanta(InputDataBuilder.C1, InputDataBuilder.OSS_ID,
                    String.valueOf(newTotalUsers), InputDataBuilder.buildTargetCells(newUsers));
            enrichedPolicyOutputEvent.getPolicyOutputEvent().setLoadBalancingQuanta(newLBQ);
        }
        return this;
    }
}
