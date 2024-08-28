/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.cm.data.stores;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.cm.data.domain.Cell;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologyObjectId;
import com.ericsson.oss.services.sonom.flm.cm.data.domain.TopologySector;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmCellGroupRetriever;
import com.ericsson.oss.services.sonom.flm.cm.data.retrieval.CmSectorCellRetriever;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmAlgorithmException;
import com.ericsson.oss.services.sonom.flm.service.api.exceptions.FlmServiceExceptionCode;
import com.ericsson.oss.services.sonom.flm.service.api.executions.Execution;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Group;
import com.ericsson.oss.services.sonom.flm.service.api.util.LoggingFormatter;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Store to fetch and cache {@link Cell}'s and {@link TopologySector}'s.
 * <p>
 * For performance reasons this class will hold only those {@link Cell}s that are added by
 * the current {@link Execution}`s {@code inclusionList} on {@link Cell} level if the
 * {@code inclusionList} is not empty. If the {@code inclusionList} is empty, then it will
 * fetch and cache all {@link Cell}s on {@link Cell} level. On {@link TopologySector}
 * level we need to store two caches since some of the stages require full view on the
 * given {@link TopologySector}, meaning we cannot erase {@code non-included}
 * {@link Cell}s.
 * <p>
 * If want to work with full {@link TopologySector}, then use
 * {@link CmSectorCellStore#fullSectors}.
 * <p>
 * If want to work with {@link TopologySector} on which {@code inclusionList} is applied,
 * then use {@link CmSectorCellStore#sectorsWithInclusionListCells}.
 * <p>
 * As of now only {@code LBDAR} requires {@link CmSectorCellStore#fullSectors},other
 * stages should  work with {@link CmSectorCellStore#sectorsWithInclusionListCells}.
 * <p>
 * <strong>NOTE:</strong> When {@code inclusionList} is applied, then {@link Cell}s under
 * {@link TopologySector} instances are shared between
 * {@link CmSectorCellStore#fullSectors} and {@link CmSectorCellStore#sectorsWithInclusionListCells}
 * thus changing {@link Cell}s inner representation might cause side-effects on
 * {@code FLM Algorithm} execution.
 */
public class CmSectorCellStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmSectorCellStore.class);

    /**
     * Holds intact {@link TopologySector}s.
     */
    private final Map<Long, TopologySector> fullSectors = Maps.newHashMap();
    /**
     * Holds {@link TopologySector}s on which {@code inclusionList} is applied on.
     */
    private final Map<Long, TopologySector> sectorsWithInclusionListCells = Maps.newHashMap();
    private final Map<TopologyObjectId, Cell> cells = Maps.newHashMap();
    private final CmSectorCellRetriever cmSectorCellRetriever;
    private final CmCellGroupRetriever cmCellGroupRetriever;
    private final Set<CellIdentifier> allIncludedCellIds = new HashSet<>();
    private boolean isInclusionListApplied;

    /**
     * Initializes the store (including making the call to CM Service).
     * <p>
     * Read more on {@link CmSectorCellStore#CmSectorCellStore(CmSectorCellRetriever, CmCellGroupRetriever, Execution)}.
     * @param execution current execution
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public CmSectorCellStore(final Execution execution) throws FlmAlgorithmException {
        this(new CmSectorCellRetriever(), new CmCellGroupRetriever(), execution);
    }

    /**
     * Fetches all {@link TopologySector} from {@code CM service}.
     * <p>
     * If {@code inclusionList} contains any {@link Group}s, then it is being applied.
     * @param cmSectorCellRetriever retriever of {@link TopologySector}s
     * @param cmCellGroupRetriever retriever of {@link Cell}s
     * @param execution current {@link Execution}
     * @throws FlmAlgorithmException
     *          if there are no {@link Cell}s after {@code inclusionList} is applied
     */
    public CmSectorCellStore(final CmSectorCellRetriever cmSectorCellRetriever,
                             final CmCellGroupRetriever cmCellGroupRetriever,
                             final Execution execution) throws FlmAlgorithmException {
        this.cmCellGroupRetriever = cmCellGroupRetriever;
        this.cmSectorCellRetriever = cmSectorCellRetriever;

        collectSectorRelatedData(sector -> fullSectors.put(sector.getSectorId(), sector),
                                 sector -> sector.getAssociatedCells()
                                                 .forEach(cell -> cells.put(cell.getTopologyObjectId(), cell)));

        if (isNotEmpty(execution.getInclusionList())) {
            final Stopwatch stopWatch = Stopwatch.createStarted();
            final Collection<Cell> includedCells = collectIncludedCells(execution);
            LOGGER.info("Collecting included cells took {}.", stopWatch);

            validateIncludedCells(includedCells);
            LOGGER.info("Collected {} cell(s) from the inclusion list.", includedCells.size());

            allIncludedCellIds.addAll(getCellIdsFromCells(includedCells));

            applyInclusionList(includedCells, execution);
            isInclusionListApplied = true;
        } else {
            LOGGER.info("Inclusion list is not applied.");
        }
    }

    /**
     * Checks if the {@code inclusionList} is applied.
     * @return {@code true} if {@code inclusionList} is applied otherwise {@code false}
     */
    public boolean isInclusionListApplied() {
        return isInclusionListApplied;
    }

    /**
     * Gets a {@link Collection} of {@link TopologySector}s.
     * <p>
     * <strong>NOTE</strong>: {@code inclusionList} is not applied on
     * {@link CmSectorCellStore#fullSectors}.
     * @return a {@link Collection} of {@link TopologySector}s populated with associated {@link Cell}s.
     */
    public Collection<TopologySector> getFullSectors() {
        return Collections.unmodifiableCollection(fullSectors.values());
    }

    /**
     * Gets a {@link TopologySector} for the given sectorId.
     * <p>
     * <strong>NOTE</strong>: {@code inclusionList} is not applied on
     * {@link CmSectorCellStore#fullSectors}.
     * @param sectorId an id of a {@link TopologySector} which associated {@link Cell}s would like to get
     * @return  a {@link TopologySector} populated with associated {@link Cell}s if {@link CmSectorCellStore#fullSectors} contain such
     *          {@link TopologySector} otherwise null
     */
    public TopologySector getFullSector(final long sectorId) {
        return fullSectors.getOrDefault(sectorId, null);
    }

    /**
     * Gets a {@link Collection} of {@link TopologySector}s.
     * <p>
     * <strong>NOTE</strong>: {@code inclusionList} is applied on
     * {@link CmSectorCellStore#sectorsWithInclusionListCells} thus they only contain
     * {@link Cell}s that are both in {@code inclusionList} and in
     * {@link TopologySector}`s associated {@link Cell}s. If {@code inclusionList} is not
     * applied then this will return {@link CmSectorCellStore#fullSectors}.
     * <p>
     * For more reference read {@link TopologySector#applyInclusionList(Collection)}.
     * @return  a {@link Collection} of {@code inclusionList} applied {@link TopologySector}s if {@code inclusionList} is applied otherwise
     *          {@link CmSectorCellStore#fullSectors}
     */
    public Collection<TopologySector> getSectorsWithInclusionListCells() {
        return isInclusionListApplied
                ? Collections.unmodifiableCollection(sectorsWithInclusionListCells.values())
                : getFullSectors();
    }

    /**
     * Gets a {@link TopologySector} for the given sectorId.
     * <p>
     * <strong>NOTE</strong>: {@code inclusionList} is applied on
     * {@link CmSectorCellStore#sectorsWithInclusionListCells} thus {@link TopologySector}s
     * only contain {@link Cell}s that are both in {@code inclusionList} and in
     * {@link TopologySector}`s associated {@link Cell}s. If {@code inclusionList} is not
     * applied then this will return {@link CmSectorCellStore#getFullSector(long)}.
     * <p>
     * For more reference read {@link TopologySector#applyInclusionList(Collection)}.
     * @param sectorId an id of a {@link TopologySector} which associated {@link Cell}s would like to get
     * @return  a {@link TopologySector} populated with associated cells if {@link CmSectorCellStore#sectorsWithInclusionListCells} contain such
     *          {@link TopologySector} otherwise null. If {@code inclusionList} is not applied then it uses
     *          {@link CmSectorCellStore#getFullSector(long)}
     */
    public TopologySector getSectorWithInclusionListCells(final long sectorId) {
        return isInclusionListApplied
                ? sectorsWithInclusionListCells.getOrDefault(sectorId, null)
                : getFullSector(sectorId);
    }

    /**
     * Gets the list of included {@link Cell}s.
     * <p>
     * If want to get all mediated {@link Cell}s use
     * {@link CmSectorCellStore#fetchAllMediatedCells()} instead.
     * @return A list of all associated {@link Cell}s from each {@link TopologySector}
     */
    public Collection<Cell> getCells() {
        return Collections.unmodifiableCollection(cells.values());
    }

    /**
     * Method to return all mediated {@link Cell}s.
     * <p>
     * If {@code inclusionList} is not applied then it returns the local cache of
     * {@link CmSectorCellStore#cells} - as it contains all {@link Cell}s - otherwise
     * it fetches all mediated {@link Cell}s from {@code CM Service} and returns those.
     * <p>
     * <strong>NOTE</strong>: If it has to fetch all mediated {@link Cell}s from
     * {@code CM Store} be careful calling this method repeatedly as makes REST calls
     * toward the {@code CM Store}.
     * @return all mediated {@link Cell}s
     */
    public Collection<Cell> getAllMediatedCells() {
        return isInclusionListApplied
                ? fetchAllMediatedCells()
                : getCells();
    }

    /**
     * Gets the {@link Set} of {@link CellIdentifier}s of included {@link Cell}s.
     * <p>
     * This consists of all {@link Cell}s in the inclusion list before any filtering of cells without sectors or multi-sector cells.
     * If want to get all {@link Cell}s (after this filtering has been applied) use
     * {@link CmSectorCellStore#getAllMediatedCells} instead.
     * @return A {@link Set} of {@link CellIdentifier}s of all associated {@link Cell}s in the inclusion list
     */
    public Set<CellIdentifier> getAllIncludedCellIds() {
        return isInclusionListApplied
                ? new HashSet<>(allIncludedCellIds)
                : getCellIdsFromCells(getCells());
    }

    /**
     * It returns a cell for the given fdn and ossId.
     * @param fdn an fdn of a {@link Cell}
     * @param ossId an ossId of a {@link Cell}
     * @return it returns a {@link Cell} object if found, null otherwise
     */
    public Cell getCellForCellFdn(final String fdn, final int ossId) {
        return cells.getOrDefault(new TopologyObjectId(fdn, ossId), null);
    }

    private Set<CellIdentifier> getCellIdsFromCells(final Collection<Cell> cells) {
        return cells.stream()
                .map(CmSectorCellStore::getCellIdentifier)
                .collect(Collectors.toSet());
    }

    private static CellIdentifier getCellIdentifier(final Cell cell) {
        return new CellIdentifier(cell.getOssId(), cell.getFdn());
    }

    private Collection<Cell> fetchAllMediatedCells() {
        final Collection<Cell> allMediatedCells = Lists.newArrayList();

        final Stopwatch stopwatch = Stopwatch.createStarted();
        collectSectorRelatedData(sector -> allMediatedCells.addAll(sector.getAssociatedCells()));
        LOGGER.info("Fetching all mediated cells took {}.", stopwatch);

        return allMediatedCells;
    }

    /**
     * For each {@link TopologySector} from {@link CmSectorCellStore#fullSectors} it
     * creates a {@code shallow-copy} on which the
     * {@link TopologySector#applyInclusionList(Collection)} is applied.
     * Created copies than saved into {@link CmSectorCellStore#sectorsWithInclusionListCells} if they
     * are not empty.
     * <p>
     * The {@link CmSectorCellStore#cells} cache will contain the
     * intersection of the map`s values and the {@code inclusionList}.
     * @param includedCells {@link Cell}s contained in by the {@code inclusionList}.
     * @param execution current {@link Execution}
     */
    private void applyInclusionList(final Collection<Cell> includedCells, final Execution execution) {
        fullSectors.forEach((id, originalSector) -> {
            final TopologySector copyOfOriginalSector = TopologySector.newInstance(originalSector);

            copyOfOriginalSector.applyInclusionList(includedCells);

            if (copyOfOriginalSector.isAssociatedCellsEmpty()) {
                LoggingFormatter.logFilteredSector(execution.getId(),
                                                   String.valueOf(copyOfOriginalSector.getSectorId()),
                                                   "Once the inclusion list is applied, all cells under the sector " +
                                                           "are out of optimization scope.");
            } else {
                sectorsWithInclusionListCells.put(copyOfOriginalSector.getSectorId(), copyOfOriginalSector);
            }
        });

        cells.values()
             .retainAll(includedCells);
    }

    /**
     * Fetches included {@link Cell}s from {@code CM Service} based on the
     * {@code inclusionList}.
     * @return {@link Collection} of included {@link Cell}s
     */
    private Collection<Cell> collectIncludedCells(final Execution execution) {
        return execution.getInclusionList()
                        .stream()
                        .map(Group::getName)
                        .map(groupName -> ExecutionWrapper.of(groupName, cmCellGroupRetriever.retrieveGroupEvaluation(groupName, execution.getId())))
                        .map(sneakyCheck(ExecutionWrapper::executeFetch))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(HashSet::new));
    }

    @SafeVarargs
    private final void collectSectorRelatedData(final Consumer<? super TopologySector>... collectors) {
        cmSectorCellRetriever.retrieve()
                             .forEach(sector -> Stream.of(collectors)
                                                      .forEach(collector -> collector.accept(sector)));
    }

    private static <T> boolean isNotEmpty(final Collection<T> collection) {
        return Objects.nonNull(collection) && !collection.isEmpty();
    }

    private static void validateIncludedCells(final Collection<Cell> includedCells) throws FlmAlgorithmException {
        if (includedCells.isEmpty()) {
            LOGGER.warn("Execution stopped as no cells were found in the specified inclusion list.");
            throw new FlmAlgorithmException(FlmServiceExceptionCode.NO_CELLS_FOUND_FOR_INCLUSION);
        }
    }

    @SuppressWarnings({ "squid:S2221",
                        "squid:S00112",
                        "PMD.AvoidThrowingRawExceptionTypes" })
    private static <T, R, E extends Exception> Function<T, R> sneakyCheck(final CheckedExceptionFunction<? super T, ? extends R, E> function) {
        return argument -> {
            try {
                return function.apply(argument);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Interface to wrap method throwing checked exception.
     * @param <T> parameter type
     * @param <R> return type
     * @param <E> exception type
     */
    @FunctionalInterface
    public interface CheckedExceptionFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    private static final class ExecutionWrapper {
        private final String groupName;
        private final Future<? extends List<Cell>> future;

        private ExecutionWrapper(final String groupName, final Future<? extends List<Cell>> future) {
            this.groupName = groupName;
            this.future = future;
        }

        private static ExecutionWrapper of(final String groupName, final Future<? extends List<Cell>> future) {
            return new ExecutionWrapper(groupName, future);
        }

        private List<Cell> executeFetch() throws ExecutionException, InterruptedException {
            final List<Cell> cells = future.get();

            if (cells.isEmpty()) {
                LOGGER.warn("Group {} in the inclusion list is empty or does not contains cells.", groupName);
            }

            return cells;
        }

    }
}
