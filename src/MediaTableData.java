/**
 * 
 * @author marc
 *
 */
public class MediaTableData implements Comparable<MediaTableData> {


    /**
     * 
     * @param in_title
     * @param in_mpaaRating
     * @param in_year
     * @param in_runtime
     * @param in_director
     * @param in_imdbRating
     * @param in_base64Image
     */
    MediaTableData(final String in_title,
                   final String in_mpaaRating,
                   final int    in_year,
                   final int    in_runtime,
                   final String in_director,
                   final int in_imdbRating,
                   final String in_base64Image) {

        title = in_title;
        mpaaRating = in_mpaaRating;
        year = in_year;
        runtime = in_runtime;
        director = in_director;
        imdbRating = in_imdbRating;
        base64Image = in_base64Image;
    }


    /**
     * 
     */
    public int compareTo(final MediaTableData o) {
        return title.compareTo(o.title);
    }


    public String title;
    public String mpaaRating;
    public int    year;
    public int    runtime;
    public String director;
    public int    imdbRating;
    public String base64Image;
}
