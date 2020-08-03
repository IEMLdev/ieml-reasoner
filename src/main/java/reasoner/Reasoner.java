package reasoner;

import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Relation;
import io.github.vletard.analogy.Solution;
import io.github.vletard.analogy.tuple.TupleEquation;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import parser.*;
import util.Pair;
import util.Quadruple;
import util.Triple;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Reasoner<T extends Writable> {
  public static final String DEFAULT_BASENAME = "/tmp/ieml-reasoner";
  public static final long PROGRESS_SAVING_DELAY_MILLIS = 10000;
  public static final String DEFAULT_LANGUAGE = "fr";
  private static final PrintStream LOG;
  static {
    try {
      LOG = new PrintStream(DEFAULT_BASENAME + ".log");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private final Dictionary dict;
  private final ArrayList<T> database;
  private final WritableBuilder<T> builder;
  String verifyingStats, solvingStats;
  private final String selectedLanguage;
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
    this.database = new ArrayList<>();
    this.builder = builder;
    this.parseFailed = new HashSet<>();
    this.selectedLanguage = translationLanguage;

    for (String usl: usls) {
      try {
        T parse = builder.parse(usl);
        if (!parse.getUSL().contentEquals(usl)) {
          synchronized (LOG) {
            LOG.println("Non matching parsed and generated USLs:\n" + usl + "\n" + parse.getUSL());
          }
        }
        this.database.add(parse);
        try {
          dict.get(parse);
        } catch (MissingTranslationException e) {
          synchronized (LOG) {
            LOG.println("No translation found for USL " + usl);
          }
        }
      } catch (ParseException e) {
        this.parseFailed.add(usl);
      }
    }

    if (!this.database.isEmpty())
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

  /** TODO optimize - dont create Iterator to determine this */
  private boolean solvableEquation(T ieml1, T ieml2, T ieml3) {
    return solveEquation(ieml1, ieml2, ieml3).hasNext();
  }

  private Iterator<Solution<T>> solveEquation(T ieml1, T ieml2, T ieml3) {
    return solutions(ieml1, ieml2, ieml3).iterator();
  }

  private Iterable<Solution<T>> solutions(T ieml1, T ieml2, T ieml3) {
    return new TupleEquation<>(ieml1, ieml2, ieml3, this.builder).uniqueSolutions();
  }

  public Iterable<String> displayProportions(String progressFilename) {
    return new Iterable<>() {

      @Override
      public Iterator<String> iterator() {
        return new Iterator<>() {
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
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getFirst())).get(DEFAULT_LANGUAGE) + " : ";
              } catch (MissingTranslationException e) {
                output += "<no " + DEFAULT_LANGUAGE + " translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getSecond())).get(DEFAULT_LANGUAGE) + " :: ";
              } catch (MissingTranslationException e) {
                output += "<no " + DEFAULT_LANGUAGE + " translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getThird())).get(DEFAULT_LANGUAGE) + " : ";
              } catch (MissingTranslationException e) {
                output += "<no " + DEFAULT_LANGUAGE + " translation> : ";
              }
              try {
                output += Reasoner.this.dict.get(Reasoner.this.database.get(t.getFourth())).get(DEFAULT_LANGUAGE) + "\n";
              } catch (MissingTranslationException e) {
                output += "<no " + DEFAULT_LANGUAGE + " translation> : ";
              }
              output += Reasoner.this.database.get(t.getFirst()).getUSL() + " : ";
              output += Reasoner.this.database.get(t.getSecond()).getUSL() + " :: ";
              output += Reasoner.this.database.get(t.getThird()).getUSL() + " : ";
              output += Reasoner.this.database.get(t.getFourth()).getUSL();
              return output;
            } else
              throw new NoSuchElementException();
          }

          private Quadruple<Integer> computeNext() {
            while (true) {
              this.l++;
              while (this.l >= Reasoner.this.database.size()) {
                this.k++;
                while (this.k >= Reasoner.this.database.size()) {
                  this.j++;
                  while (this.j >= Reasoner.this.database.size()) {
                    this.i++;
                    if (this.i >= Reasoner.this.database.size()) {
                      saveProgress(0, this.i - 1, this.j - 1, this.k - 1, this.l - 1);
                      return null;  // no next quadruple
                    } else
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
              if (new DefaultProportion<>(wi, wj, wk, wl).isValid())
                return new Quadruple<>(this.i, this.j, this.k, this.l);
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
                PrintStream out = new PrintStream(progressFilename);
                out.println(i + "\t" + j + "\t" + k + "\t" + l);
                out.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        };
      }
    };
  }

  public Iterable<Pair<String, String>> displayEquations(String baseFilename) {
    return new Iterable<>() {

      @Override
      public Iterator<Pair<String, String>> iterator() {
        return new Iterator<>() {
          private final Iterator<Triple<Integer>> indices = Reasoner.this.computeEquations(baseFilename + "-progress-equations-proportions.txt").iterator();
          private Iterator<Solution<T>> solutions = Collections.emptyIterator();
          private Triple<Integer> currentIndices;
          private final HashMap<Relation, Pair<Set<Pair<Triple<Integer>, Solution<T>>>, Set<Pair<Triple<Integer>, Solution<T>>>>> relationMap = new HashMap<>();

          @Override
          public boolean hasNext() {
            if (this.solutions.hasNext())
              return true;
            else if (this.indices.hasNext()) {
              Triple<Integer> t = this.indices.next();
              this.currentIndices = t;
              this.solutions = Reasoner.this.solveEquation(Reasoner.this.database.get(t.getFirst()), Reasoner.this.database.get(t.getSecond()), Reasoner.this.database.get(t.getThird()));
              assert (this.solutions.hasNext());
              return true;
            } else
              return false;
          }

          @Override
          public Pair<String, String> next() {
            if (this.hasNext()) {
              Solution<T> result = this.solutions.next();
              String indices = "", translations = "", usls = "", relations = "";
              boolean preexistingSolution = false;

              for (T item : Reasoner.this.database) {
                if (item.equals(result.getContent())) {
                  preexistingSolution = true;
                  break;
                }
              }

              indices = this.currentIndices.getFirst() + "\t" + this.currentIndices.getSecond() + "\t" + this.currentIndices.getThird();
              for (Integer i : this.currentIndices) {
                try {
                  translations += Reasoner.this.dict.get(Reasoner.this.database.get(i)).get(DEFAULT_LANGUAGE) + "\t";
                } catch (MissingTranslationException e) {
                  translations += "<no " + DEFAULT_LANGUAGE + " translation>\t";
                }
              }
              try {
                translations += Reasoner.this.dict.get(result.getContent()).get(DEFAULT_LANGUAGE);
                assert (preexistingSolution);
              } catch (MissingTranslationException e) {
                assert (!preexistingSolution);
                translations += "<no " + DEFAULT_LANGUAGE + " translation>"; // TODO try analogies on translations
              }

              usls = Reasoner.this.database.get(this.currentIndices.getFirst()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getSecond()).getUSL() + "\t";
              usls += Reasoner.this.database.get(this.currentIndices.getThird()).getUSL() + "\t";
              usls += result.getContent().getUSL();

              relations = "R1: " + result.getStraightRelation() + "\n";
              relations += "R2: " + result.getCrossedRelation();
              if (preexistingSolution) {
                this.updateRelations(result);
                return new Pair<>(null, indices + "\n" + translations + "\n" + usls + "\n" + relations + "\n");
              } else
                return new Pair<>(indices + "\n" + translations + "\n" + usls + "\n" + relations + "\n", null);
            } else throw new NoSuchElementException();
          }

          private void updateRelations(Solution<T> solution) {
            final Relation r1 = solution.getStraightRelation();
            final Relation r2 = solution.getCrossedRelation();

            this.relationMap.putIfAbsent(r1, new Pair<>(new HashSet<>(), new HashSet<>()));
            this.relationMap.putIfAbsent(r2, new Pair<>(new HashSet<>(), new HashSet<>()));

            this.relationMap.get(r1).first.add(new Pair<>(this.currentIndices, solution));
            this.relationMap.get(r2).second.add(new Pair<>(this.currentIndices, solution));

            try {
              PrintStream out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(baseFilename + "-relations.txt.bz2")));
              for (Entry<Relation, Pair<Set<Pair<Triple<Integer>, Solution<T>>>, Set<Pair<Triple<Integer>, Solution<T>>>>> entry : this.relationMap.entrySet()) {
                out.println(entry.getKey());
                for (Pair<Triple<Integer>, Solution<T>> p : entry.getValue().first) {
                  out.print(p.first + ":");
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getFirst())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getSecond())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getThird())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.println("\t" + Reasoner.this.dict.get(p.second.getContent()).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.println("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                }
                out.println();

                for (Pair<Triple<Integer>, Solution<T>> p : entry.getValue().second) {
                  out.print(p.first + ":");
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getFirst())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getSecond())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.print("\t" + Reasoner.this.dict.get(Reasoner.this.database.get(p.first.getThird())).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.print("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                  try {
                    out.println("\t" + Reasoner.this.dict.get(p.second.getContent()).get(DEFAULT_LANGUAGE));
                  } catch (MissingTranslationException e) {
                    out.println("\t<no " + DEFAULT_LANGUAGE + " translation>");
                  }
                }
                out.println();
                out.println();
              }
              out.close();
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
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
    return new Iterable<>() {

      @Override
      public Iterator<Triple<Integer>> iterator() {
        return new Iterator<>() {
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
            } else
              throw new NoSuchElementException();
          }

          private Triple<Integer> computeNext() {
            while (true) {
              this.k++;
              while (this.k >= Reasoner.this.database.size()) {
                this.j++;
                while (this.j >= Reasoner.this.database.size()) {
                  this.i++;
                  if (this.i >= Reasoner.this.database.size()) {
                    this.saveProgress(0, i - 1, j - 1, k - 1);
                    return null;  // no next triple 
                  } else
                    this.j = this.i + 1;
                }
                this.k = this.j + 1;
              }

              this.saveProgress(this.i, this.j, this.k);

              T wi = Reasoner.this.database.get(this.i);
              T wj = Reasoner.this.database.get(this.j);
              T wk = Reasoner.this.database.get(this.k);
              if (Reasoner.this.solvableEquation(wi, wj, wk))
                return new Triple<>(this.i, this.j, this.k);
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
                PrintStream out = new PrintStream(progressFilename);
                out.println(i + "\t" + j + "\t" + k + "\t(" + ((i + 1) * 100 / Reasoner.this.database.size()) + "%)");
                out.close();
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
    HashMap<String, LinkedList<T>> iemlPerTranslation = new HashMap<>();
    HashMap<String, LinkedList<T>> multiple = new HashMap<>();

    for (T term: this.database) {
      try {
        for (String singleTranslation: this.dict.get(term).get(this.selectedLanguage)) {
          iemlPerTranslation.putIfAbsent(singleTranslation, new LinkedList<>());
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
    ArrayList<Thread> threads = new ArrayList<>();

    threads.add(new Thread(() -> {
      String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-list.txt.bz2";
      PrintStream out;
      try {
        out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.out.println("Writing database to " + filename + "...");
      for (T t : Reasoner.this.database) {
        String output = t.getUSL() + "\t";
        try {
          output += Reasoner.this.dict.get(t).get(DEFAULT_LANGUAGE);
        } catch (MissingTranslationException e) {
          output += "<no " + DEFAULT_LANGUAGE + " translation>";
        }
        //          output += "\t" + Reasoner.this.database.get(i).toString();
        out.println(output);
      }
      out.close();
    }));

    threads.add(new Thread(() -> {
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
            out.println("\t" + term.getUSL() + " (" + Reasoner.this.dict.get(term).get(Reasoner.this.selectedLanguage) + ")");
          } catch (MissingTranslationException e) {
            out.close();
            throw new RuntimeException("Unexpected exception.", e);
          }
      }
      out.close();
    }));

    //    threads.add(new Thread(new Runnable() {
    //      @Override
    //      public void run() {
    //        String filename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-proportions.txt.bz2";
    //        PrintStream out;
    //        try {
    //          out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
    //        } catch (IOException e) {
    //          throw new RuntimeException(e);
    //        }
    //        System.out.println("Writing proportions to " + filename + "...");
    //        for (String proportionResult: Reasoner.this.displayProportions(filename + ".progress"))
    //          out.println(proportionResult);
    //        out.close();
    //      }
    //    }));

    threads.add(new Thread(() -> {
      String equationsFilename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-equations.txt.bz2";
      String proportionsFilename = Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName + "-proportions.txt.bz2";
      PrintStream equationsOut, proportionsOut;
      try {
        equationsOut = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(equationsFilename)));
        proportionsOut = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(proportionsFilename)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.out.println("Writing equations and proportions to " + equationsFilename + " and " + proportionsFilename + "...");
      for (Pair<String, String> result: Reasoner.this.displayEquations(Reasoner.DEFAULT_BASENAME + "-" + Reasoner.this.typeName)) {
        String equationResult = result.first;
        String proportionResult = result.second;

        if (equationResult != null) {
          assert(proportionResult == null);
          equationsOut.println(equationResult);
        }
        if (proportionResult != null) {
          assert(equationResult == null);
          proportionsOut.println(proportionResult);
        }
      }
      equationsOut.close();
      proportionsOut.close();
    }));

    for (Thread thread: threads)
      thread.start();
    return threads;
  }


  public static void main(String[] args) throws JSONStructureException, InterruptedException, ParseException {
    final String DICTIONARY_FILENAME = "resources/dictionary.json.bz2";

    Reasoner<Morpheme> morphemeReasoner;
    Reasoner<Polymorpheme> polymorphemeReasoner;
    Reasoner<Lexeme> lexemeReasoner;
    Reasoner<Word> wordReasoner;

    try {
      InputStream dictStream = new BZip2CompressorInputStream(new FileInputStream(DICTIONARY_FILENAME));

      ArrayList<JSONObject> jsonTranslations = new ArrayList<>();
      HashSet<String> usls = new HashSet<>();
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
      morphemeReasoner = new Reasoner<>(dict, usls, Morpheme.BUILDER, DEFAULT_LANGUAGE);
      polymorphemeReasoner = new Reasoner<>(dict, usls, Polymorpheme.BUILDER, DEFAULT_LANGUAGE);
      lexemeReasoner = new Reasoner<>(dict, usls, Lexeme.BUILDER, DEFAULT_LANGUAGE);
      wordReasoner = new Reasoner<>(dict, usls, Word.BUILDER, DEFAULT_LANGUAGE);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open IEML JSON exports. Please generate them first.", e);
    }

    ArrayList<Thread> threads = new ArrayList<>();
    threads.addAll(morphemeReasoner.defaultGeneration());
    threads.addAll(polymorphemeReasoner.defaultGeneration());
    threads.addAll(lexemeReasoner.defaultGeneration());
    threads.addAll(wordReasoner.defaultGeneration());

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
