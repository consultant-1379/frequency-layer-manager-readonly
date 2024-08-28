/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 * *****************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar;

import static com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElementStatus.PENDING_APPROVAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.cm.service.change.api.ChangeElement;
import com.ericsson.oss.services.sonom.cm.service.change.api.ParameterChanges;
import com.ericsson.oss.services.sonom.cm.service.change.api.ProposedChange;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.IdleModePrioAtRelease;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmNodeObjectsStore;
import com.ericsson.oss.services.sonom.flm.cm.data.stores.CmSectorCellStore;
import com.ericsson.oss.services.sonom.flm.loadbalancing.lbdar.exceptions.LbdarReversionException;

/**
 * This class implement a {@link ReversionElementCalculator} that calculates a reversion ChangeElement.
 */
public class ReversionElementCalculatorImpl implements ReversionElementCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReversionElementCalculatorImpl.class);
    private static final String CREATE = "CREATE";
    private static final String DELETE = "DELETE";
    private static final String MODIFY = "MODIFY";

    private final CmSectorCellStore sectorCellStore;
    private final CmNodeObjectsStore nodeObjectsStore;

    ReversionElementCalculatorImpl(final CmSectorCellStore sectorCellStore, final CmNodeObjectsStore nodeObjectsStore) {
        this.sectorCellStore = sectorCellStore;
        this.nodeObjectsStore = nodeObjectsStore;
    }

    /**
     * Calculates the reversions for all the ProposedChanges present in the optimizationElement passed.
     * Then creates and returns a Reversion ChangeElement containing these reversed ProposedChanges.
     *
     * @param optimizationElement instance of {@link ChangeElement}
     * @return returns a {@link ChangeElement} or null if was not able to create the ChangeElement
     */
    @Override
    public ChangeElement calculateReversionElement(final ChangeElement optimizationElement) throws LbdarReversionException {
        final int numberOfChanges = optimizationElement.getProposedChanges().size();
        List<ProposedChange> reversedChanges = new ArrayList<>(numberOfChanges);
        final ListIterator<ProposedChange> listIterator = optimizationElement.getProposedChanges().listIterator(numberOfChanges);
        while (listIterator.hasPrevious()) {
            reversedChanges.add(this.getReversedProposedChange(listIterator.previous()));
        }
        //Remove any null changes
        reversedChanges = reversedChanges.stream().filter(Objects::nonNull).collect(Collectors.toList());

        final ChangeElement reversionElement = ChangeElement.create(optimizationElement.getExecutionId(),
                optimizationElement.getSourceOfChange(),
                PENDING_APPROVAL.toString(),
                optimizationElement.getSourceOfChange(),
                System.currentTimeMillis(),
                reversedChanges,
                optimizationElement.getChangeId());
        reversionElement.setChangeType(ChangeElement.ChangeType.REVERSION);

        return reversionElement;
    }

    private ProposedChange getReversedProposedChange(final ProposedChange optimization) throws LbdarReversionException {
        switch (optimization.getOperationType()) {
            case DELETE:
                return this.reverseProposedChangeForDelete(optimization);
            case MODIFY:
                return this.reverseProposedChangeForModify(optimization);
            case CREATE:
                return this.reverseProposedChangeForCreate(optimization);
            default:
                throw new LbdarReversionException("Reversion element could not be calculated, invalid operation type found for Optimization, " +
                        optimization.getOperationType());
        }
    }

    private ProposedChange reverseProposedChangeForDelete(final ProposedChange proposedChange) throws LbdarReversionException {
        final String fdn = proposedChange.getFdn();
        final int ossId = proposedChange.getOssId();
        final IdleModePrioAtRelease idleModePrioAtRelease = nodeObjectsStore.getIdleModePrioAtRelease(fdn, ossId);
        if (idleModePrioAtRelease == null) {
            final String msg = String.format("Failed to get IdleModePrioAtRelease for Fdn:%s, OssId:%d", fdn, ossId);
            LOGGER.warn(msg);
            throw new LbdarReversionException(msg);
        }
        final ProposedChange reversedChange = copyProposedChange(proposedChange,
                ParameterChangesBuilder.buildProfileParameterChanges(idleModePrioAtRelease));
        reversedChange.setOperationType(CREATE);
        reversedChange.setGuid(-1L);
        return reversedChange;
    }

    private ProposedChange reverseProposedChangeForModify(final ProposedChange proposedChange) throws LbdarReversionException {
        final String fdn = proposedChange.getFdn();
        final int ossId = proposedChange.getOssId();
        final Cell cell = sectorCellStore.getCellForCellFdn(fdn, ossId);
        if (cell == null) {
            final String msg = String.format("Failed to get Cell for Fdn:%s, OssId:%d", fdn, ossId);
            LOGGER.warn(msg);
            throw new LbdarReversionException(msg);
        }
        return copyProposedChange(proposedChange, ParameterChangesBuilder.buildCellParameterChanges(cell.getIdleModePrioAtReleaseRef()));
    }

    private ProposedChange reverseProposedChangeForCreate(final ProposedChange proposedChange) {
        final ProposedChange reversedChange = copyProposedChange(proposedChange, Collections.emptyList());
        reversedChange.setOperationType(DELETE);
        return reversedChange;
    }

    private ProposedChange copyProposedChange(final ProposedChange proposedChange, final List<ParameterChanges> parameterChanges) {
        return new ProposedChange(proposedChange.getOssId(),
                proposedChange.getNodeType(),
                proposedChange.getObjectType(),
                proposedChange.getGuid(),
                proposedChange.getFdn(),
                proposedChange.getOperationType(),
                parameterChanges);
    }
}