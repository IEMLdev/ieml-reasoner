package reasoner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Solution;
import io.github.vletard.analogy.tuple.TupleEquation;
import parser.IEMLUnit;
import parser.IncompatibleSolutionException;
import parser.JSONStructureException;
import parser.Lexeme;
import parser.MissingTranslationException;
import parser.Morpheme;
import parser.ParseException;
import parser.Polymorpheme;
import parser.StyleException;
import parser.Word;
import parser.Writable;
import parser.WritableBuilder;
import util.Quadruple;
import util.Triple;

public class Reasoner<T extends Writable> {
  public static final String DEFAULT_BASENAME = "/tmp/ieml-reasoner";

  private final Dictionary dict;
  private final ArrayList<T> database;
  private final WritableBuilder<T> builder;
  String verifyingStats, solvingStats;
  private String selectedLanguage;
  private final String typeName;
  private final HashSet<String> parseFailed;

  /**
   * Initializes a Reasoner object.
   * @param dict {@link Dictionary} object providing IEML-NL translations.
   * @param usls the set of usls to be parsed as T objects.
   * @param builder the builder instance to be used on analogical equations output.
   * @param translationLanguage the selected default language for translations.
   */
  public Reasoner(Dictionary dict, Set<String> usls, WritableBuilder<T> builder, String translationLanguage) {
    this.dict = dict;
    this.database = new ArrayList<T>();
    this.builder = builder;
    this.parseFailed = new HashSet<String>();
    this.selectedLanguage = translationLanguage;

    for (String usl: usls) {
      try {
        T parse = builder.parse(usl);
        this.database.add(parse);
      } catch (ParseException e) {
        this.parseFailed.add(usl);
      }
    }

    if (this.database.size() > 0)
      this.typeName = this.database.get(0).getClass().getSimpleName();
    else
      this.typeName = "Unknown";
  }
  
  /**
   * Retrieves the set of usls that could not be parsed as the current generic type.
   * @return the set of these usls.
   */
  public Set<String> getFailedParses() {
    return Collections.unmodifiableSet(this.parseFailed);
  }

  private Iterable<T> solveEquation(T ieml1, T ieml2, T ieml3) {
    TupleEquation<IEMLUnit, T> e = new TupleEquation<IEMLUnit, T>(ieml1, ieml2, ieml3, this.builder);
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          Iterator<? extends Solution<T>> it = e.uniqueSolutions().iterator();

          @Override
          public boolean hasNext() {
            return this.it.hasNext();
          }

          @Override
          public T next() {
            if (this.hasNext())
              return this.it.next().getContent();
            else
              throw new NoSuchElementException();
          }
        };
      }
    };
  }

  public Iterable<String> displayProportions() {
    return new Iterable<String>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private int i = 0, j = 1, k = 2, l = -1;
          private Quadruple<Integer> nextValidProportion = null;

          @Override
          public boolean hasNext() {
            if (this.nextValidProportion == null && this.i < Reasoner.this.database.size() && this.j < Reasoner.this.database.size() && this.k < Reasoner.this.database.size() && this.l < Reasoner.this.database.size())
              this.nextValidProportion = this.computeNext();
            return this.nextValidProportion != null;
          }

          @Override
          public String next() {
            if (this.hasNext()) {
              Quadruple<Integer> t = this.nextValidProportion;
              this.nextValidProportion = null;
              String output = "";
              try {
                output += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(t.getFirst()).getUSL()).get("fr") + " : ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(t.getSecond()).getUSL()).get("fr") + " :: ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(t.getThird()).getUSL()).get("fr") + " : ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(t.getFourth()).getUSL()).get("fr") + "\n";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              output += Reasoner.this.database.get(t.getFirst()).getUSL() + " : ";
              output += Reasoner.this.database.get(t.getSecond()).getUSL() + " :: ";
              output += Reasoner.this.database.get(t.getThird()).getUSL() + " : ";
              output += Reasoner.this.database.get(t.getFourth()).getUSL();
              return output;
            }
            else
              throw new NoSuchElementException();
          }

          private Quadruple<Integer> computeNext() {
            this.l ++;
            while (this.i < Reasoner.this.database.size()) {
              T wi = Reasoner.this.database.get(this.i);
              if (this.j >= Reasoner.this.database.size())
                this.j = this.i + 1;
              while (this.j < Reasoner.this.database.size()) {
                T wj = Reasoner.this.database.get(this.j);
                if (this.k >= Reasoner.this.database.size())
                  this.k = this.j + 1;
                while (this.k < Reasoner.this.database.size()) {
                  T wk = Reasoner.this.database.get(this.k);
                  assert(!(this.i == this.j || this.i == this.k));
                  if (this.l >= Reasoner.this.database.size())
                    this.l = 0;
                  while (this.l < Reasoner.this.database.size()) {
                    T wl = Reasoner.this.database.get(this.l);
                    if (new DefaultProportion<T>(wi, wj, wk, wl).isValid())
                      return new Quadruple<Integer>(i, j, k, l);
                    this.l ++;
                  }
                  this.k ++;
                }
                this.j ++;
              }
              this.i ++;
            }
            return null;
          }
        };
      }
    };
  }

  public Iterable<String> displayEquations() {
    return new Iterable<String>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private Iterator<Triple<Integer>> indices = Reasoner.this.computeEquations().iterator();
          private Iterator<T> solutions = Collections.emptyIterator();
          private Triple<Integer> currentIndices;

          @Override
          public boolean hasNext() {
            if (this.solutions.hasNext())
              return true;
            else if (this.indices.hasNext()) {
              Triple<Integer> t = this.indices.next();
              this.currentIndices = t;
              this.solutions = Reasoner.this.solveEquation(Reasoner.this.database.get(t.getFirst()), Reasoner.this.database.get(t.getSecond()), Reasoner.this.database.get(t.getThird())).iterator();
              assert(this.solutions.hasNext());
              return true;
            }
            else
              return false;
          }

          @Override
          public String next() {
            if (this.hasNext()) {
              T result = this.solutions.next();
              String indices = "", translations = "", usls = "";
              indices = this.currentIndices.getFirst() + "\t" + this.currentIndices.getSecond() + "\t" + this.currentIndices.getThird();
              for (Integer i: this.currentIndices) {
                try {
                  translations += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(i).getUSL()).get("fr") + "\t";
                } catch (MissingTranslationException e) {
                  translations += "<no translation>\t";
                }
              }
              try {
                translations += Reasoner.this.dict.getFromUSL(result.getUSL()).get("fr");
              } catch (MissingTranslationException e) {
                translations += "<no translation>"; // TODO try analogies on translations
              }

              usls = Reasoner.this.database.get(this.currentIndices.getFirst()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getSecond()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getThird()).getUSL() + "\t";
              usls += result.getUSL();
              return indices + "\n" + translations + "\n" + usls;
            }
            else throw new NoSuchElementException();
          }
        };
      }
    };
  }

  /**
   * Computes and returns the list of every productive analogical equation from the word database.
   * Note that only one equation is retained for each commutative cluster.
   * @return the list of the word triples that constitute productive equations.
   */
  public Iterable<Triple<Integer>> computeEquations() {
    return new Iterable<Triple<Integer>>() {

      @Override
      public Iterator<Triple<Integer>> iterator() {
        return new Iterator<Triple<Integer>>() {
          private int i = 0, j = 1, k = 1;
          private Triple<Integer> nextProductiveEquation = null;

          @Override
          public boolean hasNext() {
            if (this.nextProductiveEquation == null && this.i < Reasoner.this.database.size() && this.j < Reasoner.this.database.size() && this.k < Reasoner.this.database.size())
              this.nextProductiveEquation = this.computeNext();
            return this.nextProductiveEquation != null;
          }

          @Override
          public Triple<Integer> next() {
            if (this.hasNext()) {
              Triple<Integer> t = this.nextProductiveEquation;
              this.nextProductiveEquation = null;
              return t;
            }
            else
              throw new NoSuchElementException();
          }

          private Triple<Integer> computeNext() {
            this.k ++;
            while (this.i < Reasoner.this.database.size()) {
              T wi = Reasoner.this.database.get(this.i);
              if (this.j >= Reasoner.this.database.size())
                this.j = this.i + 1;
              while (this.j < Reasoner.this.database.size()) {
                T wj = Reasoner.this.database.get(this.j);
                if (this.k >= Reasoner.this.database.size())
                  this.k = this.j + 1;
                while (this.k < Reasoner.this.database.size()) {
                  assert(!(this.i == this.j || this.i == this.k));
                  T wk = Reasoner.this.database.get(this.k);
                  if (Reasoner.this.solveEquation(wi, wj, wk).iterator().hasNext())
                    return new Triple<Integer>(i, j, k);
                  this.k ++;
                }
                this.j ++;
              }
              this.i ++;
            }
            return null;
          }
        };
      }
    };
    //      LinkedList<Triple<Integer>> productiveEquations = new LinkedList<Triple<Integer>>();
    //
    //      for (int i = 0; i < list.size(); i++) {
    //        System.out.println();
    //        for (int j = i+1; j < list.size(); j++) {
    //          for (int k = j+1; k < list.size(); k++) {
    //            assert(!(i == j || i == k));
    //            T wi = list.get(i);
    //            T wj = list.get(j);
    //            T wk = list.get(k);
    //            if (solveSingleEquation(wi, wj, wk).iterator().hasNext()) {
    //              productiveEquations.add(new Triple<Integer>(i, j, k));
    //              System.out.println(i + "/" + list.size() + " " + productiveEquations.size());
    //            }
    //          }
    //        }
    //      }
    //
    //      return productiveEquations;
  }

  public String getVerifyingStats() {
    return this.verifyingStats;
  }

  public String getSolvingStats() {
    return this.solvingStats;
  }

  public HashMap<String, LinkedList<T>> searchPolysemy() {
    HashMap<String, LinkedList<T>> iemlPerTranslation = new HashMap<String, LinkedList<T>>();
    HashMap<String, LinkedList<T>> multiple = new HashMap<String, LinkedList<T>>();

    for (T term: this.database) {
      try {
        for (String singleTranslation: this.dict.getFromUSL(term.getUSL()).get(this.selectedLanguage)) {
          iemlPerTranslation.putIfAbsent(singleTranslation, new LinkedList<T>());
          LinkedList<T> iemlList = iemlPerTranslation.get(singleTranslation);
          iemlList.add(term);
          if (iemlList.size() == 2)
            multiple.put(singleTranslation, iemlList);
        }
      } catch (MissingTranslationException e) {
        // Cannot check polysemy for a non translated term
      }
    }
    return multiple;
  }
  
  public ArrayList<Thread> defaultGeneration() {
    ArrayList<Thread> threads = new ArrayList<Thread>();
    
    threads.add(new Thread(new Runnable() {
      @Override
      public void run() {
        String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-list.txt.bz2";
        PrintStream out;
        try {
          out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Writing database to " + filename + "...");
        for (int i = 0; i < Reasoner.this.database.size(); i++) {
          String output = Reasoner.this.database.get(i).getUSL() + "\t";
          try {
            output += Reasoner.this.dict.getFromUSL(Reasoner.this.database.get(i).getUSL()).get("fr");
          } catch (MissingTranslationException e) {
            output += "<no translation>";
          }
          out.println(output);
        }
        out.close();
      }
    }));

    threads.add(new Thread(new Runnable() {
      @Override
      public void run() {
        String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-polysemy.txt.bz2";
        PrintStream out;
        try {
          out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Writing polysemic elements to " + filename + "...");
        for (Entry<String, LinkedList<T>> mapping: Reasoner.this.searchPolysemy().entrySet()) {
          out.println(mapping.getKey() + ":");
          for (T term: mapping.getValue())
            try {
              out.println("\t" + term.getUSL() + " (" + Reasoner.this.dict.getFromUSL(term.getUSL()).get(Reasoner.this.selectedLanguage) + ")");
            } catch (MissingTranslationException e) {
              out.close();
              throw new RuntimeException("Unexpected exception.", e);
            }
        }
        out.close();
      }
    }));

    threads.add(new Thread(new Runnable() {
      @Override
      public void run() {
        String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-proportions.txt.bz2";
        PrintStream out;
        try {
          out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Writing proportions to " + filename + "...");
        for (String proportionResult: Reasoner.this.displayProportions())
          out.println(proportionResult);
        out.close();
      }
    }));

    threads.add(new Thread(new Runnable() {
      @Override
      public void run() {
        String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-equations.txt.bz2";
        PrintStream out;
        try {
          out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Writing equations to " + filename + "...");
        for (String equationResult: Reasoner.this.displayEquations())
          out.println(equationResult);
        out.close();
      }
    }));
    
    for (Thread thread: threads)
      thread.start();
    return threads;
  }


  public static void main(String[] args) throws JSONStructureException, InterruptedException {
    final String WORDS_SAMPLE_FILENAME = "resources/words_sample.json.bz2";
    final String DICTIONARY_FILENAME = "resources/dictionary.json.bz2";
    final String lang = "fr";

    Reasoner<Morpheme> morphemeReasoner;
    Reasoner<Polymorpheme> polymorphemeReasoner;
    Reasoner<Lexeme> lexemeReasoner;
    Reasoner<Word> wordReasoner;

    try {
      InputStream wordStream = new BZip2CompressorInputStream(new FileInputStream(WORDS_SAMPLE_FILENAME));
      InputStream dictStream = new BZip2CompressorInputStream(new FileInputStream(DICTIONARY_FILENAME));

      ArrayList<JSONObject> jsonTranslations = new ArrayList<JSONObject>();
      HashSet<String> usls = new HashSet<String>();
      {
        Scanner scanner = new Scanner(dictStream);
        scanner.useDelimiter("\\A");
        JSONArray arr = new JSONArray(scanner.next());
        for (int i = 0; i < arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          jsonTranslations.add(obj);

          if (!obj.isNull("usl"))  // this should better be identical in every type of json dump
            usls.add(obj.getString("usl"));
          else
            usls.add(obj.getString("ieml"));
        }
        scanner.close();
      }

      final Dictionary dict = new Dictionary(jsonTranslations);
      morphemeReasoner = new Reasoner<Morpheme>(dict, usls, WritableBuilder.MORPHEME_BUILDER_INSTANCE, lang);
      polymorphemeReasoner = new Reasoner<Polymorpheme>(dict, usls, WritableBuilder.POLYMORPHEME_BUILDER_INSTANCE, lang);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open IEML JSON exports. Please generate them first.", e);
    }

    ArrayList<Thread> threads = new ArrayList<Thread>();
    threads.addAll(morphemeReasoner.defaultGeneration());
    threads.addAll(polymorphemeReasoner.defaultGeneration());
    
    for (Thread thread: threads)
      thread.join();
  }

  private static String formatDuration(long millisInterval) {
    long abs = Math.abs(millisInterval);
    String str;
    if (millisInterval == 0)
      return "0";
    else {
      str = (abs % 1000) + "ms";
      abs = abs / 1000;
      if (abs > 0) {
        str = (abs % 60) + "s " + str;
        abs = abs / 60;
        if (abs > 0) {
          str = (abs % 60) + "m " + str;
          abs = abs / 60;
          if (abs > 0) {
            str = (abs % 24) + "h " + str;
            abs = abs / 24;
            if (abs > 0) {
              str = (abs % 365.25) + "d " + str;
              abs = (long) Math.floor(abs / 365.25);
              if (abs > 0) {
                str = "y " + str;
              }
            }
          }
        }
      }
    }
    return millisInterval < 0 ? "-" + str : str;
  }
}
