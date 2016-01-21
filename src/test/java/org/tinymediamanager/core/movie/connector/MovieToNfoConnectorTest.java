package org.tinymediamanager.core.movie.connector;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

public class MovieToNfoConnectorTest {

  @BeforeClass
  public static void setup() {
    // create a default config file for config access
    Settings.getInstance("target/test-classes/");
  }

  @Test
  public void testKodiNfo() {
    FileUtils.deleteQuietly(new File("target/test-classes/xbmc_nfo/"));
    try {
      Movie movie = createXbmcMovie();

      MovieToXbmcNfoConnector xbmc = MovieToXbmcNfoConnector.createInstanceFromMovie(movie);
      assertThat(xbmc).isNotNull();

      // check values which does not get reimported
      assertThat(xbmc.outline).isNotEmpty();
      assertThat(xbmc.mpaa).isNotEmpty();
      assertThat(xbmc.certification).isNotEmpty();
      assertThat(xbmc.set).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails).isNotNull();
      assertThat(xbmc.fileinfo.streamdetails.video.codec).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.video.aspect).isNotEmpty().isNotEqualTo("0");
      assertThat(xbmc.fileinfo.streamdetails.video.width).isGreaterThan(0);
      assertThat(xbmc.fileinfo.streamdetails.video.height).isGreaterThan(0);
      assertThat(xbmc.fileinfo.streamdetails.video.durationinseconds).isGreaterThan(0);
      assertThat(xbmc.fileinfo.streamdetails.video.stereomode).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.audio).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.audio.get(0).codec).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.audio.get(0).language).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.audio.get(0).channels).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.subtitle).isNotEmpty();
      assertThat(xbmc.fileinfo.streamdetails.subtitle.get(0).language).isNotEmpty();
      assertThat(xbmc.playcount).isGreaterThan(0);

      // need to clean movie set because it is not reimportable in the unit test
      xbmc.set = "";

      // write it
      List<MovieNfoNaming> nfoNames = Arrays.asList(MovieNfoNaming.MOVIE_NFO);
      MovieToXbmcNfoConnector.writeNfoFiles(movie, xbmc, nfoNames);

      Path nfoFile = Paths.get("target/test-classes/xbmc_nfo/movie.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      Movie newMovie = MovieToXbmcNfoConnector.getData(nfoFile);
      compareMovies(movie, newMovie);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testMediaPortalNfo() {
    FileUtils.deleteQuietly(new File("target/test-classes/mp_nfo/"));
    try {
      Movie movie = createMpMovie();

      MovieToMpNfoConnector mp = MovieToMpNfoConnector.createInstanceFromMovie(movie);
      assertThat(mp).isNotNull();

      // check values which does not get reimported
      assertThat(mp.outline).isNotEmpty();
      assertThat(mp.mpaa).isNotEmpty();
      assertThat(mp.sets).isNotEmpty();
      assertThat(mp.sets.get(0).name).isNotEmpty();
      assertThat(mp.sets.get(0).order).isEqualTo(0); // is null because the lookup in the list returns -1 and 1 is added
      assertThat(mp.playcount).isGreaterThan(0);

      // need to clean movie set because it is not reimportable in the unit test
      mp.sets = new ArrayList<>();

      // write it
      List<MovieNfoNaming> nfoNames = Arrays.asList(MovieNfoNaming.FILENAME_NFO);
      MovieToMpNfoConnector.writeNfoFiles(movie, mp, nfoNames);

      Path nfoFile = Paths.get("target/test-classes/mp_nfo/Aladdin.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      Movie newMovie = MovieToMpNfoConnector.getData(nfoFile);
      compareMovies(movie, newMovie);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private void compareMovies(Movie movie, Movie newMovie) {
    assertThat(newMovie.getTitle()).isEqualTo(movie.getTitle());
    assertThat(newMovie.getOriginalTitle()).isEqualTo(movie.getOriginalTitle());
    assertThat(newMovie.getSortTitle()).isEqualTo(movie.getSortTitle());
    assertThat(newMovie.getRating()).isEqualTo(movie.getRating());
    assertThat(newMovie.getYear()).isEqualTo(movie.getYear());
    assertThat(newMovie.getTop250()).isEqualTo(movie.getTop250());
    assertThat(newMovie.getVotes()).isEqualTo(movie.getVotes());
    assertThat(newMovie.getPlot()).isEqualTo(movie.getPlot());
    assertThat(newMovie.getTagline()).isEqualTo(movie.getTagline());
    assertThat(newMovie.getRuntime()).isEqualTo(movie.getRuntime());
    assertThat(newMovie.getArtworkUrl(MediaFileType.POSTER)).isEqualTo(movie.getArtworkUrl(MediaFileType.POSTER));
    assertThat(newMovie.getArtworkUrl(MediaFileType.FANART)).isEqualTo(movie.getArtworkUrl(MediaFileType.FANART));
    assertThat(newMovie.getImdbId()).isEqualTo(movie.getImdbId());
    assertThat(newMovie.getTmdbId()).isEqualTo(movie.getTmdbId());
    assertThat(newMovie.getProductionCompany()).isEqualTo(movie.getProductionCompany());
    assertThat(newMovie.getCountry()).isEqualTo(movie.getCountry());
    assertThat(newMovie.getCertification()).isEqualTo(movie.getCertification());
    assertThat(newMovie.getTrailer().size()).isEqualTo(movie.getTrailer().size());
    if (!newMovie.getTrailer().isEmpty()) {
      assertThat(newMovie.getTrailer().get(0).getUrl()).isEqualTo(movie.getTrailer().get(0).getUrl());
    }
    assertThat(newMovie.getIds().size()).isEqualTo(movie.getIds().size());
    assertThat(newMovie.getId("trakt")).isEqualTo(movie.getId("trakt"));
    assertThat(newMovie.getReleaseDate()).isEqualTo(movie.getReleaseDate());
    assertThat(newMovie.isWatched()).isEqualTo(movie.isWatched());
    assertThat(newMovie.getGenres().size()).isEqualTo(movie.getGenres().size());
    assertThat(newMovie.getGenres().get(0)).isEqualTo(movie.getGenres().get(0));
    assertThat(newMovie.getWriter()).isEqualTo(movie.getWriter());
    assertThat(newMovie.getDirector()).isEqualTo(movie.getDirector());
    assertThat(newMovie.getTags().size()).isEqualTo(movie.getTags().size());
    if (!newMovie.getTags().isEmpty()) {
      assertThat(newMovie.getTags().get(0)).isEqualTo(movie.getTags().get(0));
    }
    assertThat(newMovie.getActors().size()).isEqualTo(movie.getActors().size());
    assertThat(newMovie.getActors().get(0)).isEqualTo(movie.getActors().get(0));
    assertThat(newMovie.getProducers().size()).isEqualTo(movie.getProducers().size());
    assertThat(newMovie.getProducers().get(0)).isEqualTo(movie.getProducers().get(0));
    assertThat(newMovie.getSpokenLanguages()).isEqualTo(movie.getSpokenLanguages());
    assertThat(newMovie.getMediaSource()).isEqualTo(movie.getMediaSource());
  }

  private Movie createXbmcMovie() throws Exception {
    Movie movie = new Movie();
    movie.setPath("target/test-classes/xbmc_nfo");
    movie.setTitle("Aladdin");
    movie.setOriginalTitle("Disneys Aladdin");
    movie.setSortTitle("Aladdin");
    movie.setRating(7.2f);
    movie.setYear("1992");
    movie.setTop250(199);
    movie.setVotes(5987);
    movie.setPlot("Princess Jasmine grows tired of being forced to remain in the...");
    movie.setTagline("Wish granted");
    movie.setRuntime(90);
    movie.setArtworkUrl("http://poster", MediaFileType.POSTER);
    movie.setArtworkUrl("http://fanart", MediaFileType.FANART);
    movie.setImdbId("tt0103639");
    movie.setTmdbId(812);
    movie.setId("trakt", 655);
    movie.setProductionCompany("Walt Disney");
    movie.setCountry("US");
    movie.setCertification(Certification.US_G);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    movie.setReleaseDate(sdf.parse("1992-11-25"));

    MovieTrailer trailer = new MovieTrailer();
    trailer.setUrl("https://trailer");
    trailer.setInNfo(true);
    movie.addTrailer(trailer);

    MovieSet movieSet = new MovieSet();
    movieSet.setTitle("Aladdin Collection");
    movie.setMovieSet(movieSet);

    // ToDo fileinfo
    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setFilename("Aladdin.mkv");
    mf.setVideoCodec("h264");
    mf.setVideoHeight(720);
    mf.setVideoWidth(1280);
    mf.setDuration(3600);
    mf.setVideo3DFormat(MediaFile.VIDEO_3D_SBS);

    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setChannels("6");
    mf.setAudioStreams(Arrays.asList(audio));

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    movie.addToMediaFiles(mf);

    movie.setWatched(true);
    movie.addGenre(MediaGenres.ADVENTURE);
    movie.addGenre(MediaGenres.FAMILY);
    movie.setWriter("Ted Elliott, Terry Rossio, Ron Clements, John Jusker");
    movie.setDirector("Ron Clements, John Musker");
    movie.addToTags("Disney");
    movie.addToTags("Oriental");

    movie.addActor(new MovieActor("Scott Weinger", "Aladdin 'Al' (voice)"));
    movie.addActor(new MovieActor("Robin Williams", "Genie (voice)"));

    movie.addProducer(new MovieProducer("Ron Clements", "Producer"));
    movie.addProducer(new MovieProducer("Donald W. Ernst", "Producer"));

    movie.setSpokenLanguages("en");
    movie.setMediaSource(MovieMediaSource.BLURAY);
    return movie;
  }

  private Movie createMpMovie() throws Exception {
    Movie movie = new Movie();
    movie.setPath("target/test-classes/mp_nfo");
    movie.setTitle("Aladdin");
    movie.setOriginalTitle("Disneys Aladdin");
    movie.setSortTitle("Aladdin");
    movie.setRating(7.2f);
    movie.setYear("1992");
    movie.setVotes(5987);
    movie.setPlot("Princess Jasmine grows tired of being forced to remain in the...");
    movie.setTagline("Wish granted");
    movie.setRuntime(90);
    movie.setImdbId("tt0103639");
    movie.setTmdbId(812);
    movie.setId("trakt", 655);
    movie.setProductionCompany("Walt Disney");
    movie.setCountry("US");
    movie.setCertification(Certification.US_G);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    movie.setReleaseDate(sdf.parse("1992-11-25"));

    MovieSet movieSet = new MovieSet();
    movieSet.setTitle("Aladdin Collection");
    movie.setMovieSet(movieSet);

    // ToDo fileinfo
    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setFilename("Aladdin.mkv");
    mf.setVideoCodec("h264");
    mf.setVideoHeight(720);
    mf.setVideoWidth(1280);
    mf.setDuration(3600);
    mf.setVideo3DFormat(MediaFile.VIDEO_3D_SBS);

    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setChannels("6");
    mf.setAudioStreams(Arrays.asList(audio));

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    movie.addToMediaFiles(mf);

    movie.setWatched(true);
    movie.addGenre(MediaGenres.ADVENTURE);
    movie.addGenre(MediaGenres.FAMILY);
    movie.setWriter("Ted Elliott, Terry Rossio, Ron Clements, John Jusker");
    movie.setDirector("Ron Clements, John Musker");

    movie.addActor(new MovieActor("Scott Weinger", "Aladdin 'Al' (voice)"));
    movie.addActor(new MovieActor("Robin Williams", "Genie (voice)"));

    movie.addProducer(new MovieProducer("Ron Clements", "Producer"));
    movie.addProducer(new MovieProducer("Donald W. Ernst", "Producer"));

    movie.setMediaSource(MovieMediaSource.BLURAY);
    return movie;
  }
}
