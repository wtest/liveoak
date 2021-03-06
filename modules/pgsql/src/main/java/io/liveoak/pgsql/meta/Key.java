/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Key {
    private List<Column> cols;

    Key(List<Column> cols) {
        if (cols == null) {
            this.cols = Collections.emptyList();
        } else {
            this.cols = Collections.unmodifiableList(new ArrayList(cols));
        }
    }

    public boolean isEmpty() {
        return cols.isEmpty();
    }

    public Column getColumn(String name) {
        for (Column c : cols) {
            if (c.name().equals(name)) {
                return c;
            }
        }
        return null;
    }

    public List<Column> columns() {
        return cols;
    }

    public int indexForColumn(String name) {
        int i = 0;
        for (Column c : cols) {
            if (c.name().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public boolean sameColumnsAs(Key key) {
        return columns().equals(key.columns());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!cols.equals(key.cols)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return cols.hashCode();
    }
}