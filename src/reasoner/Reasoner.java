package reasoner;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import parser.JSONStructureException;
import parser.Lexeme;
import parser.MissingTranslationException;
import parser.Morpheme;
import parser.ParseException;
import parser.Polymorpheme;
import parser.Word;
import parser.Writable;
import parser.WritableBuilder;
import util.Quadruple;
import util.Triple;

public class Reasoner<T extends Writable> {
  public static final String DEFAULT_BASENAME = "/tmp/ieml-reasoner";
  public static final long PROGRESS_SAVING_DELAY_MILLIS = 30000;

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

  private Iterable<Solution<T>> solveEquation(T ieml1, T ieml2, T ieml3) {
    TupleEquation<IEMLUnit, T> e = new TupleEquation<IEMLUnit, T>(ieml1, ieml2, ieml3, this.builder);
    return new Iterable<Solution<T>>() {
      @Override
      public Iterator<Solution<T>> iterator() {
        return e.uniqueSolutions().iterator();
      }
    };
  }

  public Iterable<String> displayProportions(String progressFilename) {
    return new Iterable<String>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private int i = 0, j = 1, k = 2, l = -1;
          private Quadruple<Integer> nextValidProportion = null;

          @Override
          public boolean hasNext() {
            if (this.nextValidProportion == null && this.i < Reasoner.this.database.size() && this.j < Reasoner.this.database.size()
                && this.k < Reasoner.this.database.size() && this.l < Reasoner.this.database.size())
              this.nextValidProportion = this.computeNext();
            return this.nextValidProportion != null;
          }

          @Override
          public String next() {
            if (this.hasNext()) {
              Quadruple<Integer> t = this.nextValidProportion;
              this.nextValidProportion = null;
              String output = "";
              output += t.getFirst() + "\t" + t.getSecond() + "\t" + t.getThird() + "\t" + t.getFourth() + "\n";
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getFirst()).getUSL()).get("fr") + " : ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getSecond()).getUSL()).get("fr") + " :: ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getThird()).getUSL()).get("fr") + " : ";
              } catch (MissingTranslationException e) {
                output += "<no translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getFourth()).getUSL()).get("fr") + "\n";
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
            while (true) {
              this.l ++;
              while (this.l >= Reasoner.this.database.size()) {
                this.k ++;
                while (this.k >= Reasoner.this.database.size()) {
                  this.j ++;
                  while (this.j >= Reasoner.this.database.size()) {
                    this.i ++;
                    if (this.i >= Reasoner.this.database.size()) {
                      saveProgress(0, this.i-1, this.j-1, this.k-1, this.l-1);
                      return null;  // no next quadruple
                    }
                    else
                      this.j = this.i + 1;
                  }
                  this.k = this.j + 1;
                }
                this.l = 0;
              }
              
              this.saveProgress(this.i, this.j, this.k, this.l);

              T wi = Reasoner.this.database.get(this.i);
              T wj = Reasoner.this.database.get(this.j);
              T wk = Reasoner.this.database.get(this.k);
              T wl = Reasoner.this.database.get(this.l);
              if (new DefaultProportion<T>(wi, wj, wk, wl).isValid())
                return new Quadruple<Integer>(this.i, this.j, this.k, this.l);
            }
          }

          private void saveProgress(int i, int j, int k, int l) {
            this.saveProgress(PROGRESS_SAVING_DELAY_MILLIS, i, j, k, l);
          }
          
          private long saveDelay = 0;
          private void saveProgress(long delay, int i, int j, int k, int l) {
            long time = System.currentTimeMillis();
            if (time - this.saveDelay > delay) {
              this.saveDelay = time;
              try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(progressFilename));
                writer.write(i + "\t" + j + "\t" + k + "\t" + l + "\n");
                writer.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        };
      }
    };
  }

  public Iterable<String> displayEquations(String progressFilename) {
    return new Iterable<String>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private Iterator<Triple<Integer>> indices = Reasoner.this.computeEquations(progressFilename).iterator();
          private Iterator<Solution<T>> solutions = Collections.emptyIterator();
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
              Solution<T> result = this.solutions.next();
              String indices = "", translations = "", usls = "", relations = "", preexisting = "new";
              boolean preexistingSolution = false;
              
              for (T item: Reasoner.this.database) {
                if (item.getUSL().contentEquals(result.getContent().getUSL())) {
                  preexistingSolution = true;
                  preexisting = "in database";
                  break;
                }
              }
              
              indices = this.currentIndices.getFirst() + "\t" + this.currentIndices.getSecond() + "\t" + this.currentIndices.getThird();
              for (Integer i: this.currentIndices) {
                try {
                  translations += Reasoner.this.dict.get(Reasoner.this.database.get(i).getUSL()).get("fr") + "\t";
                } catch (MissingTranslationException e) {
                  translations += "<no translation>\t";
                }
              }
              try {
                translations += Reasoner.this.dict.get(result.getContent().getUSL()).get("fr");
                assert(preexistingSolution);
              } catch (MissingTranslationException e) {
                assert(!preexistingSolution);
                translations += "<no translation>"; // TODO try analogies on translations
              }

              usls = Reasoner.this.database.get(this.currentIndices.getFirst()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getSecond()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getThird()).getUSL() + "\t";
              usls += result.getContent().getUSL();
              
              relations = "R1: " + result.getRelation().displayStraight() + "\nR2: " + result.getRelation().displayCrossed();
              return indices + "\n" + translations + "\n" + usls + "\n" + relations + "\n" + preexisting + "\n";
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
  public Iterable<Triple<Integer>> computeEquations(String progressFilename) {
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
            while (true) {
              this.k ++;
              while (this.k >= Reasoner.this.database.size()) {
                this.j ++;
                while (this.j >= Reasoner.this.database.size()) {
                  this.i ++;
                  if (this.i >= Reasoner.this.database.size()) {
                    this.saveProgress(0, i-1, j-1, k-1);
                    return null;  // no next triple 
                  }
                  else
                    this.j = this.i + 1;
                }
                this.k = this.j + 1;
              }

              this.saveProgress(this.i, this.j, this.k);

              T wi = Reasoner.this.database.get(this.i);
              T wj = Reasoner.this.database.get(this.j);
              T wk = Reasoner.this.database.get(this.k);
              if (Reasoner.this.solveEquation(wi, wj, wk).iterator().hasNext())
                return new Triple<Integer>(this.i, this.j, this.k);
            }
          }
          
          private void saveProgress(int i, int j, int k) {
            this.saveProgress(PROGRESS_SAVING_DELAY_MILLIS, i, j, k);
          }

          private long saveDelay = 0;
          private void saveProgress(long delay, int i, int j, int k) {
            long time = System.currentTimeMillis();
            if (time - this.saveDelay > delay) {
              this.saveDelay = time;
              try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(progressFilename));
                writer.write(i + "\t" + j + "\t" + k + "\n");
                writer.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        };
      }
    };
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
        for (String singleTranslation: this.dict.get(term.getUSL()).get(this.selectedLanguage)) {
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
            output += Reasoner.this.dict.get(Reasoner.this.database.get(i).getUSL()).get("fr");
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
              out.println("\t" + term.getUSL() + " (" + Reasoner.this.dict.get(term.getUSL()).get(Reasoner.this.selectedLanguage) + ")");
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
        for (String proportionResult: Reasoner.this.displayProportions(filename + ".progress"))
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
        for (String equationResult: Reasoner.this.displayEquations(filename + ".progress"))
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
      morphemeReasoner = new Reasoner<Morpheme>(dict, usls, Morpheme.BUILDER, lang);
      polymorphemeReasoner = new Reasoner<Polymorpheme>(dict, usls, Polymorpheme.BUILDER, lang);
      lexemeReasoner = new Reasoner<Lexeme>(dict, usls, Lexeme.BUILDER, lang);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open IEML JSON exports. Please generate them first.", e);
    }

    ArrayList<Thread> threads = new ArrayList<Thread>();
    threads.addAll(morphemeReasoner.defaultGeneration());
    threads.addAll(polymorphemeReasoner.defaultGeneration());
    threads.addAll(lexemeReasoner.defaultGeneration());

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
