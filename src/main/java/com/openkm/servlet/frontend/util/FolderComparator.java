package com.openkm.servlet.frontend.util;

import com.openkm.frontend.client.bean.GWTFolder;

public class FolderComparator extends CultureComparator<GWTFolder> {

    protected FolderComparator(final String locale) {
        super(locale);
    }

    public static FolderComparator getInstance(final String locale) {
        try {
            final FolderComparator comparator = (FolderComparator) CultureComparator
                    .getInstance(FolderComparator.class, locale);
            return comparator;
        } catch (final Exception e) {
            return new FolderComparator(locale);
        }
    }

    public static FolderComparator getInstance() {
        final FolderComparator instance = getInstance(CultureComparator.DEFAULT_LOCALE);
        return instance;
    }

    @Override
    public int compare(final GWTFolder arg0, final GWTFolder arg1) {
        final GWTFolder first = arg0;
        final GWTFolder second = arg1;

        return collator.compare(first.getName(), second.getName());
    }
}
