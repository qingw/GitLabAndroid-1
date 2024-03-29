package com.commit451.gitlab.util;

import android.net.Uri;

import retrofit.Response;
import timber.log.Timber;

/**
 * Gets the pages
 * https://github.com/gitlabhq/gitlabhq/tree/master/doc/api#pagination
 */
public final class PaginationUtil {

    private static final String PREV_PAGE_SUFFIX = "rel=\"prev\"";
    private static final String NEXT_PAGE_SUFFIX = "rel=\"next\"";
    private static final String FIRST_PAGE_SUFFIX = "rel=\"first\"";
    private static final String LAST_PAGE_SUFFIX = "rel=\"last\"";

    private PaginationUtil() {}

    public static PaginationData parse(Response response) {
        Uri prev = null;
        Uri next = null;
        Uri first = null;
        Uri last = null;

        String header = response.headers().get("Link");
        if (header != null) {
            String[] parts = header.split(",");
            for (String part : parts) {
                try {
                    int linkStart = part.indexOf('<') + 1;
                    int linkEnd = part.indexOf('>');

                    Uri link = Uri.parse(part.substring(linkStart, linkEnd));

                    if (part.contains(PREV_PAGE_SUFFIX)) {
                        prev = link;
                    } else if (part.contains(NEXT_PAGE_SUFFIX)) {
                        next = link;
                    } else if (part.contains(FIRST_PAGE_SUFFIX)) {
                        first = link;
                    } else if (part.contains(LAST_PAGE_SUFFIX)) {
                        last = link;
                    }
                } catch (Exception e) {
                    Timber.e(e, "An error occurred");
                }
            }
        }

        return new PaginationData(prev, next, first, last);
    }

    public static class PaginationData {
        private final Uri prev;
        private final Uri next;
        private final Uri first;
        private final Uri last;

        private PaginationData(Uri prev, Uri next, Uri first, Uri last) {
            this.prev = prev;
            this.next = next;
            this.first = first;
            this.last = last;
        }

        public Uri getPrev() {
            return prev;
        }

        public Uri getNext() {
            return next;
        }

        public Uri getFirst() {
            return first;
        }

        public Uri getLast() {
            return last;
        }
    }
}
