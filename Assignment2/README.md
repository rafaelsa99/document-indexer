# DocumentIndexer

## Assignment 2
Weighted (tf-idf) indexer and a ranked retrieval method, using the dataset from assignment 1.

The program contains:
- Extension of the indexer to apply term weighting.
- Two ranking methods:
  - Vector space ranking with tf-idf weights, using the lnc.ltc indexing schema.
  - BM25 ranking, using k1=1.2 and b=0.75 as default parameters
- Method to write the resulting index to file, using the following format (one term per line): term:idf;doc_id:term_weight;doc_id:term_weight;…

The evaluation of the retrieval engine is made by comparing both ranking methods:
- Processing the queries (file ‘queries.txt’) and retrieving the sorted results for each query.
- Using the relevance scores (file ‘queries.relevance.txt’), calculating the following evaluation and efficiency metrics, considering the top 10, 20 and 50 retrieved documents:
  - Mean Precision
  - Mean Recall
  - Mean F-measure
  - Mean Average Precision (MAP)
  - Mean Normalized Discounted Cumulative Gain (NDCG)
  - Query throughput
  - Median query latency
