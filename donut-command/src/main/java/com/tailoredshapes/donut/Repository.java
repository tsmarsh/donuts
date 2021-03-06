package com.tailoredshapes.donut;

import com.tailoredshapes.stash.Stash;

public interface Repository {
    Stash writeDoc(int id, Stash body);
    Stash readDoc(int id);
    Stash readDoc(int i, long toEpochMilli);
    Stash changeDoc(int id, Stash body);

    Stash deleteDoc(int id);
}

