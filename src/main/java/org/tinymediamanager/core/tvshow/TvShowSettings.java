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
package org.tinymediamanager.core.tvshow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * The Class TvShowSettings.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "TvShowSettings")
public class TvShowSettings extends AbstractSettings {
  private static final Logger                  LOGGER                         = LoggerFactory.getLogger(TvShowSettings.class);

  public final static String                   CONFIG_FILE                    = "tvShows.xml";
  public final static String                   DEFAULT_RENAMER_FOLDER_PATTERN = "$N ($Y)";
  public final static String                   DEFAULT_RENAMER_SEASON_PATTERN = "Season $1";
  public final static String                   DEFAULT_RENAMER_FILE_PATTERN   = "$N - S$2E$E - $T";

  private static TvShowSettings                instance;

  /**
   * Constants mainly for events
   */
  private final static String                  PATH                           = "path";
  private final static String                  FILENAME                       = "filename";
  private final static String                  TV_SHOW_DATA_SOURCE            = "tvShowDataSource";
  private final static String                  TV_SHOW_SCRAPER                = "tvShowScraper";
  private final static String                  TV_SHOW_ARTWORK_SCRAPERS       = "tvShowArtworkScrapers";

  private final static String                  SCRAPE_BEST_IMAGE              = "scrapeBestImage";
  private final static String                  SCRAPER_LANGU                  = "scraperLanguage";
  private final static String                  SUBTITLE_SCRAPER_LANGU         = "subtitleScraperLanguage";
  private final static String                  CERTIFICATION_COUNTRY          = "certificationCountry";
  private final static String                  RENAMER_SEASON_FOLDER          = "renamerSeasonFoldername";
  private final static String                  BUILD_IMAGE_CACHE_ON_IMPORT    = "buildImageCacheOnImport";
  private final static String                  ASCII_REPLACEMENT              = "asciiReplacement";
  private final static String                  BAD_WORDS                      = "badWords";
  private final static String                  ENTRY                          = "entry";
  private final static String                  TV_SHOW_SKIP_FOLDERS           = "tvShowSkipFolders";
  private final static String                  TV_SHOW_SUBTITLE_SCRAPERS      = "tvShowSubtitleScrapers";
  private final static String                  UI_FILTERS                     = "uiFilters";
  private final static String                  STORE_UI_FILTERS               = "storeUiFilters";
  private final static String                  EPISODE_THUMB_FILENAME         = "episodeThumbFilename";

  @XmlElementWrapper(name = TV_SHOW_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>                   tvShowDataSources              = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = BAD_WORDS)
  @XmlElement(name = ENTRY)
  private final List<String>                   badWords                       = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = TV_SHOW_ARTWORK_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>                   tvShowArtworkScrapers          = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = TV_SHOW_SKIP_FOLDERS)
  @XmlElement(name = ENTRY)
  private final List<String>                   tvShowSkipFolders              = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = TV_SHOW_SUBTITLE_SCRAPERS)
  @XmlElement(name = ENTRY)
  private final List<String>                   tvShowSubtitleScrapers         = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = EPISODE_THUMB_FILENAME)
  @XmlElement(name = FILENAME)
  private final List<TvShowEpisodeThumbNaming> episodeThumbFilenames          = new ArrayList<>();

  private Map<String, String>                  uiFilters                      = new HashMap<>();

  private String                               tvShowScraper                  = Constants.TVDB;
  private boolean                              scrapeBestImage                = true;
  private MediaLanguages                       scraperLanguage                = MediaLanguages.en;
  private MediaLanguages                       subtitleScraperLanguage        = MediaLanguages.en;
  private CountryCode                          certificationCountry           = CountryCode.US;
  private String                               renamerTvShowFoldername        = DEFAULT_RENAMER_FOLDER_PATTERN;
  private String                               renamerSeasonFoldername        = DEFAULT_RENAMER_SEASON_PATTERN;
  private String                               renamerFilename                = DEFAULT_RENAMER_FILE_PATTERN;
  private boolean                              buildImageCacheOnImport        = false;
  private boolean                              asciiReplacement               = false;
  private boolean                              renamerSpaceSubstitution       = false;
  private String                               renamerSpaceReplacement        = "_";
  private LanguageStyle                        subtitleLanguageStyle          = LanguageStyle.ISO3T;
  private boolean                              syncTrakt                      = false;
  private boolean                              dvdOrder                       = false;
  private boolean                              storeUiFilters                 = false;
  private boolean                              displayMissingEpisodes         = false;
  private TvShowScraperMetadataConfig          tvShowScraperMetadataConfig    = null;

  public TvShowSettings() {
    addPropertyChangeListener(evt -> setDirty());
    tvShowScraperMetadataConfig = new TvShowScraperMetadataConfig();
    tvShowScraperMetadataConfig.addPropertyChangeListener(ect -> setDirty());
  }

  /**
   * Gets the single instance of TvShowSettings.
   *
   * @return single instance of TvShowSettings
   */
  public synchronized static TvShowSettings getInstance() {
    return getInstance(Settings.getInstance().getSettingsFolder());
  }

  /**
   * Override our settings folder (defaults to "data")<br>
   * <b>Should only be used for unit testing et all!</b><br>
   *
   * @return single instance of TvShowSettings
   */
  public synchronized static TvShowSettings getInstance(String folder) {
    if (instance == null) {
      instance = (TvShowSettings) getInstance(folder, CONFIG_FILE, TvShowSettings.class);
    }
    return instance;
  }

  @Override
  protected String getConfigFilename() {
    return CONFIG_FILE;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void writeDefaultSettings() {
    // activate default scrapers
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.SUBTITLE)) {
      addTvShowSubtitleScraper(ms.getId());
    }
    for (MediaScraper ms : MediaScraper.getMediaScrapers(ScraperType.TV_SHOW_ARTWORK)) {
      addTvShowArtworkScraper(ms.getId());
    }

    // set default languages based on java instance
    String defaultLang = Locale.getDefault().getLanguage();
    CountryCode cc = CountryCode.getByCode(defaultLang.toUpperCase());
    if (cc != null) {
      setCertificationCountry(cc);
    }
    for (MediaLanguages ml : MediaLanguages.values()) {
      if (ml.name().equals(defaultLang)) {
        setScraperLanguage(ml);
      }
    }

    episodeThumbFilenames.add(TvShowEpisodeThumbNaming.FILENAME);

    saveSettings();
  }

  public void addTvShowDataSources(String path) {
    if (!tvShowDataSources.contains(path)) {
      tvShowDataSources.add(path);
      firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
    }
  }

  public void removeTvShowDataSources(String path) {
    TvShowList tvShowList = TvShowList.getInstance();
    tvShowList.removeDatasource(path);
    tvShowDataSources.remove(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  public List<String> getTvShowDataSource() {
    return tvShowDataSources;
  }

  public String getTvShowScraper() {
    if (StringUtils.isBlank(tvShowScraper)) {
      return Constants.TVDB;
    }
    return tvShowScraper;
  }

  public void setTvShowScraper(String newValue) {
    String oldValue = this.tvShowScraper;
    this.tvShowScraper = newValue;
    firePropertyChange(TV_SHOW_SCRAPER, oldValue, newValue);
  }

  public void addTvShowArtworkScraper(String newValue) {
    if (!tvShowArtworkScrapers.contains(newValue)) {
      tvShowArtworkScrapers.add(newValue);
      firePropertyChange(TV_SHOW_ARTWORK_SCRAPERS, null, tvShowArtworkScrapers);
    }
  }

  public void removeTvShowArtworkScraper(String newValue) {
    if (tvShowArtworkScrapers.contains(newValue)) {
      tvShowArtworkScrapers.remove(newValue);
      firePropertyChange(TV_SHOW_ARTWORK_SCRAPERS, null, tvShowArtworkScrapers);
    }
  }

  public List<String> getTvShowArtworkScrapers() {
    return tvShowArtworkScrapers;
  }

  public boolean isScrapeBestImage() {
    return scrapeBestImage;
  }

  public void setScrapeBestImage(boolean newValue) {
    boolean oldValue = this.scrapeBestImage;
    this.scrapeBestImage = newValue;
    firePropertyChange(SCRAPE_BEST_IMAGE, oldValue, newValue);
  }

  public MediaLanguages getScraperLanguage() {
    return scraperLanguage;
  }

  public void setScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange(SCRAPER_LANGU, oldValue, newValue);
  }

  public MediaLanguages getSubtitleScraperLanguage() {
    return subtitleScraperLanguage;
  }

  public void setSubtitleScraperLanguage(MediaLanguages newValue) {
    MediaLanguages oldValue = this.subtitleScraperLanguage;
    this.subtitleScraperLanguage = newValue;
    firePropertyChange(SUBTITLE_SCRAPER_LANGU, oldValue, newValue);
  }

  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  public String getRenamerSeasonFoldername() {
    return renamerSeasonFoldername;
  }

  public void setRenamerSeasonFoldername(String newValue) {
    String oldValue = this.renamerSeasonFoldername;
    this.renamerSeasonFoldername = newValue;
    firePropertyChange(RENAMER_SEASON_FOLDER, oldValue, newValue);
  }

  public String getRenamerTvShowFoldername() {
    return renamerTvShowFoldername;
  }

  public void setRenamerTvShowFoldername(String newValue) {
    String oldValue = this.renamerTvShowFoldername;
    this.renamerTvShowFoldername = newValue;
    firePropertyChange("renamerTvShowFoldername", oldValue, newValue);
  }

  public String getRenamerFilename() {
    return renamerFilename;
  }

  public void setRenamerFilename(String newValue) {
    String oldValue = this.renamerFilename;
    this.renamerFilename = newValue;
    firePropertyChange("renamerFilename", oldValue, newValue);
  }

  public boolean isBuildImageCacheOnImport() {
    return buildImageCacheOnImport;
  }

  public void setBuildImageCacheOnImport(boolean newValue) {
    boolean oldValue = this.buildImageCacheOnImport;
    this.buildImageCacheOnImport = newValue;
    firePropertyChange(BUILD_IMAGE_CACHE_ON_IMPORT, oldValue, newValue);
  }

  public boolean isAsciiReplacement() {
    return asciiReplacement;
  }

  public void setAsciiReplacement(boolean newValue) {
    boolean oldValue = this.asciiReplacement;
    this.asciiReplacement = newValue;
    firePropertyChange(ASCII_REPLACEMENT, oldValue, newValue);
  }

  public String getRenamerSpaceReplacement() {
    return renamerSpaceReplacement;
  }

  public void setRenamerSpaceReplacement(String newValue) {
    String oldValue = this.renamerSpaceReplacement;
    this.renamerSpaceReplacement = newValue;
    firePropertyChange("renamerReplacement", oldValue, newValue);
  }

  public boolean isRenamerSpaceSubstitution() {
    return renamerSpaceSubstitution;
  }

  public void setRenamerSpaceSubstitution(boolean newValue) {
    boolean oldValue = this.renamerSpaceSubstitution;
    this.renamerSpaceSubstitution = newValue;
    firePropertyChange("renamerSpaceSubstitution", oldValue, newValue);
  }

  public void setSyncTrakt(boolean newValue) {
    boolean oldValue = this.syncTrakt;
    this.syncTrakt = newValue;
    firePropertyChange("syncTrakt", oldValue, newValue);
  }

  public boolean getSyncTrakt() {
    return syncTrakt;
  }

  public boolean isDvdOrder() {
    return dvdOrder;
  }

  public void setDvdOrder(boolean newValue) {
    boolean oldValue = this.dvdOrder;
    this.dvdOrder = newValue;
    firePropertyChange("dvdOrder", oldValue, newValue);
  }

  public void addTvShowSkipFolder(String newValue) {
    if (!tvShowSkipFolders.contains(newValue)) {
      tvShowSkipFolders.add(newValue);
      firePropertyChange(TV_SHOW_SKIP_FOLDERS, null, tvShowSkipFolders);
    }
  }

  public void removeTvShowSkipFolder(String newValue) {
    if (tvShowSkipFolders.contains(newValue)) {
      tvShowSkipFolders.remove(newValue);
      firePropertyChange(TV_SHOW_SKIP_FOLDERS, null, tvShowSkipFolders);
    }
  }

  public List<String> getTvShowSkipFolders() {
    return tvShowSkipFolders;
  }

  public void addBadWord(String badWord) {
    if (!badWords.contains(badWord.toLowerCase())) {
      badWords.add(badWord.toLowerCase());
      firePropertyChange(BAD_WORDS, null, badWords);
    }
  }

  public void removeBadWord(String badWord) {
    badWords.remove(badWord.toLowerCase());
    firePropertyChange(BAD_WORDS, null, badWords);
  }

  public List<String> getBadWords() {
    // convert to lowercase for easy contains checking
    ListIterator<String> iterator = badWords.listIterator();
    while (iterator.hasNext()) {
      iterator.set(iterator.next().toLowerCase());
    }
    return badWords;
  }

  public void addEpisodeThumbFilename(TvShowEpisodeThumbNaming filename) {
    if (!episodeThumbFilenames.contains(filename)) {
      episodeThumbFilenames.add(filename);
      firePropertyChange(EPISODE_THUMB_FILENAME, null, episodeThumbFilenames);
    }
  }

  public void clearEpisodeThumbFilenames() {
    episodeThumbFilenames.clear();
    firePropertyChange(EPISODE_THUMB_FILENAME, null, episodeThumbFilenames);
  }

  public List<TvShowEpisodeThumbNaming> getEpisodeThumbFilenames() {
    return new ArrayList<>(this.episodeThumbFilenames);
  }

  public void addTvShowSubtitleScraper(String newValue) {
    if (!tvShowSubtitleScrapers.contains(newValue)) {
      tvShowSubtitleScrapers.add(newValue);
      firePropertyChange(TV_SHOW_SUBTITLE_SCRAPERS, null, tvShowSubtitleScrapers);
    }
  }

  public void removeTvShowSubtitleScraper(String newValue) {
    if (tvShowSubtitleScrapers.contains(newValue)) {
      tvShowSubtitleScrapers.remove(newValue);
      firePropertyChange(TV_SHOW_SUBTITLE_SCRAPERS, null, tvShowSubtitleScrapers);
    }
  }

  public List<String> getTvShowSubtitleScrapers() {
    return tvShowSubtitleScrapers;
  }

  public LanguageStyle getSubtitleLanguageStyle() {
    return subtitleLanguageStyle;
  }

  public void setSubtitleLanguageStyle(LanguageStyle newValue) {
    LanguageStyle oldValue = this.subtitleLanguageStyle;
    this.subtitleLanguageStyle = newValue;
    firePropertyChange("subtitleLanguageStyle", oldValue, newValue);
  }

  public void setUiFilters(Map<String, String> filters) {
    uiFilters = filters;
    firePropertyChange(UI_FILTERS, null, uiFilters);
  }

  @XmlElement(name = UI_FILTERS)
  public Map<String, String> getUiFilters() {
    if (storeUiFilters) {
      return uiFilters;
    }
    return new HashMap<>();
  }

  public void setStoreUiFilters(boolean newValue) {
    boolean oldValue = this.storeUiFilters;
    this.storeUiFilters = newValue;
    firePropertyChange(STORE_UI_FILTERS, oldValue, newValue);
  }

  public boolean isStoreUiFilters() {
    return storeUiFilters;
  }

  public boolean isDisplayMissingEpisodes() {
    return displayMissingEpisodes;
  }

  public void setDisplayMissingEpisodes(boolean newValue) {
    boolean oldValue = this.displayMissingEpisodes;
    this.displayMissingEpisodes = newValue;
    firePropertyChange("displayMissingEpisodes", oldValue, newValue);
  }

  /**
   * Gets the tv show scraper metadata config.
   *
   * @return the tv show scraper metadata config
   */
  public TvShowScraperMetadataConfig getTvShowScraperMetadataConfig() {
    return tvShowScraperMetadataConfig;
  }

  /**
   * Sets the tv show scraper metadata config.
   *
   * @param scraperMetadataConfig
   *          the new tv show scraper metadata config
   */
  public void setTvShowScraperMetadataConfig(TvShowScraperMetadataConfig scraperMetadataConfig) {
    this.tvShowScraperMetadataConfig = scraperMetadataConfig;
    this.tvShowScraperMetadataConfig.addPropertyChangeListener(evt -> setDirty());
  }
}
