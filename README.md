# DocumentIndexer

## Assignment 1
Simple document indexer, consisting of a corpus reader / document processor, tokenizer, and indexer.
The corpus for this assignment is available here: https://bit.ly/2Rg7gbX

The program contains:
- A corpus reader that iterates over the collection (corpus) of document and returns, in turn, the contents of each document. Only the title and abstract fields are considered and documents with an empty abstract are ignored.
- Two tokenizers:
  - A simple tokenizer that replaces all non-alphabetic characters by a space, lowercases tokens, splits on whitespace, and ignores all tokens with less than 3 characters.
  - An improved tokenizer that incorporates other tokenization decisions (e.g. how to deal with digits and characters such as ’, -, @, etc). Integration with the Porter stemmer and also a stopword filter.
- An indexing pipeline.

The corpus is indexed using each tokenizer above and answers are presented to the following questions:
- What was the total indexing time and how much memory (roughly) is required to index this collection?
- What is the vocabulary size?
- List of the ten first terms (in alphabetic order) that appear in only one document (document
frequency = 1).
- List of the ten terms with highest document frequency.
