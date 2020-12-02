# DocumentIndexer

## Assignment 3

Continuation of the extension of the indexing and retrieval methods.<br>
Using the latest dataset available here: ai2-semanticscholar-cord-19.s3-us-west-2.amazonaws.com/historical_releases.html

The program contains:
- Implementation of the SPIMI approach in the indexing method.
- Extension of the indexer to store term positions.
- Method to write the resulting index to file, using the following format (one term per line): term:idf;doc_id:term_weight:pos1,pos2,pos3,…;doc_id:term_weight:pos1,pos2,pos3,…
- Extension of the ranked retrieval method to account for query term proximity, that is, considering the shortest text span that includes all (or most, or several) query words and use this information for adapting the ranking score.

The evaluation of the retrieval methods is made, using the same queries as in Assignment 2, with and without query term proximity boost.
