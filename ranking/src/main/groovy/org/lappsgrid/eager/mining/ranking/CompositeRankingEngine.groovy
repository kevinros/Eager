package org.lappsgrid.eager.mining.ranking

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.eager.mining.ranking.model.Document
import org.lappsgrid.eager.mining.scoring.ScoringAlgorithm
import org.lappsgrid.eager.mining.scoring.WeightedAlgorithm

/**
 *  Scores documents by combining several weighted RankingEngines.
 */
@Slf4j("logger")
class CompositeRankingEngine {

    Map<String,RankingEngine> engines = [:]

    Map<String,Closure> fieldExtractors = [
            title: { it.title },
            abstract: { it.articleAbstract },
            intro: { it.intro }
    ]

    CompositeRankingEngine(Map params) {
        logger.info("Initializing engine: {}", params.size())
        params.each { String key, String value ->
            logger.info("key: {} value: {}", key, value)
            Triple triple = new Triple(key)
            if (triple.control == 'checkbox') {
                logger.trace("processing checkbox")
                ScoringAlgorithm algorithm = AlgorithmRegistry.get(value)
                if (algorithm != null) {
                    RankingEngine engine = engines[triple.section]
                    if (engine == null) {
                        logger.trace("adding ranking engine for {}", triple.section)
                        engine = new RankingEngine()
                        engine.section = triple.section
                        engine.field = fieldExtractors[triple.section]
                        engines.put(triple.section, engine)
                    }
                    String weightKey = key.replace("checkbox", "weight")
                    String weight = params.get(weightKey)
                    if (weight) {
                        logger.debug("Adding algorithm {}:{}", algorithm.abbrev(), weight)
                        engine.add(new WeightedAlgorithm(algorithm, weight as float))
                    }
                    else {
                        // TODO Log this as a programming error in the form.
                        logger.warn("No weight specified for {}", weightKey)
                    }
                }
                else {
                    // TODO Log this as a programming error in the form.
                    logger.warn("No algorithm for {}", value)
                }
            }
            else if (triple.id == 'x') {
                RankingEngine engine = engines[triple.section]
                if (engine) {
                    logger.info("weight for {} is {}", triple.section, value)
                    engine.weight = value as float
                }
                else {
                    // TODO Log this as a programming error in the form.
                    logger.warn("No ranking engine for {}", triple.section)
                }
            }
            else {
                logger.debug("Ignoring triple {}:{}", triple.section, triple.control)
            }

        }
        logger.info("Initialized {} engines", engines.size())
        engines.each { String section, RankingEngine engine ->
            logger.debug(engine.section)
            engine.algorithms.each { WeightedAlgorithm algorithm ->
                logger.debug("{} : {}", algorithm.abbrev(), algorithm.weight)
            }
        }
    }

    List<Document> rank(Query query, List<Document> documents) {
        engines.each { String key, RankingEngine engine ->
            logger.info("Ranking {}", key)
            engine.rank(query, documents)
        }
        logger.debug("Sorting {} documents.", documents.size())
        return documents.sort { a,b -> b.score <=> a.score }
    }

    class Triple {
        String section
        String control
        String id

        Triple(String input) {
            String[] parts = input.tokenize("-")
            if (parts.length != 3) {
                section = control = id = 'unknown'
                return
            }
            section = parts[0]
            control = parts[1]
            id = parts[2]
        }

        String toString() {
            return "$section-$control-$id"
        }

    }
}
