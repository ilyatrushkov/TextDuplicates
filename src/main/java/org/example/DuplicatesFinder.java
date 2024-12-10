package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import info.debatty.java.lsh.MinHash;
import org.json.JSONObject;


public class DuplicatesFinder {
    private List<String> _documents = new ArrayList<String>();
    private List<String> _vocabulary = new ArrayList<String>();
    private List<boolean[]> _vectors = new ArrayList<boolean[]>();
    private int _k = 0; // size of shingle
    private MinHash _minHash;
    private List<int[]> _signatures = new ArrayList<int[]>();


    public DuplicatesFinder(int shingleSize, String filepath) {
        try {
            File file = new File(filepath);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject object = new JSONObject(line);
                String text = (String) object.get("text");

                _documents.add(text.replaceAll("[\\pP\\d]", ""));
            }
            br.close();
            fr.close();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
        _k = shingleSize;
    }

    public void lemmatizeDocuments() {
        for (int i = 0; i < _documents.size(); ++i) {
            String[] words = _documents.get(i).split("\\s+");
            for (int j = 0; j < words.length; ++j) {
                words[j] = WordLemmatizer.stem(words[j]);
            }
            _documents.set(i, String.join(" ", words));
        }
//        for (String document : _documents) {
//            System.out.println(document);
//        }
    }

    public void createVocabulary() {
        for (String document : _documents) {
            for (int i = 0; i < (document.length() - _k - 1); ++i) {
                _vocabulary.add(document.substring(i, i + _k));
            }
        }
        System.out.println(_vocabulary.toString());
        System.out.println("Vocabulary of " + _k + "-shingles was built. Size of vocabulary = " + _vocabulary.size());
    }

    private int getIndex(String shingle) {
        int index = 0;
        for (String current : _vocabulary) {
            if (shingle.equals(current)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    // one-hot encoding of documents into vectors, size of vector = size of vocabulary
    public void encodeDocumentsIntoVectors() {
        for (String document : _documents) {
            boolean[] vector = new boolean[_vocabulary.size()];
            for (int i = 0; i < _vocabulary.size(); ++i) {
                vector[i] = false;
            }
            for (int i = 0; i < (document.length() - _k - 1); ++i) {
                String shingle = document.substring(i, i + _k);
                int shingleIndex = getIndex(shingle);
                if (shingleIndex != -1) {
                    vector[shingleIndex] = true;
                }
            }
            _vectors.add(vector);
        }
    }

    public void countMinHashSignatures() {
        _minHash = new MinHash(0.1, _vocabulary.size());
        for (boolean[] vector : _vectors) {
            int[] signature = _minHash.signature(vector);
            _signatures.add(signature);
        }
    }

    public double[][] getJaccardSimilarityMatrix() {
        double[][] jaccardSimilarityMatrix = new double[_signatures.size()][_signatures.size()];
        for (int i = 0; i < _signatures.size(); ++i) {
            for (int j = 0; j < _signatures.size(); ++j) {
                jaccardSimilarityMatrix[i][j] = MinHash.jaccardIndex(_vectors.get(i), _vectors.get(j));
            }
        }
        return jaccardSimilarityMatrix;
    }

    public double[][] getSimilarityMatrix() {
        double[][] similarityMatrix = new double[_signatures.size()][_signatures.size()];
        for (int i = 0; i < _signatures.size(); ++i) {
            for (int j = 0; j < _signatures.size(); ++j) {
                similarityMatrix[i][j] = _minHash.similarity(_signatures.get(i), _signatures.get(j));
            }
        }
        return similarityMatrix;
    }
}
