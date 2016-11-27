/*
 * Copyright 2012 - 2016 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.movies;

import java.text.Collator;
import java.text.Normalizer;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

import org.tinymediamanager.core.movie.entities.Movie;

/**
 * The Class MovieComparator is used to (initial) sort the movies in the moviepanel.
 * 
 * @author Manuel Laggner
 */
public class MovieComparator implements Comparator<Movie> {
  private Collator stringCollator;

  public MovieComparator() {
    RuleBasedCollator defaultCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();
    try {
      // default collator ignores whitespaces
      // using hack from http://stackoverflow.com/questions/16567287/java-collation-ignores-space
      stringCollator = new RuleBasedCollator(defaultCollator.getRules().replace("<'\u005f'", "<' '<'\u005f'"));
    }
    catch (Exception e) {
      stringCollator = defaultCollator;
    }
  }

  @Override
  public int compare(Movie movie1, Movie movie2) {
    if (stringCollator != null) {
      String titleMovie1 = Normalizer.normalize(movie1.getTitleSortable().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
      String titleMovie2 = Normalizer.normalize(movie2.getTitleSortable().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
      return stringCollator.compare(titleMovie1, titleMovie2);
    }
    return movie1.getTitleSortable().toLowerCase(Locale.ROOT).compareTo(movie2.getTitleSortable().toLowerCase(Locale.ROOT));
  }
}
