/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2013  Paco Avila & Josep Llort
 *
 *  No bytes were intentionally harmed during the development of this application.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.module.db.stuff;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.reader.ReaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeDocumentVersion;

/**
 * Provides various helper operations for working with Lucene indexes.
 */

public class IndexHelper {
    private static Logger log = LoggerFactory.getLogger(IndexHelper.class);

    private EntityManager entityManager;

    public void checkIndexOnStartup() {
        //log.info("Observed event {1} from Thread {0}", Thread.currentThread().getName(), App.INIT_SUCCESS);

        // See if we need to rebuild the index during startup ...
        final FullTextEntityManager ftEm = Search
                .getFullTextEntityManager(entityManager);
        final SearchFactory searchFactory = ftEm.getSearchFactory();
        final ReaderProvider readerProvider = searchFactory.getReaderProvider();
        final IndexReader reader = readerProvider.openReader(searchFactory
                .getDirectoryProviders(NodeDocumentVersion.class)[0]);
        int maxDoc = 0;

        try {
            maxDoc = reader.maxDoc();
        } finally {
            readerProvider.closeReader(reader);
        }

        if (maxDoc == 0) {
            log.warn("No objects indexed ... rebuilding Lucene search index from database ...");
            long _exit = 0L;
            final long _entr = System.currentTimeMillis();

            try {
                final int docs = doRebuildIndex();
                _exit = System.currentTimeMillis();
                log.info("Took " + (_exit - _entr)
                        + " (ms) to re-build the index containing " + docs
                        + " documents.");
            } catch (final Exception exc) {
                if (exc instanceof RuntimeException) {
                    throw (RuntimeException) exc;
                } else {
                    throw new RuntimeException(exc);
                }
            }

            // build the spell checker index off of the HS index.
            buildSpellCheckerIndex(searchFactory);
        }
    }

    public void updateSpellCheckerIndex(final NodeDocumentVersion nDocVer) {
        log.info("Observed Wine added/updated event for {1} from Thread {0}",
                Thread.currentThread().getName(), String.valueOf(nDocVer));
        final String text = nDocVer != null ? nDocVer.getText() : null;

        if (text != null) {
            Dictionary dictionary = null;

            try {
                final FullTextEntityManager ftEm = (FullTextEntityManager) entityManager;
                final SearchFactory searchFactory = ftEm.getSearchFactory();
                dictionary = new SetDictionary(text,
                        searchFactory.getAnalyzer("wine_en"));
            } catch (final IOException ioExc) {
                log.error("Failed to analyze dictionary text {0} from Wine {1} to update spell checker due to: {2}"
                        + text + nDocVer.getUuid() + ioExc.toString());
            }

            if (dictionary != null) {
                Directory dir = null;
                // only allow one thread to update the index at a time ...
                // the Dictionary is pre-computed, so it should happen quickly
                // ...
                // this synchronized approach only works because this component
                // is application-scoped
                synchronized (this) {
                    try {
                        dir = FSDirectory.open(new File(
                                "lucene_index/spellcheck"));
                        final SpellChecker spell = new SpellChecker(dir);
                        spell.indexDictionary(dictionary);
                        spell.close();
                        log.info("Successfully updated the spell checker index after Document added/updated.");
                    } catch (final Exception exc) {
                        log.error("Failed to update the spell checker index!",
                                exc);
                    } finally {
                        if (dir != null) {
                            try {
                                dir.close();
                            } catch (final Exception zzz) {
                            }
                        }
                    }
                }
            }
        }
    }

    // used to feed updates to the spell checker index
    class SetDictionary implements Dictionary {
        private Set<String> wordSet;

        @SuppressWarnings("unused")
        SetDictionary(final String words, final Analyzer analyzer)
                throws IOException {
            wordSet = new HashSet<String>();
            if (words != null) {
                analyzer.tokenStream(NodeDocument.TEXT_FIELD, new StringReader(
                        words));
                new Token();

                //while ((nextToken = tokenStream.next(reusableToken)) != null) {
                //String term = nextToken.term();
                //if (term != null) {
                //wordSet.add(term);
                //}
                //}
            }
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Iterator getWordsIterator() {
            return wordSet.iterator();
        }
    }

    protected void buildSpellCheckerIndex(final SearchFactory searchFactory) {
        IndexReader reader = null;
        Directory dir = null;
        final long _entr = System.currentTimeMillis();
        final File spellCheckIndexDir = new File("lucene_index/spellcheck");
        log.info("Building SpellChecker index in {0}",
                spellCheckIndexDir.getAbsolutePath());
        final ReaderProvider readerProvider = searchFactory.getReaderProvider();

        try {
            reader = readerProvider.openReader(searchFactory
                    .getDirectoryProviders(NodeDocumentVersion.class)[0]);
            dir = FSDirectory.open(spellCheckIndexDir);
            final SpellChecker spell = new SpellChecker(dir);
            spell.clearIndex();
            spell.indexDictionary(new LuceneDictionary(reader,
                    NodeDocument.TEXT_FIELD));
            spell.close();
            dir.close();
            dir = null;
            final long _exit = System.currentTimeMillis();
            log.info("Took {1} (ms) to build SpellChecker index in {0}",
                    spellCheckIndexDir.getAbsolutePath(),
                    String.valueOf(_exit - _entr));
        } catch (final Exception exc) {
            log.error("Failed to build spell checker index!", exc);
        } finally {
            if (dir != null) {
                try {
                    dir.close();
                } catch (final Exception zzz) {
                }
            }
            if (reader != null) {
                readerProvider.closeReader(reader);
            }
        }
    }

    protected int doRebuildIndex() throws Exception {
        final FullTextSession fullTextSession = (FullTextSession) entityManager
                .getDelegate();
        fullTextSession.setFlushMode(org.hibernate.FlushMode.MANUAL);
        fullTextSession.setCacheMode(org.hibernate.CacheMode.IGNORE);
        fullTextSession.purgeAll(NodeDocumentVersion.class);
        fullTextSession.getSearchFactory().optimize(NodeDocumentVersion.class);

        final String query = "select ndv from NodeDocumentVersion ndv";
        final ScrollableResults cursor = fullTextSession.createQuery(query)
                .scroll();
        cursor.last();
        final int count = cursor.getRowNumber() + 1;
        log.warn("Re-building Wine index for " + count + " objects.");

        if (count > 0) {
            final int batchSize = 300;
            cursor.first(); // Reset to first result row
            int i = 0;

            while (true) {
                fullTextSession.index(cursor.get(0));

                if (++i % batchSize == 0) {
                    fullTextSession.flushToIndexes();
                    fullTextSession.clear(); // Clear persistence context for each batch
                    log.info("Flushed index update " + i + " from Thread "
                            + Thread.currentThread().getName());
                }

                if (cursor.isLast()) {
                    break;
                }

                cursor.next();
            }
        }

        cursor.close();
        fullTextSession.flushToIndexes();
        fullTextSession.clear(); // Clear persistence context for each batch
        fullTextSession.getSearchFactory().optimize(NodeDocumentVersion.class);

        return count;
    }
}