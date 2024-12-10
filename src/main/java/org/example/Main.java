package org.example;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String filename = "";
        try {
            System.out.println("Enter name of file with documents (each document should consists of JSON objects with 'text' key):");
            filename = reader.readLine();
        }
        catch (Exception _) {}

        int k = 0;
        try {
            System.out.println("Enter size of shingles:");
            k = Integer.parseInt(reader.readLine());
        }
        catch (Exception _) {}

        DuplicatesFinder duplicatesFinder = new DuplicatesFinder(k, filename);
        duplicatesFinder.lemmatizeDocuments();
        duplicatesFinder.createVocabulary();
        duplicatesFinder.encodeDocumentsIntoVectors();
        duplicatesFinder.countMinHashSignatures();
    }
}